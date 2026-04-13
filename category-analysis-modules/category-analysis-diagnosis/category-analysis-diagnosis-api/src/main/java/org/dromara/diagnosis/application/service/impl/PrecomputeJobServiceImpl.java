package org.dromara.diagnosis.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.request.PrecomputeJobCreateRequest;
import org.dromara.diagnosis.api.response.PrecomputeEventResponse;
import org.dromara.diagnosis.api.response.PrecomputeJobProgressResponse;
import org.dromara.diagnosis.api.response.PrecomputeJobResponse;
import org.dromara.diagnosis.api.response.PrecomputeWindowResponse;
import org.dromara.diagnosis.application.batch.service.DiagnosisPrecomputeBatchRunner;
import org.dromara.diagnosis.application.batch.service.DiagnosisWindowPlanService;
import org.dromara.diagnosis.application.service.DiagnosisProgressCacheService;
import org.dromara.diagnosis.application.service.DiagnosisRequestHashService;
import org.dromara.diagnosis.application.service.PrecomputeJobService;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeEventRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeWindowRow;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 预计算任务服务实现.
 */
@Service
@RequiredArgsConstructor
public class PrecomputeJobServiceImpl implements PrecomputeJobService {

    private static final String DEFAULT_TENANT_ID = "000000";

    private static final String STATUS_PENDING = "PENDING";

    private static final String STATUS_STOPPED = "STOPPED";

    private static final String STATUS_RETRYING = "RETRYING";

    private static final String STATUS_RUNNING = "RUNNING";

    private static final String MODULE_DIAGNOSIS = "DIAGNOSIS";

    private final DiagnosisPrecomputeMapper precomputeMapper;

    private final DiagnosisWindowPlanService windowPlanService;

    private final DiagnosisPrecomputeBatchRunner batchRunner;

    private final DiagnosisProgressCacheService progressCacheService;

    private final DiagnosisRequestHashService requestHashService;

    @Override
    public PrecomputeJobResponse createJob(PrecomputeJobCreateRequest request) {
        String requestHash = requestHashService.buildHash(request);
        if (!Boolean.TRUE.equals(request.getForceRebuild())) {
            DiagnosisPrecomputeJobRow active = precomputeMapper.selectLatestActiveJobByRequestHash(DEFAULT_TENANT_ID, requestHash);
            if (active != null) {
                return getJob(active.getJobId());
            }
        }

        DiagnosisPrecomputeJobRow row = new DiagnosisPrecomputeJobRow();
        row.setTenantId(DEFAULT_TENANT_ID);
        row.setJobCode("JOB-" + UUID.randomUUID().toString().replace("-", ""));
        row.setModuleCode(StringUtils.hasText(request.getModule()) ? request.getModule() : MODULE_DIAGNOSIS);
        row.setStatusCode(STATUS_PENDING);
        row.setPriority(request.getPriority() == null ? 5 : request.getPriority());
        row.setRequestJson(request.getRequestJson());
        row.setRequestHash(requestHash);
        row.setReadRangeType(request.getReadRangeType());
        row.setReadStart(request.getReadStart());
        row.setReadEnd(request.getReadEnd());
        row.setWindowTypes(request.getWindowTypes());
        row.setForceRebuild(Boolean.TRUE.equals(request.getForceRebuild()) ? "Y" : "N");
        row.setMaxRetry(request.getMaxRetry() == null ? 1 : Math.max(0, request.getMaxRetry()));
        row.setRetryCount(0);
        row.setProgressPercent(BigDecimal.ZERO);
        row.setCurrentStage("INIT");
        row.setTotalWindows(0);
        row.setDoneWindows(0);
        row.setRowsRead(0L);
        row.setRowsWritten(0L);
        row.setSubmittedBy(request.getSubmitterId());
        row.setSubmittedTime(LocalDateTime.now());

        precomputeMapper.insertJob(row);
        List<DiagnosisPrecomputeWindowRow> windows;
        try {
            windows = windowPlanService.createWindows(
                row,
                request.getCompareStart(),
                request.getCompareEnd()
            );
        } catch (IllegalArgumentException ex) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, ex.getMessage());
        }
        if (windows.isEmpty()) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "未生成任何预计算窗口");
        }
        DiagnosisPrecomputeWindowRow window = windows.get(0);

        DiagnosisPrecomputeJobRow progress = new DiagnosisPrecomputeJobRow();
        progress.setTenantId(DEFAULT_TENANT_ID);
        progress.setJobId(row.getJobId());
        progress.setProgressPercent(BigDecimal.ZERO);
        progress.setCurrentStage("PLAN_WINDOW");
        progress.setTotalWindows(windows.size());
        progress.setDoneWindows(0);
        progress.setRowsRead(0L);
        progress.setRowsWritten(0L);
        precomputeMapper.updateJobProgress(progress);
        progressCacheService.saveFromJobRow(precomputeMapper.selectJobById(DEFAULT_TENANT_ID, row.getJobId()));

        DiagnosisPrecomputeJobRow status = new DiagnosisPrecomputeJobRow();
        status.setTenantId(DEFAULT_TENANT_ID);
        status.setJobId(row.getJobId());
        status.setStatusCode(STATUS_RUNNING);
        status.setCurrentStage("INIT");
        status.setStartedTime(LocalDateTime.now());
        precomputeMapper.updateJobStatus(status);
        progressCacheService.saveFromJobRow(precomputeMapper.selectJobById(DEFAULT_TENANT_ID, row.getJobId()));

        String dataVersion = "V" + System.currentTimeMillis();
        batchRunner.launch(row.getJobId(), window.getWindowId(), window.getPeriodStart(), window.getPeriodEnd(), dataVersion, row.getRequestJson());
        return getJob(row.getJobId());
    }

    @Override
    public PrecomputeJobResponse getJob(Long jobId) {
        DiagnosisPrecomputeJobRow row = precomputeMapper.selectJobById(DEFAULT_TENANT_ID, jobId);
        if (row == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "预计算任务不存在");
        }
        PrecomputeJobResponse response = toJobResponse(row);
        response.setWindows(getJobWindows(jobId));
        return response;
    }

    @Override
    public PrecomputeJobProgressResponse getJobProgress(Long jobId) {
        PrecomputeJobProgressResponse cache = progressCacheService.get(jobId);
        if (cache != null) {
            return cache;
        }

        DiagnosisPrecomputeJobRow row = precomputeMapper.selectJobById(DEFAULT_TENANT_ID, jobId);
        if (row == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "预计算任务不存在");
        }
        PrecomputeJobProgressResponse response = toProgressResponse(row);
        progressCacheService.save(response);
        return response;
    }

    @Override
    public List<PrecomputeWindowResponse> getJobWindows(Long jobId) {
        List<DiagnosisPrecomputeWindowRow> rows = precomputeMapper.selectWindowsByJobId(DEFAULT_TENANT_ID, jobId);
        return rows.stream().map(this::toWindowResponse).collect(Collectors.toList());
    }

    @Override
    public List<PrecomputeEventResponse> getJobEvents(Long jobId, Integer limit) {
        DiagnosisPrecomputeJobRow row = precomputeMapper.selectJobById(DEFAULT_TENANT_ID, jobId);
        if (row == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "预计算任务不存在");
        }
        int safeLimit = limit == null ? 200 : Math.max(1, Math.min(limit, 1000));
        List<DiagnosisPrecomputeEventRow> rows = precomputeMapper.selectRecentEventsByJobId(DEFAULT_TENANT_ID, jobId, safeLimit);
        return rows.stream().map(this::toEventResponse).collect(Collectors.toList());
    }

    @Override
    public void stopJob(Long jobId) {
        DiagnosisPrecomputeJobRow row = precomputeMapper.selectJobById(DEFAULT_TENANT_ID, jobId);
        if (row == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "预计算任务不存在");
        }
        DiagnosisPrecomputeJobRow update = new DiagnosisPrecomputeJobRow();
        update.setTenantId(DEFAULT_TENANT_ID);
        update.setJobId(jobId);
        update.setStatusCode(STATUS_STOPPED);
        update.setCurrentStage("STOPPED");
        update.setFinishedTime(LocalDateTime.now());
        update.setErrorMessage("任务已人工停止");
        precomputeMapper.updateJobStatus(update);
        progressCacheService.saveFromJobRow(precomputeMapper.selectJobById(DEFAULT_TENANT_ID, jobId));
    }

    @Override
    public void retryJob(Long jobId) {
        DiagnosisPrecomputeJobRow row = precomputeMapper.selectJobById(DEFAULT_TENANT_ID, jobId);
        if (row == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "预计算任务不存在");
        }
        int currentRetry = row.getRetryCount() == null ? 0 : row.getRetryCount();
        int maxRetry = row.getMaxRetry() == null ? 1 : row.getMaxRetry();
        if (currentRetry >= maxRetry) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "已达到最大重试次数: " + maxRetry);
        }

        DiagnosisPrecomputeJobRow update = new DiagnosisPrecomputeJobRow();
        update.setTenantId(DEFAULT_TENANT_ID);
        update.setJobId(jobId);
        update.setStatusCode(STATUS_RETRYING);
        update.setCurrentStage("INIT");
        update.setRetryCount(currentRetry + 1);
        update.setStartedTime(null);
        update.setFinishedTime(null);
        update.setErrorMessage(null);
        precomputeMapper.updateJobStatus(update);

        List<DiagnosisPrecomputeWindowRow> windows = precomputeMapper.selectWindowsByJobId(DEFAULT_TENANT_ID, jobId);
        for (DiagnosisPrecomputeWindowRow window : windows) {
            if (!"SUCCESS".equalsIgnoreCase(window.getStatusCode())) {
                DiagnosisPrecomputeWindowRow windowUpdate = new DiagnosisPrecomputeWindowRow();
                windowUpdate.setTenantId(DEFAULT_TENANT_ID);
                windowUpdate.setWindowId(window.getWindowId());
                windowUpdate.setStatusCode("PENDING");
                windowUpdate.setProgressPercent(BigDecimal.ZERO);
                windowUpdate.setCurrentStage("INIT");
                windowUpdate.setDataVersion(null);
                windowUpdate.setRetryCount((window.getRetryCount() == null ? 0 : window.getRetryCount()) + 1);
                windowUpdate.setRowsRead(0L);
                windowUpdate.setRowsWritten(0L);
                windowUpdate.setErrorMessage(null);
                precomputeMapper.updateWindowStatus(windowUpdate);
            }
        }

        DiagnosisPrecomputeWindowRow firstPending = precomputeMapper.selectNextPendingWindow(DEFAULT_TENANT_ID, jobId);
        if (firstPending != null) {
            String dataVersion = "V" + System.currentTimeMillis();
            batchRunner.launch(jobId, firstPending.getWindowId(), firstPending.getPeriodStart(), firstPending.getPeriodEnd(), dataVersion, row.getRequestJson());
        }
        progressCacheService.saveFromJobRow(precomputeMapper.selectJobById(DEFAULT_TENANT_ID, jobId));
    }

    private PrecomputeJobResponse toJobResponse(DiagnosisPrecomputeJobRow row) {
        PrecomputeJobResponse response = new PrecomputeJobResponse();
        response.setJobId(row.getJobId());
        response.setJobCode(row.getJobCode());
        response.setStatus(row.getStatusCode());
        response.setPriority(row.getPriority());
        response.setModule(row.getModuleCode());
        response.setReadRangeType(row.getReadRangeType());
        response.setReadStart(row.getReadStart());
        response.setReadEnd(row.getReadEnd());
        response.setWindowTypes(row.getWindowTypes());
        response.setMaxRetry(row.getMaxRetry());
        response.setRetryCount(row.getRetryCount());
        response.setProgressPercent(row.getProgressPercent());
        response.setCurrentStage(row.getCurrentStage());
        response.setTotalWindows(row.getTotalWindows());
        response.setDoneWindows(row.getDoneWindows());
        response.setRowsRead(row.getRowsRead());
        response.setRowsWritten(row.getRowsWritten());
        response.setErrorMessage(row.getErrorMessage());
        response.setSubmittedTime(row.getSubmittedTime());
        response.setStartedTime(row.getStartedTime());
        response.setFinishedTime(row.getFinishedTime());
        return response;
    }

    private PrecomputeWindowResponse toWindowResponse(DiagnosisPrecomputeWindowRow row) {
        PrecomputeWindowResponse response = new PrecomputeWindowResponse();
        response.setWindowId(row.getWindowId());
        response.setWindowKey(row.getWindowKey());
        response.setWindowType(row.getWindowType());
        response.setPeriodStart(row.getPeriodStart());
        response.setPeriodEnd(row.getPeriodEnd());
        response.setCompareStart(row.getCompareStart());
        response.setCompareEnd(row.getCompareEnd());
        response.setStatus(row.getStatusCode());
        response.setProgressPercent(row.getProgressPercent());
        response.setCurrentStage(row.getCurrentStage());
        response.setDataVersion(row.getDataVersion());
        response.setRetryCount(row.getRetryCount());
        response.setRowsRead(row.getRowsRead());
        response.setRowsWritten(row.getRowsWritten());
        response.setErrorMessage(row.getErrorMessage());
        response.setStartedTime(row.getStartedTime());
        response.setFinishedTime(row.getFinishedTime());
        return response;
    }

    private PrecomputeJobProgressResponse toProgressResponse(DiagnosisPrecomputeJobRow row) {
        PrecomputeJobProgressResponse response = new PrecomputeJobProgressResponse();
        response.setJobId(row.getJobId());
        response.setStatus(row.getStatusCode());
        response.setProgressPercent(row.getProgressPercent());
        response.setCurrentStage(row.getCurrentStage());
        response.setWindowDone(row.getDoneWindows());
        response.setTotalWindow(row.getTotalWindows());
        response.setRowsRead(row.getRowsRead());
        response.setRowsWritten(row.getRowsWritten());
        return response;
    }

    private PrecomputeEventResponse toEventResponse(DiagnosisPrecomputeEventRow row) {
        PrecomputeEventResponse response = new PrecomputeEventResponse();
        response.setEventId(row.getEventId());
        response.setJobId(row.getJobId());
        response.setWindowId(row.getWindowId());
        response.setEventTime(row.getEventTime());
        response.setEventLevel(row.getEventLevel());
        response.setEventStage(row.getEventStage());
        response.setEventMessage(row.getEventMessage());
        response.setPayloadJson(row.getPayloadJson());
        return response;
    }
}

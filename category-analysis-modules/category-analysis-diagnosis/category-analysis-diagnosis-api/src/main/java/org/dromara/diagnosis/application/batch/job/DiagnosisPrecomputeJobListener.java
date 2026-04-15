package org.dromara.diagnosis.application.batch.job;

import lombok.RequiredArgsConstructor;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.diagnosis.api.response.PrecomputeJobProgressResponse;
import org.dromara.diagnosis.application.batch.service.DiagnosisPrecomputeBatchRunner;
import org.dromara.diagnosis.application.service.DiagnosisProgressCacheService;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeEventRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeWindowRow;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 濡澘瀚鍝ョ不濡炵偓宕查柛鏂哄墲婢х晫鎮板畝鈧ú鍐触椤掆偓濞?
 */
@Component
@RequiredArgsConstructor
public class DiagnosisPrecomputeJobListener implements JobExecutionListener {

    private final DiagnosisPrecomputeMapper precomputeMapper;

    private final DiagnosisProgressCacheService progressCacheService;

    private final ObjectProvider<DiagnosisPrecomputeBatchRunner> batchRunnerProvider;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        Long jobId = jobExecution.getJobParameters().getLong("jobId");
        Long windowId = jobExecution.getJobParameters().getLong("windowId");
        DiagnosisPrecomputeJobRow row = new DiagnosisPrecomputeJobRow();
        row.setTenantId("000000");
        row.setJobId(jobId);
        row.setStatusCode("RUNNING");
        row.setCurrentStage("READ");
        row.setOrchestratorStatus("RUNNING");
        row.setStartedTime(LocalDateTime.now());
        precomputeMapper.updateJobStatus(row);

        PrecomputeJobProgressResponse progress = new PrecomputeJobProgressResponse();
        progress.setJobId(jobId);
        progress.setStatus("RUNNING");
        progress.setProgressPercent(new BigDecimal("1"));
        progress.setCurrentStage("READ");
        DiagnosisPrecomputeJobRow latest = precomputeMapper.selectJobById("000000", jobId);
        progress.setWindowDone(latest == null || latest.getDoneWindows() == null ? 0 : latest.getDoneWindows());
        progress.setTotalWindow(latest == null || latest.getTotalWindows() == null ? 1 : latest.getTotalWindows());
        progress.setRowsRead(latest == null || latest.getRowsRead() == null ? 0L : latest.getRowsRead());
        progress.setRowsWritten(latest == null || latest.getRowsWritten() == null ? 0L : latest.getRowsWritten());
        progressCacheService.save(progress);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("jobId", jobId);
        payload.put("windowId", windowId);
        payload.put("stage", "READ");
        payload.put("status", "RUNNING");

        DiagnosisPrecomputeEventRow event = new DiagnosisPrecomputeEventRow();
        event.setTenantId("000000");
        event.setJobId(jobId);
        event.setWindowId(windowId);
        event.setEventLevel("INFO");
        event.setEventStage("START");
        event.setEventMessage("棰勮绠椾换鍔″凡鍚姩");
        event.setPayloadJson(JsonUtils.toJsonString(payload));
        precomputeMapper.insertEvent(event);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Long jobId = jobExecution.getJobParameters().getLong("jobId");
        Long windowId = jobExecution.getJobParameters().getLong("windowId");
        DiagnosisPrecomputeJobRow latestStatus = precomputeMapper.selectJobById("000000", jobId);
        if (latestStatus != null && "STOPPED".equalsIgnoreCase(latestStatus.getStatusCode())) {
            progressCacheService.saveFromJobRow(latestStatus);
            return;
        }

        DiagnosisPrecomputeJobRow row = new DiagnosisPrecomputeJobRow();
        row.setTenantId("000000");
        row.setJobId(jobId);
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            DiagnosisPrecomputeWindowRow nextWindow = precomputeMapper.selectNextPendingWindow("000000", jobId);
            if (nextWindow != null) {
                row.setStatusCode("RUNNING");
                row.setCurrentStage("DISPATCH_NEXT");
                row.setOrchestratorStatus("RUNNING");
                precomputeMapper.updateJobStatus(row);

                DiagnosisPrecomputeBatchRunner batchRunner = batchRunnerProvider.getIfAvailable();
                if (batchRunner != null) {
                    String dataVersion = "V" + System.currentTimeMillis();
                    batchRunner.launch(jobId, nextWindow.getWindowId(), nextWindow.getPeriodStart(), nextWindow.getPeriodEnd(), dataVersion, latestStatus == null ? null : latestStatus.getRequestJson());
                }

                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("jobId", jobId);
                payload.put("currentWindowId", windowId);
                payload.put("nextWindowId", nextWindow.getWindowId());
                payload.put("stage", "DISPATCH_NEXT");

                DiagnosisPrecomputeEventRow event = new DiagnosisPrecomputeEventRow();
                event.setTenantId("000000");
                event.setJobId(jobId);
                event.setWindowId(nextWindow.getWindowId());
                event.setEventLevel("INFO");
                event.setEventStage("DISPATCH_NEXT");
                event.setEventMessage("绐楀彛澶勭悊瀹屾垚锛屽凡娲惧彂涓嬩竴绐楀彛浠诲姟");
                event.setPayloadJson(JsonUtils.toJsonString(payload));
                precomputeMapper.insertEvent(event);

                DiagnosisPrecomputeJobRow latest = precomputeMapper.selectJobById("000000", jobId);
                if (latest != null) {
                    progressCacheService.saveFromJobRow(latest);
                }
                return;
            }
            row.setStatusCode("SUCCESS");
            row.setCurrentStage("DONE");
            row.setOrchestratorStatus("SUCCESS");
            row.setFinishedTime(LocalDateTime.now());

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("jobId", jobId);
            payload.put("windowId", windowId);
            payload.put("stage", "DONE");
            payload.put("status", "SUCCESS");

            DiagnosisPrecomputeEventRow event = new DiagnosisPrecomputeEventRow();
            event.setTenantId("000000");
            event.setJobId(jobId);
            event.setWindowId(windowId);
            event.setEventLevel("INFO");
            event.setEventStage("DONE");
            event.setEventMessage("预计算任务全部窗口处理完成");
            event.setPayloadJson(JsonUtils.toJsonString(payload));
            precomputeMapper.insertEvent(event);
        } else {
            String keepOrchestrator = latestStatus == null ? null : latestStatus.getOrchestratorStatus();
            row.setStatusCode("FAILED");
            row.setCurrentStage("FAILED");
            if ("PARTIAL_SUCCESS".equalsIgnoreCase(keepOrchestrator)) {
                row.setOrchestratorStatus("PARTIAL_SUCCESS");
            } else {
                row.setOrchestratorStatus("FAILED");
            }
            row.setFinishedTime(LocalDateTime.now());
            row.setErrorMessage(jobExecution.getAllFailureExceptions().isEmpty() ? "濞寸姾顕ф慨鐔稿緞鏉堫偉袝" : jobExecution.getAllFailureExceptions().get(0).getMessage());

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("jobId", jobId);
            payload.put("windowId", windowId);
            payload.put("stage", "FAILED");
            payload.put("message", row.getErrorMessage());

            DiagnosisPrecomputeEventRow event = new DiagnosisPrecomputeEventRow();
            event.setTenantId("000000");
            event.setJobId(jobId);
            event.setWindowId(windowId);
            event.setEventLevel("ERROR");
            event.setEventStage("FAILED");
            event.setEventMessage(row.getErrorMessage());
            event.setPayloadJson(JsonUtils.toJsonString(payload));
            precomputeMapper.insertEvent(event);
        }
        precomputeMapper.updateJobStatus(row);

        DiagnosisPrecomputeJobRow latest = precomputeMapper.selectJobById("000000", jobId);
        if (latest != null) {
            progressCacheService.saveFromJobRow(latest);
        }
    }
}


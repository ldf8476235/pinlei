package org.dromara.diagnosis.application.batch.job;

import lombok.RequiredArgsConstructor;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.diagnosis.api.response.PrecomputeJobProgressResponse;
import org.dromara.diagnosis.application.batch.model.DiagnosisFinalizeContext;
import org.dromara.diagnosis.application.batch.model.DiagnosisOverviewFinalizeResult;
import org.dromara.diagnosis.application.batch.model.DiagnosisSubclassFinalizeResult;
import org.dromara.diagnosis.application.batch.model.DiagnosisTrendFinalizeResult;
import org.dromara.diagnosis.application.batch.service.DiagnosisExtendedSnapshotFinalizeService;
import org.dromara.diagnosis.application.batch.service.DiagnosisFinalizeSupport;
import org.dromara.diagnosis.application.batch.service.DiagnosisOverviewFinalizeService;
import org.dromara.diagnosis.application.batch.service.DiagnosisSubclassContributionFinalizeService;
import org.dromara.diagnosis.application.batch.service.DiagnosisTrendFinalizeService;
import org.dromara.diagnosis.application.service.DiagnosisProgressCacheService;
import org.dromara.diagnosis.application.service.DiagnosisQueryHashService;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisBatchSourceMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeEventRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeWindowRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceShardParam;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Finalize stage orchestrator.
 */
@Component
@RequiredArgsConstructor
public class DiagnosisFinalizeTasklet implements Tasklet {

    private static final String TENANT_ID = "000000";

    private final DiagnosisBatchSourceMapper batchSourceMapper;
    private final DiagnosisPrecomputeMapper precomputeMapper;
    private final DiagnosisProgressCacheService progressCacheService;
    private final DiagnosisQueryHashService queryHashService;
    private final DiagnosisFinalizeSupport finalizeSupport;
    private final DiagnosisOverviewFinalizeService overviewFinalizeService;
    private final DiagnosisTrendFinalizeService trendFinalizeService;
    private final DiagnosisExtendedSnapshotFinalizeService extendedSnapshotFinalizeService;
    private final DiagnosisSubclassContributionFinalizeService subclassContributionFinalizeService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        Long jobId = chunkContext.getStepContext().getStepExecution().getJobParameters().getLong("jobId");
        Long windowId = chunkContext.getStepContext().getStepExecution().getJobParameters().getLong("windowId");
        DiagnosisPrecomputeJobRow stoppedCheck = precomputeMapper.selectJobById(TENANT_ID, jobId);
        if (stoppedCheck != null && "STOPPED".equalsIgnoreCase(stoppedCheck.getStatusCode())) {
            DiagnosisPrecomputeEventRow stopEvent = new DiagnosisPrecomputeEventRow();
            stopEvent.setTenantId(TENANT_ID);
            stopEvent.setJobId(jobId);
            stopEvent.setWindowId(windowId);
            stopEvent.setEventLevel("WARN");
            stopEvent.setEventStage("STOPPED");
            stopEvent.setEventMessage("finalize skipped because job is stopped");
            precomputeMapper.insertEvent(stopEvent);
            return RepeatStatus.FINISHED;
        }

        String periodStartText = chunkContext.getStepContext().getStepExecution().getJobParameters().getString("periodStart");
        String periodEndText = chunkContext.getStepContext().getStepExecution().getJobParameters().getString("periodEnd");
        String dataVersion = chunkContext.getStepContext().getStepExecution().getJobParameters().getString("dataVersion");
        String requestJson = chunkContext.getStepContext().getStepExecution().getJobParameters().getString("requestJson");
        LocalDate periodStart = LocalDate.parse(periodStartText);
        LocalDate periodEnd = LocalDate.parse(periodEndText);
        long periodDays = ChronoUnit.DAYS.between(periodStart, periodEnd) + 1;

        DiagnosisPrecomputeWindowRow currentWindow = precomputeMapper.selectWindowById(TENANT_ID, windowId);
        LocalDate compareStart = currentWindow == null ? null : currentWindow.getCompareStart();
        LocalDate compareEnd = currentWindow == null ? null : currentWindow.getCompareEnd();
        long compareDays = compareStart == null || compareEnd == null
            ? 0
            : ChronoUnit.DAYS.between(compareStart, compareEnd) + 1;

        DiagnosisSourceShardParam param = finalizeSupport.buildParam(periodStart, periodEnd, requestJson);
        DiagnosisSourceShardParam compareParam = null;
        if (compareStart != null && compareEnd != null) {
            compareParam = finalizeSupport.buildParam(compareStart, compareEnd, requestJson);
            long comparePeriodDays = ChronoUnit.DAYS.between(compareStart, compareEnd) + 1;
            if (comparePeriodDays != periodDays) {
                throw new IllegalStateException("compare period days must match current period");
            }
        }

        DiagnosisFinalizeContext finalizeContext = DiagnosisFinalizeContext.builder()
            .jobId(jobId)
            .windowId(windowId)
            .dataVersion(dataVersion)
            .requestJson(requestJson)
            .periodStart(periodStart)
            .periodEnd(periodEnd)
            .periodDays(periodDays)
            .compareStart(compareStart)
            .compareEnd(compareEnd)
            .compareDays(compareDays)
            .param(param)
            .compareParam(compareParam)
            .hashRequest(finalizeSupport.buildHashRequest(periodStart, periodEnd, compareStart, compareEnd, requestJson))
            .build();
        finalizeContext.setQueryHash(queryHashService.buildQueryHash(finalizeContext.getHashRequest()));

        DiagnosisOverviewFinalizeResult overviewResult = overviewFinalizeService.finalizeOverview(finalizeContext);
        DiagnosisTrendFinalizeResult trendResult = trendFinalizeService.finalizeTrends(finalizeContext);
        DiagnosisSubclassFinalizeResult subclassResult = subclassContributionFinalizeService.finalizeSubclassContribution(finalizeContext);
        extendedSnapshotFinalizeService.finalizeSnapshots(finalizeContext, overviewResult);

        Long totalRows = batchSourceMapper.countRows(param);
        long windowRowsRead = totalRows == null ? 0L : totalRows;
        long windowRowsWritten = 1L
            + trendResult.getBasicTrendRows()
            + trendResult.getCategoryPerformanceTrendRows()
            + subclassResult.getContributionRows()
            + subclassResult.getTrendRows();

        updateWindow(windowId, dataVersion, windowRowsRead, windowRowsWritten);
        DiagnosisPrecomputeJobRow job = updateJob(jobId, windowRowsRead, windowRowsWritten);
        saveProgress(jobId, job);
        insertFinalizeEvent(
            finalizeContext,
            dataVersion,
            compareStart,
            compareEnd,
            overviewResult,
            trendResult,
            subclassResult,
            windowRowsRead,
            windowRowsWritten,
            job
        );

        return RepeatStatus.FINISHED;
    }

    private void updateWindow(Long windowId, String dataVersion, long windowRowsRead, long windowRowsWritten) {
        DiagnosisPrecomputeWindowRow window = new DiagnosisPrecomputeWindowRow();
        window.setTenantId(TENANT_ID);
        window.setWindowId(windowId);
        window.setStatusCode("SUCCESS");
        window.setProgressPercent(new BigDecimal("100"));
        window.setCurrentStage("FINALIZE");
        window.setDataVersion(dataVersion);
        window.setRowsRead(windowRowsRead);
        window.setRowsWritten(windowRowsWritten);
        window.setFinishedTime(LocalDateTime.now());
        precomputeMapper.updateWindowStatus(window);
    }

    private DiagnosisPrecomputeJobRow updateJob(Long jobId, long windowRowsRead, long windowRowsWritten) {
        DiagnosisPrecomputeJobRow currentJob = precomputeMapper.selectJobById(TENANT_ID, jobId);
        int totalWindows = currentJob == null || currentJob.getTotalWindows() == null ? 1 : currentJob.getTotalWindows();
        int currentDone = currentJob == null || currentJob.getDoneWindows() == null ? 0 : currentJob.getDoneWindows();
        int nextDone = Math.min(totalWindows, currentDone + 1);
        BigDecimal progressPercent = BigDecimal.valueOf(nextDone)
            .multiply(new BigDecimal("100"))
            .divide(BigDecimal.valueOf(Math.max(1, totalWindows)), 2, RoundingMode.HALF_UP);

        long currentRowsRead = currentJob == null || currentJob.getRowsRead() == null ? 0L : currentJob.getRowsRead();
        long currentRowsWritten = currentJob == null || currentJob.getRowsWritten() == null ? 0L : currentJob.getRowsWritten();

        DiagnosisPrecomputeJobRow job = new DiagnosisPrecomputeJobRow();
        job.setTenantId(TENANT_ID);
        job.setJobId(jobId);
        job.setProgressPercent(progressPercent);
        job.setCurrentStage(nextDone >= totalWindows ? "FINALIZE" : "WINDOW_DONE");
        job.setDoneWindows(nextDone);
        job.setTotalWindows(totalWindows);
        job.setRowsRead(currentRowsRead + windowRowsRead);
        job.setRowsWritten(currentRowsWritten + windowRowsWritten);
        precomputeMapper.updateJobProgress(job);
        return job;
    }

    private void saveProgress(Long jobId, DiagnosisPrecomputeJobRow job) {
        PrecomputeJobProgressResponse progressResponse = new PrecomputeJobProgressResponse();
        progressResponse.setJobId(jobId);
        progressResponse.setStatus("RUNNING");
        progressResponse.setProgressPercent(job.getProgressPercent());
        progressResponse.setCurrentStage(job.getCurrentStage());
        progressResponse.setWindowDone(job.getDoneWindows());
        progressResponse.setTotalWindow(job.getTotalWindows());
        progressResponse.setRowsRead(job.getRowsRead());
        progressResponse.setRowsWritten(job.getRowsWritten());
        progressCacheService.save(progressResponse);
    }

    private void insertFinalizeEvent(DiagnosisFinalizeContext finalizeContext,
                                     String dataVersion,
                                     LocalDate compareStart,
                                     LocalDate compareEnd,
                                     DiagnosisOverviewFinalizeResult overviewResult,
                                     DiagnosisTrendFinalizeResult trendResult,
                                     DiagnosisSubclassFinalizeResult subclassResult,
                                     long windowRowsRead,
                                     long windowRowsWritten,
                                     DiagnosisPrecomputeJobRow job) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("queryHash", finalizeContext.getQueryHash());
        payload.put("dataVersion", dataVersion);
        payload.put("periodStart", finalizeContext.getPeriodStart());
        payload.put("periodEnd", finalizeContext.getPeriodEnd());
        payload.put("compareStart", compareStart);
        payload.put("compareEnd", compareEnd);
        payload.put("sales", overviewResult.getSales());
        payload.put("gross", overviewResult.getGross());
        payload.put("saleQuantity", overviewResult.getSaleQuantity());
        payload.put("salesCost", overviewResult.getSalesCost());
        payload.put("avgInventory", overviewResult.getAvgInventory());
        payload.put("customerCount", overviewResult.getCustomerCount());
        payload.put("customerCountTotal", overviewResult.getCustomerCountTotal());
        payload.put("categoryPerformanceTrendRows", trendResult.getCategoryPerformanceTrendRows());
        payload.put("subclassContributionRows", subclassResult.getContributionRows());
        payload.put("subclassTrendRows", subclassResult.getTrendRows());
        payload.put("windowRowsRead", windowRowsRead);
        payload.put("windowRowsWritten", windowRowsWritten);
        payload.put("jobDoneWindows", job.getDoneWindows());
        payload.put("jobTotalWindows", job.getTotalWindows());

        DiagnosisPrecomputeEventRow event = new DiagnosisPrecomputeEventRow();
        event.setTenantId(TENANT_ID);
        event.setJobId(finalizeContext.getJobId());
        event.setWindowId(finalizeContext.getWindowId());
        event.setEventLevel("INFO");
        event.setEventStage("FINALIZE");
        event.setEventMessage("finalize completed and snapshots written");
        event.setPayloadJson(JsonUtils.toJsonString(payload));
        precomputeMapper.insertEvent(event);
    }
}

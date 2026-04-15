package org.dromara.diagnosis.application.batch.job;

import lombok.RequiredArgsConstructor;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.diagnosis.api.response.PrecomputeJobProgressResponse;
import org.dromara.diagnosis.application.batch.model.DiagnosisChannelFinalizeResult;
import org.dromara.diagnosis.application.batch.model.DiagnosisAbcFinalizeResult;
import org.dromara.diagnosis.application.batch.model.DiagnosisFinalizeContext;
import org.dromara.diagnosis.application.batch.model.DiagnosisGmroiFinalizeResult;
import org.dromara.diagnosis.application.batch.model.DiagnosisGrossFinalizeResult;
import org.dromara.diagnosis.application.batch.model.DiagnosisOverviewFinalizeResult;
import org.dromara.diagnosis.application.batch.model.DiagnosisPriceBandFinalizeResult;
import org.dromara.diagnosis.application.batch.model.DiagnosisSubclassFinalizeResult;
import org.dromara.diagnosis.application.batch.model.DiagnosisTrendFinalizeResult;
import org.dromara.diagnosis.application.batch.model.DiagnosisVipFinalizeResult;
import org.dromara.diagnosis.application.batch.service.DiagnosisChannelPerformanceFinalizeService;
import org.dromara.diagnosis.application.batch.service.DiagnosisAbcStructureFinalizeService;
import org.dromara.diagnosis.application.batch.service.DiagnosisExtendedSnapshotFinalizeService;
import org.dromara.diagnosis.application.batch.service.DiagnosisFinalizeSupport;
import org.dromara.diagnosis.application.batch.service.DiagnosisGmroiContributionFinalizeService;
import org.dromara.diagnosis.application.batch.service.DiagnosisGrossContributionFinalizeService;
import org.dromara.diagnosis.application.batch.service.DiagnosisOverviewFinalizeService;
import org.dromara.diagnosis.application.batch.service.DiagnosisPriceBandFinalizeService;
import org.dromara.diagnosis.application.batch.service.DiagnosisSubclassContributionFinalizeService;
import org.dromara.diagnosis.application.batch.service.DiagnosisTrendFinalizeService;
import org.dromara.diagnosis.application.batch.service.DiagnosisVipAnalysisFinalizeService;
import org.dromara.diagnosis.application.service.DiagnosisAsyncOrchestratorService;
import org.dromara.diagnosis.application.service.DiagnosisProgressCacheService;
import org.dromara.diagnosis.application.service.DiagnosisQueryHashService;
import org.dromara.diagnosis.application.model.DiagnosisAsyncOrchestratorRequest;
import org.dromara.diagnosis.application.model.OrchestratorResult;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisBatchSourceMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisResultPublishVersionMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeEventRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeWindowRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisResultPublishVersionRow;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Finalize stage orchestrator.
 */
@Component
@RequiredArgsConstructor
public class DiagnosisFinalizeTasklet implements Tasklet {

    private static final String TENANT_ID = "000000";

    private final DiagnosisBatchSourceMapper batchSourceMapper;
    private final DiagnosisPrecomputeMapper precomputeMapper;
    private final DiagnosisResultPublishVersionMapper resultPublishVersionMapper;
    private final DiagnosisProgressCacheService progressCacheService;
    private final DiagnosisQueryHashService queryHashService;
    private final DiagnosisFinalizeSupport finalizeSupport;
    private final DiagnosisOverviewFinalizeService overviewFinalizeService;
    private final DiagnosisTrendFinalizeService trendFinalizeService;
    private final DiagnosisExtendedSnapshotFinalizeService extendedSnapshotFinalizeService;
    private final DiagnosisSubclassContributionFinalizeService subclassContributionFinalizeService;
    private final DiagnosisChannelPerformanceFinalizeService channelPerformanceFinalizeService;
    private final DiagnosisVipAnalysisFinalizeService vipAnalysisFinalizeService;
    private final DiagnosisAbcStructureFinalizeService abcStructureFinalizeService;
    private final DiagnosisGrossContributionFinalizeService grossContributionFinalizeService;
    private final DiagnosisGmroiContributionFinalizeService gmroiContributionFinalizeService;
    private final DiagnosisPriceBandFinalizeService priceBandFinalizeService;
    private final DiagnosisAsyncOrchestratorService asyncOrchestratorService;

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
        upsertPublishStatus(jobId, finalizeContext.getQueryHash(), dataVersion, "RUNNING", null);

        AtomicReference<DiagnosisOverviewFinalizeResult> overviewResultRef = new AtomicReference<>();
        AtomicReference<DiagnosisTrendFinalizeResult> trendResultRef = new AtomicReference<>();
        AtomicReference<DiagnosisSubclassFinalizeResult> subclassResultRef = new AtomicReference<>();
        AtomicReference<DiagnosisChannelFinalizeResult> channelResultRef = new AtomicReference<>();
        AtomicReference<DiagnosisVipFinalizeResult> vipResultRef = new AtomicReference<>();
        AtomicReference<DiagnosisAbcFinalizeResult> abcResultRef = new AtomicReference<>();
        AtomicReference<DiagnosisGrossFinalizeResult> grossResultRef = new AtomicReference<>();
        AtomicReference<DiagnosisGmroiFinalizeResult> gmroiResultRef = new AtomicReference<>();
        AtomicReference<DiagnosisPriceBandFinalizeResult> priceBandResultRef = new AtomicReference<>();

        Map<String, Supplier<Long>> moduleSuppliers = new LinkedHashMap<>();
        moduleSuppliers.put("overview", () -> {
            DiagnosisOverviewFinalizeResult overviewResult = overviewFinalizeService.finalizeOverview(finalizeContext);
            DiagnosisTrendFinalizeResult trendResult = trendFinalizeService.finalizeTrends(finalizeContext);
            extendedSnapshotFinalizeService.finalizeSnapshots(finalizeContext, overviewResult);
            overviewResultRef.set(overviewResult);
            trendResultRef.set(trendResult);
            return 1L + trendResult.getBasicTrendRows() + trendResult.getCategoryPerformanceTrendRows();
        });
        moduleSuppliers.put("subclass", () -> {
            DiagnosisSubclassFinalizeResult subclassResult = subclassContributionFinalizeService.finalizeSubclassContribution(finalizeContext);
            subclassResultRef.set(subclassResult);
            return subclassResult.getContributionRows() + subclassResult.getTrendRows();
        });
        moduleSuppliers.put("channel", () -> {
            DiagnosisChannelFinalizeResult channelResult = channelPerformanceFinalizeService.finalizeChannelPerformance(finalizeContext);
            channelResultRef.set(channelResult);
            return channelResult.getContributionRows() + channelResult.getTrendRows();
        });
        moduleSuppliers.put("vip", () -> {
            DiagnosisVipFinalizeResult vipResult = vipAnalysisFinalizeService.finalizeVipAnalysis(finalizeContext);
            vipResultRef.set(vipResult);
            return vipResult.getDetailRows();
        });
        moduleSuppliers.put("abc", () -> {
            DiagnosisAbcFinalizeResult abcResult = abcStructureFinalizeService.finalizeAbcStructure(finalizeContext);
            abcResultRef.set(abcResult);
            return abcResult.getParamsRows() + abcResult.getBucketRows() + abcResult.getMatrixRows() + abcResult.getSkuRows();
        });
        moduleSuppliers.put("gross", () -> {
            DiagnosisGrossFinalizeResult grossResult = grossContributionFinalizeService.finalizeGrossContribution(finalizeContext);
            grossResultRef.set(grossResult);
            return grossResult.getSkuRows();
        });
        moduleSuppliers.put("gmroi", () -> {
            DiagnosisGmroiFinalizeResult gmroiResult = gmroiContributionFinalizeService.finalizeGmroi(finalizeContext);
            gmroiResultRef.set(gmroiResult);
            return gmroiResult.getSkuRows();
        });
        moduleSuppliers.put("priceBand", () -> {
            DiagnosisPriceBandFinalizeResult priceBandResult = priceBandFinalizeService.finalizePriceBand(finalizeContext);
            priceBandResultRef.set(priceBandResult);
            return priceBandResult.getRangeRows()
                + priceBandResult.getLineRows()
                + priceBandResult.getPointRows()
                + priceBandResult.getSkuRows();
        });

        DiagnosisAsyncOrchestratorRequest orchestratorRequest = DiagnosisAsyncOrchestratorRequest.builder()
            .jobId(jobId)
            .queryHash(finalizeContext.getQueryHash())
            .dataVersion(dataVersion)
            .requestJson(requestJson)
            .periodStart(periodStart)
            .periodEnd(periodEnd)
            .compareStart(compareStart)
            .compareEnd(compareEnd)
            .build();

        try {
            CompletableFuture<OrchestratorResult> orchestratorFuture = asyncOrchestratorService.orchestrate(
                orchestratorRequest,
                moduleSuppliers,
                payload -> {
                    @SuppressWarnings("unchecked")
                    Map<String, String> statusMap = (Map<String, String>) payload.get("status");
                    @SuppressWarnings("unchecked")
                    Map<String, String> errorMap = (Map<String, String>) payload.get("errors");
                    updateModuleProgress(jobId, statusMap, errorMap);
                },
                () -> {
                    DiagnosisPrecomputeJobRow latest = precomputeMapper.selectJobById(TENANT_ID, jobId);
                    return latest != null && "STOPPED".equalsIgnoreCase(latest.getStatusCode());
                }
            );
            OrchestratorResult orchestratorResult = orchestratorFuture.join();
            if (!orchestratorResult.isSuccess()) {
                boolean anySuccess = false;
                boolean anyFailed = false;
                if (orchestratorResult.getModuleStatus() != null) {
                    for (String moduleStatus : orchestratorResult.getModuleStatus().values()) {
                        if ("SUCCESS".equalsIgnoreCase(moduleStatus)) {
                            anySuccess = true;
                        } else if ("FAILED".equalsIgnoreCase(moduleStatus)
                            || "TIMEOUT".equalsIgnoreCase(moduleStatus)
                            || "STOPPED".equalsIgnoreCase(moduleStatus)) {
                            anyFailed = true;
                        }
                    }
                }
                String publishStatus = (anySuccess && anyFailed) ? "PARTIAL_SUCCESS" : "FAILED";
                String orchestratorStatus = (anySuccess && anyFailed) ? "PARTIAL_SUCCESS" : "FAILED";
                DiagnosisPrecomputeJobRow failed = new DiagnosisPrecomputeJobRow();
                failed.setTenantId(TENANT_ID);
                failed.setJobId(jobId);
                failed.setStatusCode("FAILED");
                failed.setCurrentStage("FINALIZE_FAILED");
                failed.setOrchestratorStatus(orchestratorStatus);
                failed.setModuleProgressJson(buildModuleProgressJson(orchestratorResult.getModuleStatus(), orchestratorResult.getModuleErrors()));
                failed.setErrorMessage("orchestrator module execution failed: " + publishStatus);
                precomputeMapper.updateJobStatus(failed);
                throw new IllegalStateException("orchestrator module execution failed: " + publishStatus);
            }

            DiagnosisOverviewFinalizeResult overviewResult = overviewResultRef.get();
            DiagnosisTrendFinalizeResult trendResult = trendResultRef.get();
            DiagnosisSubclassFinalizeResult subclassResult = subclassResultRef.get();
            DiagnosisChannelFinalizeResult channelResult = channelResultRef.get();
            DiagnosisVipFinalizeResult vipResult = vipResultRef.get();
            DiagnosisAbcFinalizeResult abcResult = abcResultRef.get();
            DiagnosisGrossFinalizeResult grossResult = grossResultRef.get();
            DiagnosisGmroiFinalizeResult gmroiResult = gmroiResultRef.get();
            DiagnosisPriceBandFinalizeResult priceBandResult = priceBandResultRef.get();

            Long totalRows = batchSourceMapper.countRows(param);
            long windowRowsRead = totalRows == null ? 0L : totalRows;
            long windowRowsWritten = 0L;
            if (orchestratorResult.getModuleRows() != null) {
                for (Long value : orchestratorResult.getModuleRows().values()) {
                    windowRowsWritten += value == null ? 0L : value;
                }
            }

            updateWindow(windowId, dataVersion, windowRowsRead, windowRowsWritten);
            DiagnosisPrecomputeJobRow job = updateJob(
                jobId,
                windowRowsRead,
                windowRowsWritten,
                buildModuleProgressJson(orchestratorResult.getModuleStatus(), orchestratorResult.getModuleErrors())
            );
            saveProgress(jobId, job);
            insertFinalizeEvent(
                finalizeContext,
                dataVersion,
                compareStart,
                compareEnd,
                overviewResult,
                trendResult,
                subclassResult,
                channelResult,
                vipResult,
                abcResult,
                grossResult,
                gmroiResult,
                priceBandResult,
                windowRowsRead,
                windowRowsWritten,
                job
            );
            upsertPublishStatus(jobId, finalizeContext.getQueryHash(), dataVersion, "PUBLISHED", null);
        } catch (RuntimeException ex) {
            DiagnosisPrecomputeJobRow latest = precomputeMapper.selectJobById(TENANT_ID, jobId);
            String publishStatus = (latest != null && "PARTIAL_SUCCESS".equalsIgnoreCase(latest.getOrchestratorStatus()))
                ? "PARTIAL_SUCCESS"
                : "FAILED";
            upsertPublishStatus(jobId, finalizeContext.getQueryHash(), dataVersion, publishStatus, ex.getMessage());
            throw ex;
        }

        return RepeatStatus.FINISHED;
    }

    private void upsertPublishStatus(Long jobId,
                                     String queryHash,
                                     String dataVersion,
                                     String status,
                                     String errorSummary) {
        if (queryHash == null || dataVersion == null) {
            return;
        }
        DiagnosisResultPublishVersionRow row = new DiagnosisResultPublishVersionRow();
        row.setTenantId(TENANT_ID);
        row.setQueryHash(queryHash);
        row.setDataVersion(dataVersion);
        row.setPublishStatus(status);
        row.setJobId(jobId);
        row.setErrorSummary(errorSummary);
        if ("PUBLISHED".equalsIgnoreCase(status)) {
            row.setPublishedTime(LocalDateTime.now());
        }
        resultPublishVersionMapper.upsert(row);
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

    private DiagnosisPrecomputeJobRow updateJob(Long jobId,
                                                long windowRowsRead,
                                                long windowRowsWritten,
                                                String moduleProgressJson) {
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
        job.setModuleProgressJson(moduleProgressJson);
        precomputeMapper.updateJobProgress(job);
        job.setOrchestratorStatus(nextDone >= totalWindows ? "SUCCESS" : "RUNNING");
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
        progressResponse.setOrchestratorStatus(job.getOrchestratorStatus());
        progressResponse.setModuleProgressJson(job.getModuleProgressJson());
        progressCacheService.save(progressResponse);
    }

    private void updateModuleProgress(Long jobId, Map<String, String> statusMap, Map<String, String> errorMap) {
        DiagnosisPrecomputeJobRow current = precomputeMapper.selectJobById(TENANT_ID, jobId);
        if (current == null) {
            return;
        }
        DiagnosisPrecomputeJobRow update = new DiagnosisPrecomputeJobRow();
        update.setTenantId(TENANT_ID);
        update.setJobId(jobId);
        update.setStatusCode(current.getStatusCode());
        update.setCurrentStage(current.getCurrentStage());
        update.setOrchestratorStatus(current.getOrchestratorStatus());
        update.setModuleProgressJson(buildModuleProgressJson(statusMap, errorMap));
        precomputeMapper.updateJobStatus(update);
    }

    private String buildModuleProgressJson(Map<String, String> statusMap, Map<String, String> errorMap) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("overview", moduleNode(statusMap, errorMap, "overview"));
        payload.put("subclass", moduleNode(statusMap, errorMap, "subclass"));
        payload.put("channel", moduleNode(statusMap, errorMap, "channel"));
        payload.put("vip", moduleNode(statusMap, errorMap, "vip"));
        payload.put("abc", moduleNode(statusMap, errorMap, "abc"));
        payload.put("gross", moduleNode(statusMap, errorMap, "gross"));
        payload.put("gmroi", moduleNode(statusMap, errorMap, "gmroi"));
        payload.put("priceBand", moduleNode(statusMap, errorMap, "priceBand"));
        return JsonUtils.toJsonString(payload);
    }

    private Map<String, Object> moduleNode(Map<String, String> statusMap, Map<String, String> errorMap, String module) {
        Map<String, Object> item = new LinkedHashMap<>();
        String status = statusMap == null ? "PENDING" : statusMap.getOrDefault(module, "PENDING");
        item.put("status", status);
        item.put("done", "SUCCESS".equalsIgnoreCase(status) ? 1 : 0);
        item.put("total", 1);
        item.put("error", errorMap == null ? null : errorMap.get(module));
        return item;
    }

    private void insertFinalizeEvent(DiagnosisFinalizeContext finalizeContext,
                                     String dataVersion,
                                     LocalDate compareStart,
                                     LocalDate compareEnd,
                                     DiagnosisOverviewFinalizeResult overviewResult,
                                     DiagnosisTrendFinalizeResult trendResult,
                                     DiagnosisSubclassFinalizeResult subclassResult,
                                     DiagnosisChannelFinalizeResult channelResult,
                                     DiagnosisVipFinalizeResult vipResult,
                                     DiagnosisAbcFinalizeResult abcResult,
                                     DiagnosisGrossFinalizeResult grossResult,
                                     DiagnosisGmroiFinalizeResult gmroiResult,
                                     DiagnosisPriceBandFinalizeResult priceBandResult,
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
        payload.put("sales", overviewResult == null ? null : overviewResult.getSales());
        payload.put("gross", overviewResult == null ? null : overviewResult.getGross());
        payload.put("saleQuantity", overviewResult == null ? null : overviewResult.getSaleQuantity());
        payload.put("salesCost", overviewResult == null ? null : overviewResult.getSalesCost());
        payload.put("avgInventory", overviewResult == null ? null : overviewResult.getAvgInventory());
        payload.put("customerCount", overviewResult == null ? null : overviewResult.getCustomerCount());
        payload.put("customerCountTotal", overviewResult == null ? null : overviewResult.getCustomerCountTotal());
        payload.put("categoryPerformanceTrendRows", trendResult == null ? 0L : trendResult.getCategoryPerformanceTrendRows());
        payload.put("subclassContributionRows", subclassResult == null ? 0L : subclassResult.getContributionRows());
        payload.put("subclassTrendRows", subclassResult == null ? 0L : subclassResult.getTrendRows());
        payload.put("channelContributionRows", channelResult == null ? 0L : channelResult.getContributionRows());
        payload.put("channelTrendRows", channelResult == null ? 0L : channelResult.getTrendRows());
        payload.put("vipRows", vipResult == null ? 0L : vipResult.getDetailRows());
        payload.put("abcParamsRows", abcResult == null ? 0L : abcResult.getParamsRows());
        payload.put("abcBucketRows", abcResult == null ? 0L : abcResult.getBucketRows());
        payload.put("abcMatrixRows", abcResult == null ? 0L : abcResult.getMatrixRows());
        payload.put("abcSkuRows", abcResult == null ? 0L : abcResult.getSkuRows());
        payload.put("grossSkuRows", grossResult == null ? 0L : grossResult.getSkuRows());
        payload.put("gmroiSkuRows", gmroiResult == null ? 0L : gmroiResult.getSkuRows());
        payload.put("priceBandRangeRows", priceBandResult == null ? 0L : priceBandResult.getRangeRows());
        payload.put("priceBandLineRows", priceBandResult == null ? 0L : priceBandResult.getLineRows());
        payload.put("priceBandPointRows", priceBandResult == null ? 0L : priceBandResult.getPointRows());
        payload.put("priceBandSkuRows", priceBandResult == null ? 0L : priceBandResult.getSkuRows());
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

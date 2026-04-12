package org.dromara.diagnosis.application.batch.job;

import lombok.RequiredArgsConstructor;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.diagnosis.api.response.PrecomputeJobProgressResponse;
import org.dromara.diagnosis.application.service.DiagnosisProgressCacheService;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisBatchSourceMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisExtendedSnapshotMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.model.*;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 汇总并落快照任务.
 */
@Component
@RequiredArgsConstructor
public class DiagnosisFinalizeTasklet implements Tasklet {

    private final DiagnosisBatchSourceMapper batchSourceMapper;

    private final DiagnosisSnapshotMapper snapshotMapper;

    private final DiagnosisExtendedSnapshotMapper extendedSnapshotMapper;

    private final DiagnosisPrecomputeMapper precomputeMapper;

    private final DiagnosisProgressCacheService progressCacheService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Long jobId = chunkContext.getStepContext().getStepExecution().getJobParameters().getLong("jobId");
        Long windowId = chunkContext.getStepContext().getStepExecution().getJobParameters().getLong("windowId");
        DiagnosisPrecomputeJobRow stoppedCheck = precomputeMapper.selectJobById("000000", jobId);
        if (stoppedCheck != null && "STOPPED".equalsIgnoreCase(stoppedCheck.getStatusCode())) {
            DiagnosisPrecomputeEventRow stopEvent = new DiagnosisPrecomputeEventRow();
            stopEvent.setTenantId("000000");
            stopEvent.setJobId(jobId);
            stopEvent.setWindowId(windowId);
            stopEvent.setEventLevel("WARN");
            stopEvent.setEventStage("STOPPED");
            stopEvent.setEventMessage("任务已停止，汇总阶段跳过");
            precomputeMapper.insertEvent(stopEvent);
            return RepeatStatus.FINISHED;
        }

        String periodStartText = chunkContext.getStepContext().getStepExecution().getJobParameters().getString("periodStart");
        String periodEndText = chunkContext.getStepContext().getStepExecution().getJobParameters().getString("periodEnd");
        String dataVersion = chunkContext.getStepContext().getStepExecution().getJobParameters().getString("dataVersion");
        LocalDate periodStart = LocalDate.parse(periodStartText);
        LocalDate periodEnd = LocalDate.parse(periodEndText);
        DiagnosisPrecomputeWindowRow currentWindow = precomputeMapper.selectWindowById("000000", windowId);
        LocalDate compareStart = currentWindow == null ? null : currentWindow.getCompareStart();
        LocalDate compareEnd = currentWindow == null ? null : currentWindow.getCompareEnd();

        DiagnosisSourceShardParam param = new DiagnosisSourceShardParam();
        param.setPeriodStart(periodStart);
        param.setPeriodEnd(periodEnd);

        DiagnosisSourceOverviewAggRow agg = batchSourceMapper.aggregateOverview(param);
        if (agg == null) {
            agg = new DiagnosisSourceOverviewAggRow();
            agg.setTotalSales(BigDecimal.ZERO);
            agg.setTotalGross(BigDecimal.ZERO);
            agg.setTotalSku(0);
            agg.setActiveSku(0);
        }
        DiagnosisSourceOverviewAggRow compareAgg = null;
        if (compareStart != null && compareEnd != null) {
            DiagnosisSourceShardParam compareParam = new DiagnosisSourceShardParam();
            compareParam.setPeriodStart(compareStart);
            compareParam.setPeriodEnd(compareEnd);
            compareAgg = batchSourceMapper.aggregateOverview(compareParam);
        }

        String queryHash = md5Hex("000000|" + periodStart + "|" + periodEnd + "|" + compareStart + "|" + compareEnd);

        DiagnosisOverviewSnapshotRow overview = new DiagnosisOverviewSnapshotRow();
        overview.setTenantId("000000");
        overview.setQueryHash(queryHash);
        overview.setPeriodStart(periodStart);
        overview.setPeriodEnd(periodEnd);
        overview.setCompareStart(compareStart);
        overview.setCompareEnd(compareEnd);
        overview.setMetricTotalSales(nvl(agg.getTotalSales()));
        overview.setMetricTotalProfit(nvl(agg.getTotalGross()));
        overview.setMetricProfitMargin(calcMargin(agg.getTotalSales(), agg.getTotalGross()));
        overview.setMetricTotalSku(agg.getTotalSku() == null ? 0 : agg.getTotalSku());
        overview.setMetricActiveSku(agg.getActiveSku() == null ? 0 : agg.getActiveSku());
        overview.setMetricSalesRate(calcRate(agg.getActiveSku(), agg.getTotalSku()));
        overview.setDataVersion(dataVersion);
        overview.setSnapshotTime(LocalDateTime.now());
        snapshotMapper.upsertOverviewSnapshot(overview);

        snapshotMapper.deleteTrendsByVersion("000000", queryHash, dataVersion);
        List<DiagnosisSourceTrendAggRow> trendRows = batchSourceMapper.aggregateTrendsByDate(param);
        if (trendRows != null && !trendRows.isEmpty()) {
            Map<LocalDate, BigDecimal> compareValueMap = new HashMap<>();
            if (compareStart != null && compareEnd != null) {
                DiagnosisSourceShardParam compareParam = new DiagnosisSourceShardParam();
                compareParam.setPeriodStart(compareStart);
                compareParam.setPeriodEnd(compareEnd);
                List<DiagnosisSourceTrendAggRow> compareRows = batchSourceMapper.aggregateTrendsByDate(compareParam);
                long offsetDays = ChronoUnit.DAYS.between(compareStart, periodStart);
                if (compareRows != null) {
                    for (DiagnosisSourceTrendAggRow compareRow : compareRows) {
                        if (compareRow.getSaleDate() == null) {
                            continue;
                        }
                        LocalDate alignDate = compareRow.getSaleDate().plusDays(offsetDays);
                        compareValueMap.put(alignDate, nvl(compareRow.getTotalSales()));
                    }
                }
            }

            List<DiagnosisTrendSnapshotRow> salesTrend = trendRows.stream().map(r -> {
                DiagnosisTrendSnapshotRow row = new DiagnosisTrendSnapshotRow();
                row.setTenantId("000000");
                row.setQueryHash(queryHash);
                row.setMetricCode("sales");
                row.setPointDate(r.getSaleDate());
                row.setCurrentValue(nvl(r.getTotalSales()));
                BigDecimal compareValue = compareValueMap.get(r.getSaleDate());
                row.setCompareValue(compareValue);
                row.setGrowthRate(calcGrowthRate(compareValue, nvl(r.getTotalSales())));
                row.setPeriodLabel("CURRENT");
                row.setDataVersion(dataVersion);
                row.setSnapshotTime(LocalDateTime.now());
                return row;
            }).toList();
            snapshotMapper.batchInsertTrends(salesTrend);
        }

        extendedSnapshotMapper.deleteRoleDistributionByVersion("000000", queryHash, dataVersion);
        DiagnosisRoleDistributionSnapshotRow roleRow = new DiagnosisRoleDistributionSnapshotRow();
        roleRow.setTenantId("000000");
        roleRow.setQueryHash(queryHash);
        roleRow.setRoleCode("ALL");
        roleRow.setRoleName("全部");
        roleRow.setSalesAmount(nvl(agg.getTotalSales()));
        roleRow.setSkuCount(agg.getTotalSku() == null ? 0 : agg.getTotalSku());
        roleRow.setSalesRatio(new BigDecimal("100"));
        roleRow.setDataVersion(dataVersion);
        roleRow.setSnapshotTime(LocalDateTime.now());
        extendedSnapshotMapper.batchInsertRoleDistribution(List.of(roleRow));

        extendedSnapshotMapper.deleteInsightsByVersion("000000", queryHash, dataVersion);
        DiagnosisInsightSnapshotRow insightRow = new DiagnosisInsightSnapshotRow();
        insightRow.setTenantId("000000");
        insightRow.setQueryHash(queryHash);
        insightRow.setInsightType("SUMMARY");
        insightRow.setInsightCode("profit_margin");
        insightRow.setTitle("毛利率诊断");
        BigDecimal margin = calcMargin(agg.getTotalSales(), agg.getTotalGross());
        insightRow.setContent("当前窗口毛利率=" + margin + "%");
        insightRow.setSeverity(margin.compareTo(BigDecimal.ZERO) < 0 ? "WARN" : "INFO");
        insightRow.setSortNo(1);
        insightRow.setDataVersion(dataVersion);
        insightRow.setSnapshotTime(LocalDateTime.now());
        extendedSnapshotMapper.batchInsertInsights(List.of(insightRow));

        Long totalRows = batchSourceMapper.countRows(param);

        DiagnosisPrecomputeWindowRow window = new DiagnosisPrecomputeWindowRow();
        window.setTenantId("000000");
        window.setWindowId(windowId);
        window.setStatusCode("SUCCESS");
        window.setProgressPercent(new BigDecimal("100"));
        window.setCurrentStage("FINALIZE");
        window.setDataVersion(dataVersion);
        window.setRowsRead(totalRows == null ? 0L : totalRows);
        window.setRowsWritten(trendRows == null ? 1L : trendRows.size() + 1L);
        window.setFinishedTime(LocalDateTime.now());
        precomputeMapper.updateWindowStatus(window);

        DiagnosisPrecomputeJobRow job = new DiagnosisPrecomputeJobRow();
        DiagnosisPrecomputeJobRow currentJob = precomputeMapper.selectJobById("000000", jobId);
        int totalWindows = currentJob == null || currentJob.getTotalWindows() == null ? 1 : currentJob.getTotalWindows();
        int currentDone = currentJob == null || currentJob.getDoneWindows() == null ? 0 : currentJob.getDoneWindows();
        int nextDone = Math.min(totalWindows, currentDone + 1);
        BigDecimal progressPercent = BigDecimal.valueOf(nextDone)
            .multiply(new BigDecimal("100"))
            .divide(BigDecimal.valueOf(Math.max(1, totalWindows)), 2, RoundingMode.HALF_UP);

        job.setTenantId("000000");
        job.setJobId(jobId);
        job.setProgressPercent(progressPercent);
        job.setCurrentStage(nextDone >= totalWindows ? "FINALIZE" : "WINDOW_DONE");
        job.setDoneWindows(nextDone);
        job.setTotalWindows(totalWindows);
        long currentRowsRead = currentJob == null || currentJob.getRowsRead() == null ? 0L : currentJob.getRowsRead();
        long currentRowsWritten = currentJob == null || currentJob.getRowsWritten() == null ? 0L : currentJob.getRowsWritten();
        long windowRowsRead = window.getRowsRead() == null ? 0L : window.getRowsRead();
        long windowRowsWritten = window.getRowsWritten() == null ? 0L : window.getRowsWritten();
        job.setRowsRead(currentRowsRead + windowRowsRead);
        job.setRowsWritten(currentRowsWritten + windowRowsWritten);
        precomputeMapper.updateJobProgress(job);

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

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("queryHash", queryHash);
        payload.put("dataVersion", dataVersion);
        payload.put("periodStart", periodStart);
        payload.put("periodEnd", periodEnd);
        payload.put("compareStart", compareStart);
        payload.put("compareEnd", compareEnd);
        payload.put("compareTotalSales", compareAgg == null ? null : compareAgg.getTotalSales());
        payload.put("windowRowsRead", windowRowsRead);
        payload.put("windowRowsWritten", windowRowsWritten);
        payload.put("roleRowsWritten", 1);
        payload.put("insightRowsWritten", 1);
        payload.put("jobDoneWindows", nextDone);
        payload.put("jobTotalWindows", totalWindows);

        DiagnosisPrecomputeEventRow event = new DiagnosisPrecomputeEventRow();
        event.setTenantId("000000");
        event.setJobId(jobId);
        event.setWindowId(windowId);
        event.setEventLevel("INFO");
        event.setEventStage("FINALIZE");
        event.setEventMessage("窗口汇总完成并写入快照");
        event.setPayloadJson(JsonUtils.toJsonString(payload));
        precomputeMapper.insertEvent(event);

        return RepeatStatus.FINISHED;
    }

    private BigDecimal calcMargin(BigDecimal sales, BigDecimal gross) {
        if (sales == null || sales.compareTo(BigDecimal.ZERO) == 0 || gross == null) {
            return BigDecimal.ZERO;
        }
        return gross.multiply(new BigDecimal("100")).divide(sales, 6, RoundingMode.HALF_UP);
    }

    private BigDecimal calcRate(Integer active, Integer total) {
        if (active == null || total == null || total == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(active)
            .multiply(new BigDecimal("100"))
            .divide(BigDecimal.valueOf(total), 6, RoundingMode.HALF_UP);
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal calcGrowthRate(BigDecimal compareValue, BigDecimal currentValue) {
        if (compareValue == null || compareValue.compareTo(BigDecimal.ZERO) == 0 || currentValue == null) {
            return null;
        }
        return currentValue.subtract(compareValue)
            .multiply(new BigDecimal("100"))
            .divide(compareValue, 6, RoundingMode.HALF_UP);
    }

    private String md5Hex(String text) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] bytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format(Locale.ROOT, "%02x", b));
        }
        return sb.toString();
    }
}

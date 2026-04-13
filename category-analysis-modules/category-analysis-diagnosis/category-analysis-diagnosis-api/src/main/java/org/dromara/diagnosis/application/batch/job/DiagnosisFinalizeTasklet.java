package org.dromara.diagnosis.application.batch.job;

import cn.hutool.core.lang.Dict;
import lombok.RequiredArgsConstructor;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.diagnosis.api.request.DiagnosisSessionCreateRequest;
import org.dromara.diagnosis.api.response.PrecomputeJobProgressResponse;
import org.dromara.diagnosis.application.service.DiagnosisProgressCacheService;
import org.dromara.diagnosis.application.service.DiagnosisQueryHashService;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisBatchSourceMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisExtendedSnapshotMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisInsightSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisOverviewSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeEventRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeWindowRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisRoleDistributionSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceOverviewAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceShardParam;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceTrendAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisTrendSnapshotRow;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 汇总并落快照任务.
 */
@Component
@RequiredArgsConstructor
public class DiagnosisFinalizeTasklet implements Tasklet {

    private static final String TENANT_ID = "000000";

    private final DiagnosisBatchSourceMapper batchSourceMapper;

    private final DiagnosisSnapshotMapper snapshotMapper;

    private final DiagnosisExtendedSnapshotMapper extendedSnapshotMapper;

    private final DiagnosisPrecomputeMapper precomputeMapper;

    private final DiagnosisProgressCacheService progressCacheService;

    private final DiagnosisQueryHashService queryHashService;

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
            stopEvent.setEventMessage("任务已停止，汇总阶段跳过");
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
        long compareDays = (compareStart == null || compareEnd == null) ? 0 : (ChronoUnit.DAYS.between(compareStart, compareEnd) + 1);

        DiagnosisSourceShardParam param = new DiagnosisSourceShardParam();
        param.setPeriodStart(periodStart);
        param.setPeriodEnd(periodEnd);
        fillFilterParam(param, requestJson);

        DiagnosisSourceOverviewAggRow agg = defaultOverview(batchSourceMapper.aggregateOverview(param));
        BigDecimal avgInventory = nvl(batchSourceMapper.selectAvgInventory(param));
        BigDecimal customerCount = toDecimal(batchSourceMapper.countCustomerByClass(param));
        BigDecimal customerCountTotal = toDecimal(batchSourceMapper.countCustomerTotal(param));

        DiagnosisSourceOverviewAggRow compareAgg = null;
        BigDecimal compareAvgInventory = null;
        BigDecimal compareCustomerCount = null;
        BigDecimal compareCustomerCountTotal = null;
        if (compareStart != null && compareEnd != null) {
            DiagnosisSourceShardParam compareParam = new DiagnosisSourceShardParam();
            compareParam.setPeriodStart(compareStart);
            compareParam.setPeriodEnd(compareEnd);
            fillFilterParam(compareParam, requestJson);
            compareAgg = defaultOverview(batchSourceMapper.aggregateOverview(compareParam));
            compareAvgInventory = nvl(batchSourceMapper.selectAvgInventory(compareParam));
            compareCustomerCount = toDecimal(batchSourceMapper.countCustomerByClass(compareParam));
            compareCustomerCountTotal = toDecimal(batchSourceMapper.countCustomerTotal(compareParam));
        }

        DiagnosisSessionCreateRequest hashRequest = buildHashRequest(periodStart, periodEnd, compareStart, compareEnd, requestJson);
        String queryHash = queryHashService.buildQueryHash(hashRequest);

        BigDecimal sales = nvl(agg.getTotalSales());
        BigDecimal gross = nvl(agg.getTotalGross());
        BigDecimal saleQuantity = nvl(agg.getTotalSaleQuantity());
        BigDecimal salesCost = nvl(agg.getTotalSalesCost());
        Integer totalSku = agg.getTotalSku() == null ? 0 : agg.getTotalSku();
        Integer activeSku = agg.getActiveSku() == null ? 0 : agg.getActiveSku();

        BigDecimal customerPrice = safeDivide(sales, customerCount);
        BigDecimal customerAvgQuantity = safeDivide(saleQuantity, customerCount);
        BigDecimal pieceAvgPrice = safeDivide(sales, saleQuantity);
        BigDecimal inventorySalesRatio = safeDivide(avgInventory, sales);
        BigDecimal turnoverDays = inventorySalesRatio == null ? BigDecimal.ZERO : inventorySalesRatio.multiply(BigDecimal.valueOf(periodDays));
        BigDecimal penetrateRate = ratioPercent(customerCount, customerCountTotal);

        BigDecimal compareSales = compareAgg == null ? null : nvl(compareAgg.getTotalSales());
        BigDecimal compareGross = compareAgg == null ? null : nvl(compareAgg.getTotalGross());
        BigDecimal compareSaleQuantity = compareAgg == null ? null : nvl(compareAgg.getTotalSaleQuantity());
        BigDecimal compareSalesCost = compareAgg == null ? null : nvl(compareAgg.getTotalSalesCost());
        BigDecimal compareInventorySalesRatio = compareAgg == null ? null : safeDivide(compareAvgInventory, compareSales);
        BigDecimal compareTurnoverDays = compareInventorySalesRatio == null ? null : compareInventorySalesRatio.multiply(BigDecimal.valueOf(Math.max(1L, compareDays)));
        BigDecimal comparePenetrateRate = compareAgg == null ? null : ratioPercent(compareCustomerCount, compareCustomerCountTotal);

        DiagnosisOverviewSnapshotRow overview = new DiagnosisOverviewSnapshotRow();
        overview.setTenantId(TENANT_ID);
        overview.setQueryHash(queryHash);
        overview.setClassLevel(param.getClassLevel());
        overview.setClassNo(param.getClassNo());
        overview.setClassName(hashRequest.getClassName());
        overview.setRetailTypeId(param.getRetailTypeId());
        overview.setDeptId(toLong(param.getDeptId()));
        overview.setBusinessCircleId(param.getBusinessCircleId());
        overview.setDeptGroupId(param.getDeptGroupId());
        overview.setStoreNo(param.getStoreNo());
        overview.setPeriodStart(periodStart);
        overview.setPeriodEnd(periodEnd);
        overview.setCompareStart(compareStart);
        overview.setCompareEnd(compareEnd);
        overview.setMetricTotalSales(sales);
        overview.setMetricTotalProfit(gross);
        overview.setMetricProfitMargin(calcMargin(sales, gross));
        overview.setMetricSaleQuantity(saleQuantity);
        overview.setMetricSalesCost(salesCost);
        overview.setMetricCustomerCount(customerCount);
        overview.setMetricCustomerCountTotal(customerCountTotal);
        overview.setMetricCustomerPrice(customerPrice);
        overview.setMetricCustomerAvgQuantity(customerAvgQuantity);
        overview.setMetricPieceAvgPrice(pieceAvgPrice);
        overview.setMetricAvgInventory(avgInventory);
        overview.setMetricInventorySalesRatio(inventorySalesRatio == null ? BigDecimal.ZERO : inventorySalesRatio);
        overview.setMetricInventoryTurnoverDays(turnoverDays == null ? BigDecimal.ZERO : turnoverDays);
        overview.setMetricPenetrateRate(penetrateRate == null ? BigDecimal.ZERO : penetrateRate);
        overview.setMetricTotalSku(totalSku);
        overview.setMetricActiveSku(activeSku);
        overview.setMetricSalesRate(calcRate(activeSku, totalSku));
        overview.setMetricCompareSales(compareSales);
        overview.setMetricCompareGross(compareGross);
        overview.setMetricCompareSaleQuantity(compareSaleQuantity);
        overview.setMetricCompareSalesCost(compareSalesCost);
        overview.setMetricCompareCustomerCount(compareCustomerCount);
        overview.setMetricCompareCustomerCountTotal(compareCustomerCountTotal);
        overview.setMetricCompareAvgInventory(compareAvgInventory);
        overview.setMetricCompareInventorySalesRatio(compareInventorySalesRatio);
        overview.setMetricCompareInventoryTurnoverDays(compareTurnoverDays);
        overview.setMetricComparePenetrateRate(comparePenetrateRate);
        overview.setDataVersion(dataVersion);
        overview.setSnapshotTime(LocalDateTime.now());
        snapshotMapper.upsertOverviewSnapshot(overview);

        snapshotMapper.deleteTrendsByVersion(TENANT_ID, queryHash, dataVersion);
        List<DiagnosisSourceTrendAggRow> trendRows = batchSourceMapper.aggregateTrendsByDate(param);
        if (trendRows != null && !trendRows.isEmpty()) {
            Map<LocalDate, DiagnosisSourceTrendAggRow> compareByDate = new HashMap<>();
            if (compareStart != null && compareEnd != null) {
                DiagnosisSourceShardParam compareParam = new DiagnosisSourceShardParam();
                compareParam.setPeriodStart(compareStart);
                compareParam.setPeriodEnd(compareEnd);
                fillFilterParam(compareParam, requestJson);
                List<DiagnosisSourceTrendAggRow> compareRows = batchSourceMapper.aggregateTrendsByDate(compareParam);
                long offsetDays = ChronoUnit.DAYS.between(compareStart, periodStart);
                if (compareRows != null) {
                    for (DiagnosisSourceTrendAggRow compareRow : compareRows) {
                        if (compareRow.getSaleDate() == null) {
                            continue;
                        }
                        compareByDate.put(compareRow.getSaleDate().plusDays(offsetDays), compareRow);
                    }
                }
            }

            List<DiagnosisTrendSnapshotRow> salesTrend = trendRows.stream().map(r -> buildTrendRow(
                queryHash, dataVersion, "sales", r.getSaleDate(),
                nvl(r.getTotalSales()), compareByDate.get(r.getSaleDate()) == null ? null : nvl(compareByDate.get(r.getSaleDate()).getTotalSales())
            )).toList();

            List<DiagnosisTrendSnapshotRow> grossTrend = trendRows.stream().map(r -> buildTrendRow(
                queryHash, dataVersion, "gross", r.getSaleDate(),
                nvl(r.getTotalGross()), compareByDate.get(r.getSaleDate()) == null ? null : nvl(compareByDate.get(r.getSaleDate()).getTotalGross())
            )).toList();

            snapshotMapper.batchInsertTrends(salesTrend);
            snapshotMapper.batchInsertTrends(grossTrend);
        }

        extendedSnapshotMapper.deleteRoleDistributionByVersion(TENANT_ID, queryHash, dataVersion);
        DiagnosisRoleDistributionSnapshotRow roleRow = new DiagnosisRoleDistributionSnapshotRow();
        roleRow.setTenantId(TENANT_ID);
        roleRow.setQueryHash(queryHash);
        roleRow.setRoleCode("ALL");
        roleRow.setRoleName("全部");
        roleRow.setSalesAmount(sales);
        roleRow.setSkuCount(totalSku);
        roleRow.setSalesRatio(new BigDecimal("100"));
        roleRow.setDataVersion(dataVersion);
        roleRow.setSnapshotTime(LocalDateTime.now());
        extendedSnapshotMapper.batchInsertRoleDistribution(List.of(roleRow));

        extendedSnapshotMapper.deleteInsightsByVersion(TENANT_ID, queryHash, dataVersion);
        DiagnosisInsightSnapshotRow insightRow = new DiagnosisInsightSnapshotRow();
        insightRow.setTenantId(TENANT_ID);
        insightRow.setQueryHash(queryHash);
        insightRow.setInsightType("SUMMARY");
        insightRow.setInsightCode("profit_margin");
        insightRow.setTitle("毛利率诊断");
        BigDecimal margin = calcMargin(sales, gross);
        insightRow.setContent("当前窗口毛利率" + margin + "%");
        insightRow.setSeverity(margin.compareTo(BigDecimal.ZERO) < 0 ? "WARN" : "INFO");
        insightRow.setSortNo(1);
        insightRow.setDataVersion(dataVersion);
        insightRow.setSnapshotTime(LocalDateTime.now());
        extendedSnapshotMapper.batchInsertInsights(List.of(insightRow));

        Long totalRows = batchSourceMapper.countRows(param);

        DiagnosisPrecomputeWindowRow window = new DiagnosisPrecomputeWindowRow();
        window.setTenantId(TENANT_ID);
        window.setWindowId(windowId);
        window.setStatusCode("SUCCESS");
        window.setProgressPercent(new BigDecimal("100"));
        window.setCurrentStage("FINALIZE");
        window.setDataVersion(dataVersion);
        window.setRowsRead(totalRows == null ? 0L : totalRows);
        window.setRowsWritten(trendRows == null ? 1L : trendRows.size() * 2L + 1L);
        window.setFinishedTime(LocalDateTime.now());
        precomputeMapper.updateWindowStatus(window);

        DiagnosisPrecomputeJobRow job = new DiagnosisPrecomputeJobRow();
        DiagnosisPrecomputeJobRow currentJob = precomputeMapper.selectJobById(TENANT_ID, jobId);
        int totalWindows = currentJob == null || currentJob.getTotalWindows() == null ? 1 : currentJob.getTotalWindows();
        int currentDone = currentJob == null || currentJob.getDoneWindows() == null ? 0 : currentJob.getDoneWindows();
        int nextDone = Math.min(totalWindows, currentDone + 1);
        BigDecimal progressPercent = BigDecimal.valueOf(nextDone)
            .multiply(new BigDecimal("100"))
            .divide(BigDecimal.valueOf(Math.max(1, totalWindows)), 2, RoundingMode.HALF_UP);

        job.setTenantId(TENANT_ID);
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
        payload.put("sales", sales);
        payload.put("gross", gross);
        payload.put("saleQuantity", saleQuantity);
        payload.put("salesCost", salesCost);
        payload.put("avgInventory", avgInventory);
        payload.put("customerCount", customerCount);
        payload.put("customerCountTotal", customerCountTotal);
        payload.put("windowRowsRead", windowRowsRead);
        payload.put("windowRowsWritten", windowRowsWritten);
        payload.put("jobDoneWindows", nextDone);
        payload.put("jobTotalWindows", totalWindows);

        DiagnosisPrecomputeEventRow event = new DiagnosisPrecomputeEventRow();
        event.setTenantId(TENANT_ID);
        event.setJobId(jobId);
        event.setWindowId(windowId);
        event.setEventLevel("INFO");
        event.setEventStage("FINALIZE");
        event.setEventMessage("窗口汇总完成并写入快照");
        event.setPayloadJson(JsonUtils.toJsonString(payload));
        precomputeMapper.insertEvent(event);

        return RepeatStatus.FINISHED;
    }

    private DiagnosisTrendSnapshotRow buildTrendRow(String queryHash,
                                                    String dataVersion,
                                                    String metricCode,
                                                    LocalDate pointDate,
                                                    BigDecimal currentValue,
                                                    BigDecimal compareValue) {
        DiagnosisTrendSnapshotRow row = new DiagnosisTrendSnapshotRow();
        row.setTenantId(TENANT_ID);
        row.setQueryHash(queryHash);
        row.setMetricCode(metricCode);
        row.setPointDate(pointDate);
        row.setCurrentValue(currentValue);
        row.setCompareValue(compareValue);
        row.setGrowthRate(calcGrowthRate(compareValue, currentValue));
        row.setPeriodLabel("CURRENT");
        row.setDataVersion(dataVersion);
        row.setSnapshotTime(LocalDateTime.now());
        return row;
    }

    private DiagnosisSourceOverviewAggRow defaultOverview(DiagnosisSourceOverviewAggRow row) {
        if (row == null) {
            DiagnosisSourceOverviewAggRow d = new DiagnosisSourceOverviewAggRow();
            d.setTotalSales(BigDecimal.ZERO);
            d.setTotalGross(BigDecimal.ZERO);
            d.setTotalSaleQuantity(BigDecimal.ZERO);
            d.setTotalSalesCost(BigDecimal.ZERO);
            d.setTotalSku(0);
            d.setActiveSku(0);
            return d;
        }
        if (row.getTotalSales() == null) {
            row.setTotalSales(BigDecimal.ZERO);
        }
        if (row.getTotalGross() == null) {
            row.setTotalGross(BigDecimal.ZERO);
        }
        if (row.getTotalSaleQuantity() == null) {
            row.setTotalSaleQuantity(BigDecimal.ZERO);
        }
        if (row.getTotalSalesCost() == null) {
            row.setTotalSalesCost(BigDecimal.ZERO);
        }
        if (row.getTotalSku() == null) {
            row.setTotalSku(0);
        }
        if (row.getActiveSku() == null) {
            row.setActiveSku(0);
        }
        return row;
    }

    private void fillFilterParam(DiagnosisSourceShardParam param, String requestJson) {
        Dict map = JsonUtils.parseMap(requestJson);
        if (map == null) {
            return;
        }
        param.setClassLevel(map.getInt("classLevel"));
        param.setClassNo(trim(map.getStr("classNo")));
        param.setDeptId(trim(map.getStr("deptId")));
        param.setRetailTypeId(trim(map.getStr("retailTypeId")));
        param.setBusinessCircleId(trim(map.getStr("businessCircleId")));
        param.setDeptGroupId(trim(map.getStr("deptGroupId")));
        param.setStoreNo(trim(map.getStr("storeNo")));
    }

    private DiagnosisSessionCreateRequest buildHashRequest(LocalDate periodStart, LocalDate periodEnd,
                                                           LocalDate compareStart, LocalDate compareEnd,
                                                           String requestJson) {
        DiagnosisSessionCreateRequest request = new DiagnosisSessionCreateRequest();
        request.setPeriodStart(periodStart);
        request.setPeriodEnd(periodEnd);
        request.setCompareStart(compareStart);
        request.setCompareEnd(compareEnd);
        Dict map = JsonUtils.parseMap(requestJson);
        if (map != null) {
            request.setClassLevel(map.getInt("classLevel"));
            request.setClassNo(trim(map.getStr("classNo")));
            request.setClassName(trim(map.getStr("className")));
            request.setDeptId(trim(map.getStr("deptId")));
            request.setRetailTypeId(trim(map.getStr("retailTypeId")));
            request.setBusinessCircleId(trim(map.getStr("businessCircleId")));
            request.setDeptGroupId(trim(map.getStr("deptGroupId")));
            request.setStoreNo(trim(map.getStr("storeNo")));
            request.setExtraFilterJson(trim(map.getStr("extraFilterJson")));
        }
        return request;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }

    private Long toLong(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
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

    private BigDecimal safeDivide(BigDecimal dividend, BigDecimal divisor) {
        if (dividend == null || divisor == null || divisor.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return dividend.divide(divisor, 6, RoundingMode.HALF_UP);
    }

    private BigDecimal ratioPercent(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return numerator.multiply(new BigDecimal("100")).divide(denominator, 6, RoundingMode.HALF_UP);
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal toDecimal(Long value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(value);
    }

    private BigDecimal calcGrowthRate(BigDecimal compareValue, BigDecimal currentValue) {
        if (compareValue == null || compareValue.compareTo(BigDecimal.ZERO) == 0 || currentValue == null) {
            return null;
        }
        return currentValue.subtract(compareValue)
            .multiply(new BigDecimal("100"))
            .divide(compareValue, 6, RoundingMode.HALF_UP);
    }
}

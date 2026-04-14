package org.dromara.diagnosis.application.batch.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.application.batch.model.DiagnosisFinalizeContext;
import org.dromara.diagnosis.application.batch.model.DiagnosisTrendFinalizeResult;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisBatchSourceMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisCategoryPerformanceTrendMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisCategoryPerformanceTrendRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceDailyTrendRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceShardParam;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceTrendAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisTrendSnapshotRow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiagnosisTrendFinalizeService {

    private static final String TENANT_ID = "000000";

    private final DiagnosisBatchSourceMapper batchSourceMapper;
    private final DiagnosisSnapshotMapper snapshotMapper;
    private final DiagnosisCategoryPerformanceTrendMapper categoryPerformanceTrendMapper;
    private final DiagnosisFinalizeSupport finalizeSupport;

    public DiagnosisTrendFinalizeResult finalizeTrends(DiagnosisFinalizeContext context) {
        long basicTrendRows = finalizeBasicTrends(context);
        long categoryPerformanceTrendRows = finalizeCategoryPerformanceTrends(context);
        return DiagnosisTrendFinalizeResult.builder()
            .basicTrendRows(basicTrendRows)
            .categoryPerformanceTrendRows(categoryPerformanceTrendRows)
            .build();
    }

    private long finalizeBasicTrends(DiagnosisFinalizeContext context) {
        snapshotMapper.deleteTrendsByVersion(TENANT_ID, context.getQueryHash(), context.getDataVersion());
        List<DiagnosisSourceTrendAggRow> trendRows = batchSourceMapper.aggregateTrendsByDate(context.getParam());
        if (trendRows == null || trendRows.isEmpty()) {
            return 0L;
        }

        Map<LocalDate, DiagnosisSourceTrendAggRow> compareByDate = new HashMap<>();
        if (context.getCompareParam() != null) {
            List<DiagnosisSourceTrendAggRow> compareRows = batchSourceMapper.aggregateTrendsByDate(context.getCompareParam());
            long offsetDays = ChronoUnit.DAYS.between(context.getCompareStart(), context.getPeriodStart());
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
            context, "sales", r.getSaleDate(),
            finalizeSupport.nvl(r.getTotalSales()),
            compareByDate.get(r.getSaleDate()) == null ? null : finalizeSupport.nvl(compareByDate.get(r.getSaleDate()).getTotalSales())
        )).toList();

        List<DiagnosisTrendSnapshotRow> grossTrend = trendRows.stream().map(r -> buildTrendRow(
            context, "gross", r.getSaleDate(),
            finalizeSupport.nvl(r.getTotalGross()),
            compareByDate.get(r.getSaleDate()) == null ? null : finalizeSupport.nvl(compareByDate.get(r.getSaleDate()).getTotalGross())
        )).toList();

        snapshotMapper.batchInsertTrends(salesTrend);
        snapshotMapper.batchInsertTrends(grossTrend);
        return trendRows.size() * 2L;
    }

    private long finalizeCategoryPerformanceTrends(DiagnosisFinalizeContext context) {
        categoryPerformanceTrendMapper.deleteByVersion(TENANT_ID, context.getQueryHash(), context.getDataVersion());
        List<DiagnosisCategoryPerformanceTrendRow> rows = buildCategoryPerformanceRows(
            context, context.getParam(), context.getPeriodStart(), context.getPeriodEnd(), "1");
        if (context.getCompareParam() != null) {
            rows.addAll(buildCategoryPerformanceRows(
                context, context.getCompareParam(), context.getCompareStart(), context.getCompareEnd(), "2"));
        }
        if (!rows.isEmpty()) {
            categoryPerformanceTrendMapper.batchInsert(rows);
        }
        return rows.size();
    }

    private List<DiagnosisCategoryPerformanceTrendRow> buildCategoryPerformanceRows(DiagnosisFinalizeContext context,
                                                                                   DiagnosisSourceShardParam param,
                                                                                   LocalDate periodStart,
                                                                                   LocalDate periodEnd,
                                                                                   String periodFlag) {
        Map<LocalDate, DiagnosisSourceDailyTrendRow> salesFacts = finalizeSupport.toDailyMap(batchSourceMapper.aggregateDailySalesFacts(param));
        Map<LocalDate, DiagnosisSourceDailyTrendRow> customerCounts = finalizeSupport.toDailyMap(batchSourceMapper.aggregateDailyCustomerCounts(param));
        Map<LocalDate, DiagnosisSourceDailyTrendRow> stockCosts = finalizeSupport.toDailyMap(batchSourceMapper.aggregateDailyStockCosts(param));
        List<LocalDate> dates = finalizeSupport.enumerateDates(periodStart, periodEnd);
        LocalDateTime snapshotTime = LocalDateTime.now();

        List<DiagnosisCategoryPerformanceTrendRow> rows = new ArrayList<>(dates.size());
        for (int i = 0; i < dates.size(); i++) {
            LocalDate pointDate = dates.get(i);
            DiagnosisSourceDailyTrendRow salesRow = salesFacts.get(pointDate);
            DiagnosisSourceDailyTrendRow customerRow = customerCounts.get(pointDate);
            DiagnosisSourceDailyTrendRow stockRow = stockCosts.get(pointDate);

            BigDecimal sales = finalizeSupport.round2(finalizeSupport.nvl(salesRow == null ? null : salesRow.getTotalSales()));
            BigDecimal saleQuantity = finalizeSupport.round2(finalizeSupport.nvl(salesRow == null ? null : salesRow.getTotalSaleQuantity()));
            BigDecimal gross = finalizeSupport.round2(finalizeSupport.nvl(salesRow == null ? null : salesRow.getTotalGross()));
            BigDecimal saleCost = finalizeSupport.round2(finalizeSupport.nvl(salesRow == null ? null : salesRow.getTotalSaleCost()));
            BigDecimal customerCount = finalizeSupport.round2(finalizeSupport.toDecimal(customerRow == null ? null : customerRow.getTotalCustomerCount()));
            BigDecimal stockCost = finalizeSupport.round2(finalizeSupport.nvl(stockRow == null ? null : stockRow.getTotalStockCost()));

            DiagnosisCategoryPerformanceTrendRow row = new DiagnosisCategoryPerformanceTrendRow();
            row.setTenantId(TENANT_ID);
            row.setQueryHash(context.getQueryHash());
            row.setDataVersion(context.getDataVersion());
            row.setPeriodFlag(periodFlag);
            row.setPointIndex(i + 1);
            row.setPointDate(pointDate);
            row.setClassLevel(param.getClassLevel());
            row.setClassNo(param.getClassNo());
            row.setClassName(context.getHashRequest().getClassName());
            row.setRetailTypeId(param.getRetailTypeId());
            row.setDeptId(finalizeSupport.toLong(param.getDeptId()));
            row.setBusinessCircleId(param.getBusinessCircleId());
            row.setDeptGroupId(param.getDeptGroupId());
            row.setStoreNo(param.getStoreNo());
            row.setSales(sales);
            row.setSaleQuantity(saleQuantity);
            row.setGross(gross);
            row.setGrossRate(finalizeSupport.round2(finalizeSupport.ratioPercentOrZero(gross, sales)));
            row.setCustomerCount(customerCount);
            row.setCustomerPrice(finalizeSupport.round2(finalizeSupport.divideOrZero(sales, customerCount)));
            row.setSaleCost(saleCost);
            row.setStockCost(stockCost);
            row.setStockCostRate(finalizeSupport.round2(finalizeSupport.divideOrZero(stockCost, sales)));
            row.setSnapshotTime(snapshotTime);
            rows.add(row);
        }
        return rows;
    }

    private DiagnosisTrendSnapshotRow buildTrendRow(DiagnosisFinalizeContext context, String metricCode,
                                                    LocalDate pointDate, BigDecimal currentValue, BigDecimal compareValue) {
        DiagnosisTrendSnapshotRow row = new DiagnosisTrendSnapshotRow();
        row.setTenantId(TENANT_ID);
        row.setQueryHash(context.getQueryHash());
        row.setMetricCode(metricCode);
        row.setPointDate(pointDate);
        row.setCurrentValue(currentValue);
        row.setCompareValue(compareValue);
        row.setGrowthRate(finalizeSupport.calcGrowthRate(compareValue, currentValue));
        row.setPeriodLabel("CURRENT");
        row.setDataVersion(context.getDataVersion());
        row.setSnapshotTime(LocalDateTime.now());
        return row;
    }
}

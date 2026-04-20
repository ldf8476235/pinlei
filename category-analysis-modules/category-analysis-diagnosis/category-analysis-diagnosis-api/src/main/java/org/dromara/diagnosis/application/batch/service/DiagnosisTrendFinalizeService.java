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
        List<DiagnosisSourceDailyTrendRow> salesRows = batchSourceMapper.aggregateDailySalesFacts(context.getParam());
        if (salesRows == null || salesRows.isEmpty()) {
            return 0L;
        }

        Map<LocalDate, DiagnosisSourceDailyTrendRow> currentCustomerByDate = finalizeSupport.toDailyMap(batchSourceMapper.aggregateDailyCustomerCounts(context.getParam()));
        Map<LocalDate, DiagnosisSourceDailyTrendRow> currentStockByDate = finalizeSupport.toDailyMap(batchSourceMapper.aggregateDailyStockCosts(context.getParam()));

        Map<LocalDate, DiagnosisSourceDailyTrendRow> compareSalesByDate = new HashMap<>();
        Map<LocalDate, DiagnosisSourceDailyTrendRow> compareCustomerByDate = new HashMap<>();
        Map<LocalDate, DiagnosisSourceDailyTrendRow> compareStockByDate = new HashMap<>();
        if (context.getCompareParam() != null) {
            long offsetDays = ChronoUnit.DAYS.between(context.getCompareStart(), context.getPeriodStart());
            List<DiagnosisSourceDailyTrendRow> compareSalesRows = batchSourceMapper.aggregateDailySalesFacts(context.getCompareParam());
            List<DiagnosisSourceDailyTrendRow> compareCustomerRows = batchSourceMapper.aggregateDailyCustomerCounts(context.getCompareParam());
            List<DiagnosisSourceDailyTrendRow> compareStockRows = batchSourceMapper.aggregateDailyStockCosts(context.getCompareParam());
            if (compareSalesRows != null) {
                for (DiagnosisSourceDailyTrendRow compareRow : compareSalesRows) {
                    if (compareRow == null || compareRow.getPointDate() == null) {
                        continue;
                    }
                    compareSalesByDate.put(compareRow.getPointDate().plusDays(offsetDays), compareRow);
                }
            }
            if (compareCustomerRows != null) {
                for (DiagnosisSourceDailyTrendRow compareRow : compareCustomerRows) {
                    if (compareRow == null || compareRow.getPointDate() == null) {
                        continue;
                    }
                    compareCustomerByDate.put(compareRow.getPointDate().plusDays(offsetDays), compareRow);
                }
            }
            if (compareStockRows != null) {
                for (DiagnosisSourceDailyTrendRow compareRow : compareStockRows) {
                    if (compareRow == null || compareRow.getPointDate() == null) {
                        continue;
                    }
                    compareStockByDate.put(compareRow.getPointDate().plusDays(offsetDays), compareRow);
                }
            }
        }

        List<DiagnosisTrendSnapshotRow> salesTrend = new ArrayList<>(salesRows.size());
        List<DiagnosisTrendSnapshotRow> saleQuantityTrend = new ArrayList<>(salesRows.size());
        List<DiagnosisTrendSnapshotRow> grossTrend = new ArrayList<>(salesRows.size());
        List<DiagnosisTrendSnapshotRow> grossRateTrend = new ArrayList<>(salesRows.size());
        List<DiagnosisTrendSnapshotRow> customerCountTrend = new ArrayList<>(salesRows.size());
        List<DiagnosisTrendSnapshotRow> customerPriceTrend = new ArrayList<>(salesRows.size());
        List<DiagnosisTrendSnapshotRow> inventorySalesTrend = new ArrayList<>(salesRows.size());

        for (DiagnosisSourceDailyTrendRow currentSalesRow : salesRows) {
            if (currentSalesRow == null || currentSalesRow.getPointDate() == null) {
                continue;
            }
            LocalDate pointDate = currentSalesRow.getPointDate();
            DiagnosisSourceDailyTrendRow currentCustomerRow = currentCustomerByDate.get(pointDate);
            DiagnosisSourceDailyTrendRow currentStockRow = currentStockByDate.get(pointDate);
            DiagnosisSourceDailyTrendRow compareSalesRow = compareSalesByDate.get(pointDate);
            DiagnosisSourceDailyTrendRow compareCustomerRow = compareCustomerByDate.get(pointDate);
            DiagnosisSourceDailyTrendRow compareStockRow = compareStockByDate.get(pointDate);

            BigDecimal currentSales = finalizeSupport.round2(finalizeSupport.nvl(currentSalesRow.getTotalSales()));
            BigDecimal currentSaleQuantity = finalizeSupport.round2(finalizeSupport.nvl(currentSalesRow.getTotalSaleQuantity()));
            BigDecimal currentGross = finalizeSupport.round2(finalizeSupport.nvl(currentSalesRow.getTotalGross()));
            BigDecimal currentCustomerCount = finalizeSupport.round2(finalizeSupport.toDecimal(currentCustomerRow == null ? null : currentCustomerRow.getTotalCustomerCount()));
            BigDecimal currentStockCost = finalizeSupport.round2(finalizeSupport.nvl(currentStockRow == null ? null : currentStockRow.getTotalStockCost()));

            BigDecimal compareSales = compareSalesRow == null ? null : finalizeSupport.round2(finalizeSupport.nvl(compareSalesRow.getTotalSales()));
            BigDecimal compareSaleQuantity = compareSalesRow == null ? null : finalizeSupport.round2(finalizeSupport.nvl(compareSalesRow.getTotalSaleQuantity()));
            BigDecimal compareGross = compareSalesRow == null ? null : finalizeSupport.round2(finalizeSupport.nvl(compareSalesRow.getTotalGross()));
            BigDecimal compareCustomerCount = compareCustomerRow == null ? null : finalizeSupport.round2(finalizeSupport.toDecimal(compareCustomerRow.getTotalCustomerCount()));
            BigDecimal compareStockCost = compareStockRow == null ? null : finalizeSupport.round2(finalizeSupport.nvl(compareStockRow.getTotalStockCost()));

            salesTrend.add(buildTrendRow(context, "sales", pointDate, currentSales, compareSales));
            saleQuantityTrend.add(buildTrendRow(context, "salesQuantity", pointDate, currentSaleQuantity, compareSaleQuantity));
            grossTrend.add(buildTrendRow(context, "gross", pointDate, currentGross, compareGross));
            grossRateTrend.add(buildTrendRow(context, "grossRate", pointDate,
                finalizeSupport.round2(finalizeSupport.ratioPercentOrZero(currentGross, currentSales)),
                compareGross == null || compareSales == null ? null : finalizeSupport.round2(finalizeSupport.ratioPercentOrZero(compareGross, compareSales))));
            customerCountTrend.add(buildTrendRow(context, "customerCount", pointDate, currentCustomerCount, compareCustomerCount));
            customerPriceTrend.add(buildTrendRow(context, "customerPrice", pointDate,
                finalizeSupport.round2(finalizeSupport.divideOrZero(currentSales, currentCustomerCount)),
                compareSales == null || compareCustomerCount == null ? null : finalizeSupport.round2(finalizeSupport.divideOrZero(compareSales, compareCustomerCount))));
            inventorySalesTrend.add(buildTrendRow(context, "inventorySales", pointDate,
                finalizeSupport.round2(finalizeSupport.divideOrZero(currentStockCost, currentSales)),
                compareStockCost == null || compareSales == null ? null : finalizeSupport.round2(finalizeSupport.divideOrZero(compareStockCost, compareSales))));
        }

        snapshotMapper.batchInsertTrends(salesTrend);
        snapshotMapper.batchInsertTrends(saleQuantityTrend);
        snapshotMapper.batchInsertTrends(grossTrend);
        snapshotMapper.batchInsertTrends(grossRateTrend);
        snapshotMapper.batchInsertTrends(customerCountTrend);
        snapshotMapper.batchInsertTrends(customerPriceTrend);
        snapshotMapper.batchInsertTrends(inventorySalesTrend);
        return salesTrend.size() * 7L;
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

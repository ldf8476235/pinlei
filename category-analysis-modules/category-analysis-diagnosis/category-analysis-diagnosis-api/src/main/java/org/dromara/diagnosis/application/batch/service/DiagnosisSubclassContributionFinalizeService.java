package org.dromara.diagnosis.application.batch.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.application.batch.model.DiagnosisFinalizeContext;
import org.dromara.diagnosis.application.batch.model.DiagnosisSubclassFinalizeResult;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisBatchSourceMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSubclassContributionMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceSubclassContributionAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceSubclassDailyTrendAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSubclassContributionRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSubclassTrendRow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiagnosisSubclassContributionFinalizeService {

    private static final String TENANT_ID = "000000";

    private final DiagnosisBatchSourceMapper batchSourceMapper;
    private final DiagnosisSubclassContributionMapper subclassContributionMapper;
    private final DiagnosisFinalizeSupport finalizeSupport;

    public DiagnosisSubclassFinalizeResult finalizeSubclassContribution(DiagnosisFinalizeContext context) {
        subclassContributionMapper.deleteContributionByVersion(TENANT_ID, context.getQueryHash(), context.getDataVersion());
        subclassContributionMapper.deleteTrendByVersion(TENANT_ID, context.getQueryHash(), context.getDataVersion());

        List<DiagnosisSourceSubclassContributionAggRow> currentAggRows = safeContributionRows(
            batchSourceMapper.aggregateSubclassContribution(context.getParam()));
        List<DiagnosisSourceSubclassContributionAggRow> compareAggRows = context.getCompareParam() == null
            ? List.of()
            : safeContributionRows(batchSourceMapper.aggregateSubclassContribution(context.getCompareParam()));

        List<DiagnosisSubclassContributionRow> contributionRows = buildContributionRows(context, currentAggRows, compareAggRows);
        if (!contributionRows.isEmpty()) {
            subclassContributionMapper.batchInsertContribution(contributionRows);
        }

        List<DiagnosisSubclassTrendRow> trendRows = buildTrendRows(
            context,
            contributionRows,
            safeTrendRows(batchSourceMapper.aggregateSubclassDailySales(context.getParam()))
        );
        if (!trendRows.isEmpty()) {
            subclassContributionMapper.batchInsertTrend(trendRows);
        }

        return DiagnosisSubclassFinalizeResult.builder()
            .contributionRows(contributionRows.size())
            .trendRows(trendRows.size())
            .build();
    }

    private List<DiagnosisSubclassContributionRow> buildContributionRows(DiagnosisFinalizeContext context,
                                                                        List<DiagnosisSourceSubclassContributionAggRow> currentAggRows,
                                                                        List<DiagnosisSourceSubclassContributionAggRow> compareAggRows) {
        Map<String, DiagnosisSourceSubclassContributionAggRow> currentBySubclass = toContributionMap(currentAggRows);
        Map<String, DiagnosisSourceSubclassContributionAggRow> compareBySubclass = toContributionMap(compareAggRows);
        Map<String, DiagnosisSourceSubclassContributionAggRow> orderedUnion = new LinkedHashMap<>();
        currentAggRows.forEach(row -> orderedUnion.putIfAbsent(row.getSubClassNo(), row));
        compareAggRows.forEach(row -> orderedUnion.putIfAbsent(row.getSubClassNo(), row));

        BigDecimal currentSalesTotal = sumSales(currentAggRows);
        BigDecimal currentGrossTotal = sumGross(currentAggRows);
        BigDecimal compareSalesTotal = sumSales(compareAggRows);
        BigDecimal compareGrossTotal = sumGross(compareAggRows);
        LocalDateTime snapshotTime = LocalDateTime.now();

        List<DiagnosisSubclassContributionRow> rows = new ArrayList<>(orderedUnion.size());
        for (DiagnosisSourceSubclassContributionAggRow seed : orderedUnion.values()) {
            DiagnosisSourceSubclassContributionAggRow currentRow = currentBySubclass.get(seed.getSubClassNo());
            DiagnosisSourceSubclassContributionAggRow compareRow = compareBySubclass.get(seed.getSubClassNo());

            DiagnosisSubclassContributionRow row = new DiagnosisSubclassContributionRow();
            row.setTenantId(TENANT_ID);
            row.setQueryHash(context.getQueryHash());
            row.setDataVersion(context.getDataVersion());
            row.setParentClassLevel(context.getParam().getClassLevel());
            row.setParentClassNo(context.getParam().getClassNo());
            row.setSubClassLevel(seed.getSubClassLevel());
            row.setSubClassNo(seed.getSubClassNo());
            row.setSubClassName(seed.getSubClassName());
            row.setRetailTypeId(context.getParam().getRetailTypeId());
            row.setDeptId(finalizeSupport.toLong(context.getParam().getDeptId()));
            row.setBusinessCircleId(context.getParam().getBusinessCircleId());
            row.setDeptGroupId(context.getParam().getDeptGroupId());
            row.setStoreNo(context.getParam().getStoreNo());
            row.setPeriodStart(context.getPeriodStart());
            row.setPeriodEnd(context.getPeriodEnd());
            row.setCompareStart(context.getCompareStart());
            row.setCompareEnd(context.getCompareEnd());

            BigDecimal currentSales = money(currentRow == null ? null : currentRow.getTotalSales());
            BigDecimal currentGross = money(currentRow == null ? null : currentRow.getTotalGross());
            BigDecimal currentSaleQuantity = money(currentRow == null ? null : currentRow.getTotalSaleQuantity());
            BigDecimal currentSaleCost = money(currentRow == null ? null : currentRow.getTotalSaleCost());
            BigDecimal currentCustomerCount = count(currentRow == null ? null : currentRow.getTotalCustomerCount());
            BigDecimal currentAvgInventory = money(currentRow == null ? null : currentRow.getAvgInventory());
            Integer currentTotalSku = currentRow == null || currentRow.getTotalSku() == null ? 0 : currentRow.getTotalSku();
            Integer currentActiveSku = currentRow == null || currentRow.getActiveSku() == null ? 0 : currentRow.getActiveSku();

            BigDecimal compareSales = money(compareRow == null ? null : compareRow.getTotalSales());
            BigDecimal compareGross = money(compareRow == null ? null : compareRow.getTotalGross());
            BigDecimal compareSaleQuantity = money(compareRow == null ? null : compareRow.getTotalSaleQuantity());
            BigDecimal compareCustomerCount = count(compareRow == null ? null : compareRow.getTotalCustomerCount());

            row.setCurrentSales(currentSales);
            row.setCurrentSalesPer(percent(currentSales, currentSalesTotal));
            row.setCurrentGross(currentGross);
            row.setCurrentGrossPer(percent(currentGross, currentGrossTotal));
            row.setCurrentGrossRate(percent(currentGross, currentSales));
            row.setCurrentSaleQuantity(currentSaleQuantity);
            row.setCurrentCustomerCount(currentCustomerCount);
            row.setCurrentCustomerPrice(rate(currentSales, currentCustomerCount));
            row.setCurrentAvgInventory(currentAvgInventory);
            row.setCurrentTurnoverRate(finalizeSupport.round2(finalizeSupport.calcRate(currentActiveSku, currentTotalSku)));
            row.setCurrentTurnoverDays(turnoverDays(currentAvgInventory, currentSales, context.getPeriodDays()));
            row.setCurrentGmroi(rate(currentGross, currentAvgInventory));
            row.setCurrentSaleCost(currentSaleCost);
            row.setCurrentTotalSku(currentTotalSku);
            row.setCurrentActiveSku(currentActiveSku);

            row.setCompareSales(compareSales);
            row.setCompareSalesPer(percent(compareSales, compareSalesTotal));
            row.setCompareSalesAddRate(growthRate(compareSales, currentSales));
            row.setCompareGross(compareGross);
            row.setCompareGrossPer(percent(compareGross, compareGrossTotal));
            row.setCompareGrossAddRate(growthRate(compareGross, currentGross));
            row.setCompareGrossRate(percent(compareGross, compareSales));
            row.setCompareSaleQuantity(compareSaleQuantity);
            row.setCompareSaleQuantityAddRate(growthRate(compareSaleQuantity, currentSaleQuantity));
            row.setCompareCustomerCount(compareCustomerCount);
            row.setCompareCustomerPrice(rate(compareSales, compareCustomerCount));
            row.setCompareCustomerPriceAddRate(growthRate(rate(compareSales, compareCustomerCount), rate(currentSales, currentCustomerCount)));
            row.setSnapshotTime(snapshotTime);
            rows.add(row);
        }
        return rows;
    }

    private List<DiagnosisSubclassTrendRow> buildTrendRows(DiagnosisFinalizeContext context,
                                                           List<DiagnosisSubclassContributionRow> contributionRows,
                                                           List<DiagnosisSourceSubclassDailyTrendAggRow> currentTrendAggRows) {
        if (contributionRows.isEmpty()) {
            return List.of();
        }

        Map<String, Map<LocalDate, DiagnosisSourceSubclassDailyTrendAggRow>> trendMap = new HashMap<>();
        for (DiagnosisSourceSubclassDailyTrendAggRow row : currentTrendAggRows) {
            if (row.getSubClassNo() == null || row.getPointDate() == null) {
                continue;
            }
            trendMap.computeIfAbsent(row.getSubClassNo(), key -> new HashMap<>()).put(row.getPointDate(), row);
        }

        List<LocalDate> dates = finalizeSupport.enumerateDates(context.getPeriodStart(), context.getPeriodEnd());
        LocalDateTime snapshotTime = LocalDateTime.now();
        List<DiagnosisSubclassTrendRow> trendRows = new ArrayList<>(contributionRows.size() * dates.size());

        for (DiagnosisSubclassContributionRow contributionRow : contributionRows) {
            Map<LocalDate, DiagnosisSourceSubclassDailyTrendAggRow> dailyMap =
                trendMap.getOrDefault(contributionRow.getSubClassNo(), Map.of());
            for (int i = 0; i < dates.size(); i++) {
                LocalDate pointDate = dates.get(i);
                DiagnosisSourceSubclassDailyTrendAggRow dailyRow = dailyMap.get(pointDate);

                DiagnosisSubclassTrendRow row = new DiagnosisSubclassTrendRow();
                row.setTenantId(TENANT_ID);
                row.setQueryHash(context.getQueryHash());
                row.setDataVersion(context.getDataVersion());
                row.setParentClassLevel(contributionRow.getParentClassLevel());
                row.setParentClassNo(contributionRow.getParentClassNo());
                row.setSubClassLevel(contributionRow.getSubClassLevel());
                row.setSubClassNo(contributionRow.getSubClassNo());
                row.setSubClassName(contributionRow.getSubClassName());
                row.setRetailTypeId(contributionRow.getRetailTypeId());
                row.setDeptId(contributionRow.getDeptId());
                row.setBusinessCircleId(contributionRow.getBusinessCircleId());
                row.setDeptGroupId(contributionRow.getDeptGroupId());
                row.setStoreNo(contributionRow.getStoreNo());
                row.setPointIndex(i + 1);
                row.setPointDate(pointDate);
                row.setSales(money(dailyRow == null ? null : dailyRow.getTotalSales()));
                row.setSnapshotTime(snapshotTime);
                trendRows.add(row);
            }
        }
        return trendRows;
    }

    private Map<String, DiagnosisSourceSubclassContributionAggRow> toContributionMap(List<DiagnosisSourceSubclassContributionAggRow> rows) {
        Map<String, DiagnosisSourceSubclassContributionAggRow> map = new LinkedHashMap<>();
        for (DiagnosisSourceSubclassContributionAggRow row : rows) {
            if (row.getSubClassNo() == null) {
                continue;
            }
            map.put(row.getSubClassNo(), row);
        }
        return map;
    }

    private List<DiagnosisSourceSubclassContributionAggRow> safeContributionRows(List<DiagnosisSourceSubclassContributionAggRow> rows) {
        return rows == null ? List.of() : rows;
    }

    private List<DiagnosisSourceSubclassDailyTrendAggRow> safeTrendRows(List<DiagnosisSourceSubclassDailyTrendAggRow> rows) {
        return rows == null ? List.of() : rows;
    }

    private BigDecimal sumSales(List<DiagnosisSourceSubclassContributionAggRow> rows) {
        BigDecimal sum = BigDecimal.ZERO;
        for (DiagnosisSourceSubclassContributionAggRow row : rows) {
            sum = sum.add(finalizeSupport.nvl(row.getTotalSales()));
        }
        return sum;
    }

    private BigDecimal sumGross(List<DiagnosisSourceSubclassContributionAggRow> rows) {
        BigDecimal sum = BigDecimal.ZERO;
        for (DiagnosisSourceSubclassContributionAggRow row : rows) {
            sum = sum.add(finalizeSupport.nvl(row.getTotalGross()));
        }
        return sum;
    }

    private BigDecimal money(BigDecimal value) {
        return finalizeSupport.round2(finalizeSupport.nvl(value));
    }

    private BigDecimal count(Long value) {
        return finalizeSupport.toDecimal(value);
    }

    private BigDecimal percent(BigDecimal numerator, BigDecimal denominator) {
        return finalizeSupport.round2(finalizeSupport.ratioPercentOrZero(
            finalizeSupport.nvl(numerator), finalizeSupport.nvl(denominator)));
    }

    private BigDecimal rate(BigDecimal numerator, BigDecimal denominator) {
        return finalizeSupport.round2(finalizeSupport.divideOrZero(
            finalizeSupport.nvl(numerator), finalizeSupport.nvl(denominator)));
    }

    private BigDecimal turnoverDays(BigDecimal avgInventory, BigDecimal sales, long periodDays) {
        BigDecimal inventorySalesRatio = finalizeSupport.divideOrZero(
            finalizeSupport.nvl(avgInventory), finalizeSupport.nvl(sales));
        return finalizeSupport.round2(inventorySalesRatio.multiply(BigDecimal.valueOf(periodDays)));
    }

    private BigDecimal growthRate(BigDecimal compareValue, BigDecimal currentValue) {
        return finalizeSupport.round2(finalizeSupport.nvl(finalizeSupport.calcGrowthRate(
            finalizeSupport.nvl(compareValue), finalizeSupport.nvl(currentValue))));
    }
}

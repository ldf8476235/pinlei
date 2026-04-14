package org.dromara.diagnosis.application.batch.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.application.batch.model.DiagnosisFinalizeContext;
import org.dromara.diagnosis.application.batch.model.DiagnosisOverviewFinalizeResult;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisBatchSourceMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisOverviewSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceOverviewAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceShardParam;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DiagnosisOverviewFinalizeService {

    private static final String TENANT_ID = "000000";

    private final DiagnosisBatchSourceMapper batchSourceMapper;
    private final DiagnosisSnapshotMapper snapshotMapper;
    private final DiagnosisFinalizeSupport finalizeSupport;

    public DiagnosisOverviewFinalizeResult finalizeOverview(DiagnosisFinalizeContext context) {
        DiagnosisSourceShardParam param = context.getParam();
        DiagnosisSourceOverviewAggRow agg = finalizeSupport.defaultOverview(batchSourceMapper.aggregateOverview(param));
        BigDecimal avgInventory = finalizeSupport.nvl(batchSourceMapper.selectAvgInventory(param));
        BigDecimal customerCount = finalizeSupport.toDecimal(batchSourceMapper.countCustomerByClass(param));
        BigDecimal customerCountTotal = finalizeSupport.toDecimal(batchSourceMapper.countCustomerTotal(param));

        DiagnosisSourceOverviewAggRow compareAgg = null;
        BigDecimal compareAvgInventory = null;
        BigDecimal compareCustomerCount = null;
        BigDecimal compareCustomerCountTotal = null;
        if (context.getCompareParam() != null) {
            compareAgg = finalizeSupport.defaultOverview(batchSourceMapper.aggregateOverview(context.getCompareParam()));
            compareAvgInventory = finalizeSupport.nvl(batchSourceMapper.selectAvgInventory(context.getCompareParam()));
            compareCustomerCount = finalizeSupport.toDecimal(batchSourceMapper.countCustomerByClass(context.getCompareParam()));
            compareCustomerCountTotal = finalizeSupport.toDecimal(batchSourceMapper.countCustomerTotal(context.getCompareParam()));
        }

        BigDecimal sales = finalizeSupport.nvl(agg.getTotalSales());
        BigDecimal gross = finalizeSupport.nvl(agg.getTotalGross());
        BigDecimal saleQuantity = finalizeSupport.nvl(agg.getTotalSaleQuantity());
        BigDecimal salesCost = finalizeSupport.nvl(agg.getTotalSalesCost());
        Integer totalSku = agg.getTotalSku() == null ? 0 : agg.getTotalSku();
        Integer activeSku = agg.getActiveSku() == null ? 0 : agg.getActiveSku();

        BigDecimal customerPrice = finalizeSupport.safeDivide(sales, customerCount);
        BigDecimal customerAvgQuantity = finalizeSupport.safeDivide(saleQuantity, customerCount);
        BigDecimal pieceAvgPrice = finalizeSupport.safeDivide(sales, saleQuantity);
        BigDecimal inventorySalesRatio = finalizeSupport.safeDivide(avgInventory, sales);
        BigDecimal turnoverDays = inventorySalesRatio == null ? BigDecimal.ZERO
            : inventorySalesRatio.multiply(BigDecimal.valueOf(context.getPeriodDays()));
        BigDecimal penetrateRate = finalizeSupport.ratioPercent(customerCount, customerCountTotal);

        BigDecimal compareSales = compareAgg == null ? null : finalizeSupport.nvl(compareAgg.getTotalSales());
        BigDecimal compareGross = compareAgg == null ? null : finalizeSupport.nvl(compareAgg.getTotalGross());
        BigDecimal compareSaleQuantity = compareAgg == null ? null : finalizeSupport.nvl(compareAgg.getTotalSaleQuantity());
        BigDecimal compareSalesCost = compareAgg == null ? null : finalizeSupport.nvl(compareAgg.getTotalSalesCost());
        Integer compareTotalSku = compareAgg == null || compareAgg.getTotalSku() == null ? null : compareAgg.getTotalSku();
        BigDecimal compareInventorySalesRatio = compareAgg == null ? null : finalizeSupport.safeDivide(compareAvgInventory, compareSales);
        BigDecimal compareTurnoverDays = compareInventorySalesRatio == null ? null
            : compareInventorySalesRatio.multiply(BigDecimal.valueOf(Math.max(1L, context.getCompareDays())));
        BigDecimal comparePenetrateRate = compareAgg == null ? null
            : finalizeSupport.ratioPercent(compareCustomerCount, compareCustomerCountTotal);
        BigDecimal compareSalesRate = compareAgg == null ? null
            : finalizeSupport.calcRate(compareAgg.getActiveSku(), compareAgg.getTotalSku());

        DiagnosisOverviewSnapshotRow overview = new DiagnosisOverviewSnapshotRow();
        overview.setTenantId(TENANT_ID);
        overview.setQueryHash(context.getQueryHash());
        overview.setClassLevel(param.getClassLevel());
        overview.setClassNo(param.getClassNo());
        overview.setClassName(context.getHashRequest().getClassName());
        overview.setRetailTypeId(param.getRetailTypeId());
        overview.setDeptId(finalizeSupport.toLong(param.getDeptId()));
        overview.setBusinessCircleId(param.getBusinessCircleId());
        overview.setDeptGroupId(param.getDeptGroupId());
        overview.setStoreNo(param.getStoreNo());
        overview.setPeriodStart(context.getPeriodStart());
        overview.setPeriodEnd(context.getPeriodEnd());
        overview.setCompareStart(context.getCompareStart());
        overview.setCompareEnd(context.getCompareEnd());
        overview.setMetricTotalSales(sales);
        overview.setMetricTotalProfit(gross);
        overview.setMetricProfitMargin(finalizeSupport.calcMargin(sales, gross));
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
        overview.setMetricSalesRate(finalizeSupport.calcRate(activeSku, totalSku));
        overview.setMetricCompareTotalSku(compareTotalSku);
        overview.setMetricCompareSalesRate(compareSalesRate);
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
        overview.setDataVersion(context.getDataVersion());
        overview.setSnapshotTime(LocalDateTime.now());
        snapshotMapper.upsertOverviewSnapshot(overview);

        return DiagnosisOverviewFinalizeResult.builder()
            .overview(overview)
            .sales(sales)
            .gross(gross)
            .saleQuantity(saleQuantity)
            .salesCost(salesCost)
            .avgInventory(avgInventory)
            .customerCount(customerCount)
            .customerCountTotal(customerCountTotal)
            .totalSku(totalSku)
            .activeSku(activeSku)
            .build();
    }
}

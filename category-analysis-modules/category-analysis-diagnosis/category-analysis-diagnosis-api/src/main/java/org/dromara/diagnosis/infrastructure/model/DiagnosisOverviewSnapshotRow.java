package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 诊断概览快照行模型.
 */
@Data
public class DiagnosisOverviewSnapshotRow {

    private Long id;

    private String tenantId;

    private String queryHash;

    private Integer classLevel;

    private String classNo;

    private String className;

    private String retailTypeId;

    private Long deptId;

    private String businessCircleId;

    private String deptGroupId;

    private String storeNo;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private LocalDate compareStart;

    private LocalDate compareEnd;

    private BigDecimal metricTotalSales;

    private BigDecimal metricTotalProfit;

    private BigDecimal metricProfitMargin;

    private BigDecimal metricSaleQuantity;

    private BigDecimal metricSalesCost;

    private BigDecimal metricCustomerCount;

    private BigDecimal metricCustomerCountTotal;

    private BigDecimal metricCustomerPrice;

    private BigDecimal metricCustomerAvgQuantity;

    private BigDecimal metricPieceAvgPrice;

    private BigDecimal metricAvgInventory;

    private BigDecimal metricInventorySalesRatio;

    private BigDecimal metricInventoryTurnoverDays;

    private BigDecimal metricPenetrateRate;

    private Integer metricTotalSku;

    private Integer metricActiveSku;

    private BigDecimal metricSalesRate;

    private Integer metricCompareTotalSku;

    private BigDecimal metricCompareSalesRate;

    private BigDecimal metricCompareSales;

    private BigDecimal metricCompareGross;

    private BigDecimal metricCompareSaleQuantity;

    private BigDecimal metricCompareSalesCost;

    private BigDecimal metricCompareCustomerCount;

    private BigDecimal metricCompareCustomerCountTotal;

    private BigDecimal metricCompareAvgInventory;

    private BigDecimal metricCompareInventorySalesRatio;

    private BigDecimal metricCompareInventoryTurnoverDays;

    private BigDecimal metricComparePenetrateRate;

    private String dataVersion;

    private LocalDateTime snapshotTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 诊断概览响应.
 */
@Data
public class DiagnosisOverviewResponse {

    /**
     * 兼容旧接口字段：/salesStoreClass/classPerformance
     */
    private String classNo;

    private String className;

    private Integer currentClassSku;

    private Integer compareClassSku;

    private BigDecimal comparativeGrowthRate;

    private BigDecimal currentTurnoverRate;

    private BigDecimal compareTurnoverRate;

    private BigDecimal comparativeTurnoverRate;

    private BigDecimal currentPenetrateRate;

    private BigDecimal comparePenetrateRate;

    private BigDecimal comparativePenetrateRate;

    private BigDecimal currentTurnoverDays;

    private BigDecimal compareTurnoverDays;

    private BigDecimal comparativeTurnoverDays;

    private BigDecimal currentInventorySales;

    private BigDecimal compareInventorySales;

    private BigDecimal comparativeInventorySales;

    private BigDecimal currentAvgInventory;

    private BigDecimal compareAvgInventory;

    private BigDecimal comparativeAvgInventory;

    private BigDecimal currentSaleQuantity;

    private BigDecimal compareSaleQuantity;

    private BigDecimal comparativeSaleQuantity;

    private BigDecimal currentSales;

    private BigDecimal compareSales;

    private BigDecimal comparativeSales;

    private BigDecimal currentGross;

    private BigDecimal compareGross;

    private BigDecimal comparativeGross;

    private BigDecimal currentGrossRate;

    private BigDecimal compareGrossRate;

    private BigDecimal comparativeGrossRate;

    private BigDecimal currentCustomerCount;

    private BigDecimal compareCustomerCount;

    private BigDecimal comparativeCustomerCount;

    private BigDecimal currentCustomerPrice;

    private BigDecimal compareCustomerPrice;

    private BigDecimal comparativeCustomerPrice;

    private BigDecimal currentCustomerAvgQuantity;

    private BigDecimal compareCustomerAvgQuantity;

    private BigDecimal comparativeCustomerAvgQuantity;

    private BigDecimal currentPieceAvgPrice;

    private BigDecimal comparePieceAvgPrice;

    private BigDecimal comparativePieceAvgPrice;

    private BigDecimal currentSalesCost;

    private BigDecimal compareSalesCost;

    private BigDecimal currentCustomerCountTotal;

    private BigDecimal compareCustomerCountTotal;
}

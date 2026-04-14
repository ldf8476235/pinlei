package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Raw aggregation row for subclass contribution summary.
 */
@Data
public class DiagnosisSourceSubclassContributionAggRow {

    private Integer subClassLevel;
    private String subClassNo;
    private String subClassName;
    private BigDecimal totalSales;
    private BigDecimal totalGross;
    private BigDecimal totalSaleQuantity;
    private BigDecimal totalSaleCost;
    private Long totalCustomerCount;
    private BigDecimal avgInventory;
    private Integer totalSku;
    private Integer activeSku;
}

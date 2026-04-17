package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiagnosisSourceBrandAggRow {

    private String brandNo;
    private String productBrand;
    private String brandType;
    private String brandTypeName;
    private Integer skuCount;
    private Integer activeStoreCount;
    private BigDecimal saleQuantity;
    private BigDecimal sales;
    private BigDecimal gross;
    private BigDecimal saleCost;
    private Integer activitySku;
}

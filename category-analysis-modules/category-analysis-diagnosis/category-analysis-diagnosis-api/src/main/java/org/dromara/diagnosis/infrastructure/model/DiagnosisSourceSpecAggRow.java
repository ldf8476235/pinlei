package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiagnosisSourceSpecAggRow {

    private String specNo;
    private String specName;
    private String specType;
    private String specTypeName;
    private Integer skuCount;
    private Integer activeStoreCount;
    private BigDecimal saleQuantity;
    private BigDecimal sales;
    private BigDecimal gross;
    private BigDecimal saleCost;
    private Integer activitySku;
}

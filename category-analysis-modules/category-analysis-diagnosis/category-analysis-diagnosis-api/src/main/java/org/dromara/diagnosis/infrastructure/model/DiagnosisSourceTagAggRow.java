package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiagnosisSourceTagAggRow {

    private String tagType;
    private String tagTypeName;
    private String tagNo;
    private String tagName;
    private String metricKey;
    private Boolean isUntagged;
    private Integer skuCount;
    private Integer activeStoreCount;
    private BigDecimal saleQuantity;
    private BigDecimal sales;
    private BigDecimal gross;
    private BigDecimal saleCost;
    private Integer activitySku;
}

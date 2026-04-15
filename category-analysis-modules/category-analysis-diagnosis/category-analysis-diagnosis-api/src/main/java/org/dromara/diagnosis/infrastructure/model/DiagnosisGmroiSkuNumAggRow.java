package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiagnosisGmroiSkuNumAggRow {

    private Integer sku1;
    private Integer sku2;
    private Integer sku3;
    private Integer sku4;
    private BigDecimal skuPer1;
    private BigDecimal skuPer2;
    private BigDecimal skuPer3;
    private BigDecimal skuPer4;
}


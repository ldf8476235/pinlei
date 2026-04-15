package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiagnosisGmroiSkuPerAggRow {

    private BigDecimal currentSkuPer1;
    private BigDecimal currentSkuPer2;
    private BigDecimal currentSkuPer3;
    private BigDecimal currentSkuPer4;
    private Integer currentSku3;
    private BigDecimal compareSkuPer1;
    private BigDecimal compareSkuPer2;
    private BigDecimal compareSkuPer3;
    private BigDecimal compareSkuPer4;
}


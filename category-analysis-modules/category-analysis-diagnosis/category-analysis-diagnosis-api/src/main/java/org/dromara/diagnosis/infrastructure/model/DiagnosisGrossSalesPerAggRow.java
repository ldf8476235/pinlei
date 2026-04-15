package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiagnosisGrossSalesPerAggRow {

    private BigDecimal currentSalesPer1;
    private BigDecimal currentSalesPer2;
    private BigDecimal currentSalesPer3;
    private BigDecimal currentSalesPer4;
    private BigDecimal compareSalesPer1;
    private BigDecimal compareSalesPer2;
    private BigDecimal compareSalesPer3;
    private BigDecimal compareSalesPer4;
}


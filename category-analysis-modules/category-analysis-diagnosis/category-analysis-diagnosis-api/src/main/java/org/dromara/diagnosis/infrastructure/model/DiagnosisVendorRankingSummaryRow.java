package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiagnosisVendorRankingSummaryRow {

    private BigDecimal ave;
    private BigDecimal maxData;
    private BigDecimal minData;
    private Long total;
}

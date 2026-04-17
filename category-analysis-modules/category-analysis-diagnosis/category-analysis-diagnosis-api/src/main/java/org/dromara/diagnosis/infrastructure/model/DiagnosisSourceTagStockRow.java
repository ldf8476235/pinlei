package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiagnosisSourceTagStockRow {

    private String metricKey;
    private BigDecimal avgStockQuantity;
}

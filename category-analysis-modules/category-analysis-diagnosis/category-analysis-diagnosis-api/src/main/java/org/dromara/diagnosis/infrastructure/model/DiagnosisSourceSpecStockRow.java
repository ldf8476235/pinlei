package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiagnosisSourceSpecStockRow {

    private String specNo;
    private BigDecimal avgStockQuantity;
}

package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiagnosisSourceBrandStockRow {

    private String brandNo;
    private BigDecimal avgStockQuantity;
}

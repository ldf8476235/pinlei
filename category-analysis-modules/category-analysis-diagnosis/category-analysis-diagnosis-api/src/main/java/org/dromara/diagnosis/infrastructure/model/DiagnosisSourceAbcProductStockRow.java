package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiagnosisSourceAbcProductStockRow {

    private String productNo;
    private BigDecimal stockQuantity;
    private BigDecimal stockSaleMoney;
}

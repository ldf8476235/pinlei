package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiagnosisBrandRankingItemRow {

    private String brandNo;
    private String productBrand;
    private BigDecimal data;
}

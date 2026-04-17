package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiagnosisSpecRankingItemRow {

    private String specNo;
    private String specName;
    private BigDecimal data;
}

package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiagnosisVendorRankingItemRow {

    private String productVendorNo;
    private String productVendorName;
    private BigDecimal data;
}

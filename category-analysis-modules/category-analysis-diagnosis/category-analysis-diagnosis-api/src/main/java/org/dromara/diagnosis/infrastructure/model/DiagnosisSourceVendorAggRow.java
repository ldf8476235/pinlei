package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiagnosisSourceVendorAggRow {

    private String vendorNo;
    private String vendorName;
    private String statusNo;
    private String statusName;
    private Integer skuCount;
    private Integer newSkuCount;
    private BigDecimal sales;
    private BigDecimal gross;
    private BigDecimal oiAmount;
    private BigDecimal netOrderAmount;
    private BigDecimal diffOrderAmount;
    private BigDecimal baseOrderAmount;
}

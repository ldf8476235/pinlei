package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiagnosisSourceVipGenderAgeAggRow {

    private Integer gender;
    private Integer ageValue;
    private BigDecimal totalSales;
    private BigDecimal totalSaleQuantity;
    private BigDecimal totalCustomerCount;
}

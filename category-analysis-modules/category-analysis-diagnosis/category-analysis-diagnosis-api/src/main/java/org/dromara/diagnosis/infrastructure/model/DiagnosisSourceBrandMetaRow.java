package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DiagnosisSourceBrandMetaRow {

    private String brandNo;
    private String productBrand;
    private String brandType;
    private String brandTypeName;
    private String ownBrandNo;
    private String ownBrandName;
    private LocalDate firstSaleDate;
}

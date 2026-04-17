package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DiagnosisSourceSpecMetaRow {

    private String specNo;
    private String specName;
    private String specType;
    private String specTypeName;
    private LocalDate firstSaleDate;
}

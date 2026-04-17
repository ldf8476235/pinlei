package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DiagnosisSourceTagMetaRow {

    private String tagType;
    private String tagTypeName;
    private String tagNo;
    private String tagName;
    private String metricKey;
    private Boolean isUntagged;
    private LocalDate firstSaleDate;
}

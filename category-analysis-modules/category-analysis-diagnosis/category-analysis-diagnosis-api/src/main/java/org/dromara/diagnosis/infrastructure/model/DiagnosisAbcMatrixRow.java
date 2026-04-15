package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

@Data
public class DiagnosisAbcMatrixRow {

    private Long id;
    private String tenantId;
    private String queryHash;
    private String dataVersion;
    private String abcType;
    private Integer aaNum;
    private Integer abNum;
    private Integer acNum;
    private Integer anNum;
    private Integer atNum;
    private Integer baNum;
    private Integer bbNum;
    private Integer bcNum;
    private Integer bnNum;
    private Integer btNum;
    private Integer caNum;
    private Integer cbNum;
    private Integer ccNum;
    private Integer cnNum;
    private Integer ctNum;
}

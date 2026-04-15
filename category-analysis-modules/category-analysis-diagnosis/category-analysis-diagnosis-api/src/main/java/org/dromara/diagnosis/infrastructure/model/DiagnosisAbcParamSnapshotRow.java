package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiagnosisAbcParamSnapshotRow {

    private Long id;
    private String tenantId;
    private String queryHash;
    private String dataVersion;
    private String abcType;
    private String abcTypeName;
    private BigDecimal salesPer;
    private BigDecimal grossPer;
    private BigDecimal saleQuantityPer;
    private BigDecimal aRate;
    private BigDecimal bRate;
    private BigDecimal cRate;
    private BigDecimal aSkuRate;
    private BigDecimal bSkuRate;
    private BigDecimal cSkuRate;
}

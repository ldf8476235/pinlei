package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiagnosisAbcBucketRow {

    private Long id;
    private String tenantId;
    private String queryHash;
    private String dataVersion;
    private String abcType;
    private String bucket;
    private BigDecimal currentSalesPer;
    private BigDecimal currentSkuPer;
    private BigDecimal compareSalesPer;
    private BigDecimal compareSkuPer;
    private BigDecimal setSalesPer;
    private BigDecimal setSkuPer;
    private Integer currentSku;
    private Integer compareSku;
    private Integer changeSku;
    private BigDecimal currentSales;
    private BigDecimal stockQuantity;
    private BigDecimal stockQuantityPer;
}

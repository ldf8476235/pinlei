package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DiagnosisVendorMetricRow {

    private Long id;
    private String tenantId;
    private String queryHash;
    private String dataVersion;
    private String productVendorNo;
    private String productVendorName;
    private String productVendorNoName;
    private String productVendorStatusNo;
    private String productVendorStatusName;
    private Integer skuCount;
    private Integer newSkuCount;
    private BigDecimal sales;
    private BigDecimal salesPer;
    private BigDecimal gross;
    private BigDecimal grossRate;
    private BigDecimal oiAmount;
    private BigDecimal allGross;
    private BigDecimal allGrossRate;
    private BigDecimal netOrderAmount;
    private BigDecimal diffOrderAmount;
    private BigDecimal diffOrderAmountRate;
    private BigDecimal unitOutput;
    private LocalDateTime snapshotTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

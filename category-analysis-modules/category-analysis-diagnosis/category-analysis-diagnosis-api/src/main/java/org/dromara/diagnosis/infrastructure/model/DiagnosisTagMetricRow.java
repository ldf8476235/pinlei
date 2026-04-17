package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DiagnosisTagMetricRow {

    private Long id;
    private String tenantId;
    private String queryHash;
    private String dataVersion;
    private String tagType;
    private String tagTypeName;
    private String tagNo;
    private String tagName;
    private String metricKey;
    private Boolean isUntagged;
    private Integer skuCount;
    private Integer compareSkuCount;
    private Integer skuChange;
    private BigDecimal skuInc;
    private BigDecimal skuPer;
    private BigDecimal saleQuantity;
    private BigDecimal compareSaleQuantity;
    private BigDecimal saleQuantityChange;
    private BigDecimal saleQuantityInc;
    private BigDecimal saleQuantityPer;
    private BigDecimal saleQuantityPsd;
    private BigDecimal sales;
    private BigDecimal compareSales;
    private BigDecimal salesChange;
    private BigDecimal salesInc;
    private BigDecimal salesPer;
    private BigDecimal salesPsd;
    private BigDecimal gross;
    private BigDecimal compareGross;
    private BigDecimal grossChange;
    private BigDecimal grossInc;
    private BigDecimal grossPer;
    private BigDecimal grossPsd;
    private BigDecimal grossRate;
    private BigDecimal compareGrossRate;
    private BigDecimal grossRateInc;
    private BigDecimal stockQuantity;
    private BigDecimal compareStockQuantity;
    private BigDecimal salesCost;
    private BigDecimal compareSalesCost;
    private BigDecimal turnoverRate;
    private BigDecimal turnoverDays;
    private BigDecimal stockSalesRate;
    private BigDecimal contributionRate;
    private BigDecimal gmroi;
    private BigDecimal salesRate;
    private Integer activitySku;
    private Integer activeStoreCount;
    private Integer periodDays;
    private LocalDateTime snapshotTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

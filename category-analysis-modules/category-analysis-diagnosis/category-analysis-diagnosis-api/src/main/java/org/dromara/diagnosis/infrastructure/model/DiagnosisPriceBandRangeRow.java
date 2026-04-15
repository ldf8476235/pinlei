package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DiagnosisPriceBandRangeRow {

    private Long id;
    private String tenantId;
    private String queryHash;
    private String dataVersion;
    private BigDecimal priceBandMin;
    private BigDecimal priceBandMax;
    private String priceBandLabel;
    private Integer sku;
    private BigDecimal skuPer;
    private BigDecimal saleQuantity;
    private BigDecimal saleQuantityPer;
    private BigDecimal saleQuantityUnit;
    private BigDecimal sales;
    private BigDecimal salesPer;
    private Integer activitySku;
    private Integer suggestSku;
    private BigDecimal suggestSkuPer;
    private BigDecimal salePrice;
    private String bandLevel;
    private String strategyCode;
    private LocalDateTime snapshotTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DiagnosisPriceBandPointRow {

    private Long id;
    private String tenantId;
    private String queryHash;
    private String dataVersion;
    private BigDecimal priceBandMin;
    private BigDecimal priceBandMax;
    private BigDecimal salePrice;
    private Integer sku;
    private BigDecimal sales;
    private BigDecimal saleQuantity;
    private BigDecimal totalSales;
    private String waveType;
    private LocalDateTime snapshotTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

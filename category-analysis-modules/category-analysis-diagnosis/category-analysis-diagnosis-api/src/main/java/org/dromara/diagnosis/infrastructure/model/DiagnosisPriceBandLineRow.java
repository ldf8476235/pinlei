package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DiagnosisPriceBandLineRow {

    private Long id;
    private String tenantId;
    private String queryHash;
    private String dataVersion;
    private BigDecimal priceLine;
    private Integer sku;
    private BigDecimal sales;
    private LocalDateTime snapshotTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

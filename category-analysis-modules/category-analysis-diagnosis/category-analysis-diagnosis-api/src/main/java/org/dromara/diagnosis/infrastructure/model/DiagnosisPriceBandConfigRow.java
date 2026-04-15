package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DiagnosisPriceBandConfigRow {

    private Long id;
    private String tenantId;
    private String queryHash;
    private Integer sortNo;
    private BigDecimal priceBandMin;
    private BigDecimal priceBandMax;
    private String isOpenEnded;
    private String isActive;
    private String createdBy;
    private LocalDateTime createdTime;
    private String updatedBy;
    private LocalDateTime updatedTime;
}

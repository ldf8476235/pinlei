package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 诊断趋势快照行模型.
 */
@Data
public class DiagnosisTrendSnapshotRow {

    private Long id;

    private String tenantId;

    private String queryHash;

    private String metricCode;

    private LocalDate pointDate;

    private BigDecimal currentValue;

    private BigDecimal compareValue;

    private BigDecimal growthRate;

    private String periodLabel;

    private String dataVersion;

    private LocalDateTime snapshotTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

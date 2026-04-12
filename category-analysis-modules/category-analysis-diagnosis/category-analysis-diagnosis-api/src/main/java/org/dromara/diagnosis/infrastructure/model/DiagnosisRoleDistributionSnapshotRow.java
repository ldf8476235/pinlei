package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 诊断角色分布快照行模型.
 */
@Data
public class DiagnosisRoleDistributionSnapshotRow {

    private Long id;

    private String tenantId;

    private String queryHash;

    private String roleCode;

    private String roleName;

    private BigDecimal salesAmount;

    private Integer skuCount;

    private BigDecimal salesRatio;

    private String dataVersion;

    private LocalDateTime snapshotTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}


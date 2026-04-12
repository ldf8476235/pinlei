package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 诊断概览快照行模型.
 */
@Data
public class DiagnosisOverviewSnapshotRow {

    private Long id;

    private String tenantId;

    private String queryHash;

    private Integer classLevel;

    private String classNo;

    private String className;

    private String retailTypeId;

    private Long deptId;

    private String businessCircleId;

    private String deptGroupId;

    private String storeNo;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private LocalDate compareStart;

    private LocalDate compareEnd;

    private BigDecimal metricTotalSales;

    private BigDecimal metricTotalProfit;

    private BigDecimal metricProfitMargin;

    private Integer metricTotalSku;

    private Integer metricActiveSku;

    private BigDecimal metricSalesRate;

    private String dataVersion;

    private LocalDateTime snapshotTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Snapshot row for subclass contribution trend.
 */
@Data
public class DiagnosisSubclassTrendRow {

    private Long id;
    private String tenantId;
    private String queryHash;
    private String dataVersion;
    private Integer parentClassLevel;
    private String parentClassNo;
    private Integer subClassLevel;
    private String subClassNo;
    private String subClassName;
    private String retailTypeId;
    private Long deptId;
    private String businessCircleId;
    private String deptGroupId;
    private String storeNo;
    private Integer pointIndex;
    private LocalDate pointDate;
    private BigDecimal sales;
    private LocalDateTime snapshotTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

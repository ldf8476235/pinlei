package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DiagnosisRecordQueryRow {

    private Long jobId;

    private String requestHash;

    private String statusCode;

    private BigDecimal progressPercent;

    private String currentStage;

    private Long submittedBy;

    private LocalDateTime submittedTime;

    private Integer classLevel;

    private String classNo;

    private String className;

    private String deptId;

    private String deptName;

    private String retailTypeId;

    private String retailTypeName;

    private String businessCircleId;

    private String businessCircleName;

    private String deptGroupId;

    private String deptGroupName;

    private String storeNo;

    private String storeName;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private LocalDate compareStart;

    private LocalDate compareEnd;

    private String dataVersion;

    private Long snapshotId;

    private String createByName;
}

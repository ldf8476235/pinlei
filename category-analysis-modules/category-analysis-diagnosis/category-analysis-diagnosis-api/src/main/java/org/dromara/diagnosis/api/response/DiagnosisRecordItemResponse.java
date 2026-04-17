package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DiagnosisRecordItemResponse {

    private String recordId;

    private Long jobId;

    private String requestHash;

    private String dataVersion;

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

    private String storeRangeName;

    private String status;

    private String statusLabel;

    private Boolean ready;

    private Boolean canViewReport;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private LocalDate compareStart;

    private LocalDate compareEnd;

    private Long createBy;

    private String createByName;

    private LocalDateTime createTime;

    private BigDecimal progressPercent;

    private String currentStage;
}

package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预计算任务行模型.
 */
@Data
public class DiagnosisPrecomputeJobRow {

    private Long jobId;

    private String tenantId;

    private String jobCode;

    private String moduleCode;

    private String statusCode;

    private Integer priority;

    private String requestJson;

    private String requestHash;

    private String readRangeType;

    private LocalDate readStart;

    private LocalDate readEnd;

    private String windowTypes;

    private String forceRebuild;

    private Integer maxRetry;

    private Integer retryCount;

    private BigDecimal progressPercent;

    private String currentStage;

    private Integer totalWindows;

    private Integer doneWindows;

    private Long rowsRead;

    private Long rowsWritten;

    private String orchestratorStatus;

    private String moduleProgressJson;

    private Long submittedBy;

    private LocalDateTime submittedTime;

    private LocalDateTime startedTime;

    private LocalDateTime finishedTime;

    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

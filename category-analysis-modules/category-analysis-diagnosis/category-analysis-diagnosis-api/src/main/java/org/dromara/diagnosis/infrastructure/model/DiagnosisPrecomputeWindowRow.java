package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预计算窗口行模型.
 */
@Data
public class DiagnosisPrecomputeWindowRow {

    private Long windowId;

    private String tenantId;

    private Long jobId;

    private String windowKey;

    private String windowType;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private LocalDate compareStart;

    private LocalDate compareEnd;

    private String statusCode;

    private BigDecimal progressPercent;

    private String currentStage;

    private String dataVersion;

    private Integer retryCount;

    private Long rowsRead;

    private Long rowsWritten;

    private LocalDateTime startedTime;

    private LocalDateTime finishedTime;

    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

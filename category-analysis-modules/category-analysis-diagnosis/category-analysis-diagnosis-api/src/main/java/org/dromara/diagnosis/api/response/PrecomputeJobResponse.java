package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 预计算任务响应模型.
 */
@Data
public class PrecomputeJobResponse {

    private Long jobId;

    private String jobCode;

    private String status;

    private Integer priority;

    private String module;

    private String readRangeType;

    private LocalDate readStart;

    private LocalDate readEnd;

    private String windowTypes;

    private Integer maxRetry;

    private Integer retryCount;

    private BigDecimal progressPercent;

    private String currentStage;

    private Integer totalWindows;

    private Integer doneWindows;

    private Long rowsRead;

    private Long rowsWritten;

    private String errorMessage;

    private LocalDateTime submittedTime;

    private LocalDateTime startedTime;

    private LocalDateTime finishedTime;

    private List<PrecomputeWindowResponse> windows;
}

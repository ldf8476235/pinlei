package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预计算窗口响应.
 */
@Data
public class PrecomputeWindowResponse {

    private Long windowId;

    private String windowKey;

    private String windowType;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private LocalDate compareStart;

    private LocalDate compareEnd;

    private String status;

    private BigDecimal progressPercent;

    private String currentStage;

    private String dataVersion;

    private Integer retryCount;

    private Long rowsRead;

    private Long rowsWritten;

    private String errorMessage;

    private LocalDateTime startedTime;

    private LocalDateTime finishedTime;
}

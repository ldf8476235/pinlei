package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 预计算任务进度响应.
 */
@Data
public class PrecomputeJobProgressResponse {

    private Long jobId;

    private String status;

    private BigDecimal progressPercent;

    private String currentStage;

    private Integer windowDone;

    private Integer totalWindow;

    private Long rowsRead;

    private Long rowsWritten;
}

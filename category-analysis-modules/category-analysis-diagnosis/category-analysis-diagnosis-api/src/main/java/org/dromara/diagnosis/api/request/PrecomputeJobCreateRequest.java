package org.dromara.diagnosis.api.request;

import lombok.Data;

import java.time.LocalDate;

/**
 * 预计算任务创建请求.
 */
@Data
public class PrecomputeJobCreateRequest {

    private String module;

    private String readRangeType;

    private LocalDate readStart;

    private LocalDate readEnd;

    /**
     * 对比期开始时间（可选）.
     */
    private LocalDate compareStart;

    /**
     * 对比期结束时间（可选）.
     */
    private LocalDate compareEnd;

    /**
     * 逗号分隔的窗口类型, 例如 MONTH,YOY.
     */
    private String windowTypes;

    private Boolean forceRebuild;

    /**
     * 最大重试次数（默认 1）.
     */
    private Integer maxRetry;

    private Integer priority;

    private String requestJson;

    private Long submitterId;
}

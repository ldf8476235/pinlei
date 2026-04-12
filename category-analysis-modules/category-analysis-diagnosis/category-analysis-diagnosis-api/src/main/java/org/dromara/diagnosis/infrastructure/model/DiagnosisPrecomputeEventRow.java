package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预计算事件行模型.
 */
@Data
public class DiagnosisPrecomputeEventRow {

    private Long eventId;

    private String tenantId;

    private Long jobId;

    private Long windowId;

    private LocalDateTime eventTime;

    private String eventLevel;

    private String eventStage;

    private String eventMessage;

    private String payloadJson;

    private LocalDateTime createTime;
}

package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预计算事件响应.
 */
@Data
public class PrecomputeEventResponse {

    private Long eventId;

    private Long jobId;

    private Long windowId;

    private LocalDateTime eventTime;

    private String eventLevel;

    private String eventStage;

    private String eventMessage;

    private String payloadJson;
}


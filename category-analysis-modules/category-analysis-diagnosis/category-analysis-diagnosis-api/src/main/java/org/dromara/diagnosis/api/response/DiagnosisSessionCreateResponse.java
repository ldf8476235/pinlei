package org.dromara.diagnosis.api.response;

import lombok.Data;

/**
 * 诊断会话创建响应.
 */
@Data
public class DiagnosisSessionCreateResponse {

    private String sessionId;

    private String queryHash;

    private String dataVersion;

    private Boolean cacheHit;

    private Boolean ready;

    private Long triggeredJobId;

    /**
     * SNAPSHOT_HIT / TRIGGERED / WAIT_READY
     */
    private String source;
}

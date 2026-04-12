package org.dromara.diagnosis.application.model;

import lombok.Data;

/**
 * 诊断会话缓存模型.
 */
@Data
public class DiagnosisSessionCacheModel {

    private String sessionId;

    private String queryHash;

    private String dataVersion;
}


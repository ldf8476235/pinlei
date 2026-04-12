package org.dromara.diagnosis.api.response;

import lombok.Data;
import org.dromara.diagnosis.infrastructure.model.DiagnosisOverviewSnapshotRow;

/**
 * 诊断概览响应.
 */
@Data
public class DiagnosisOverviewResponse {

    private String sessionId;

    private String dataVersion;

    private Boolean cacheHit;

    private DiagnosisOverviewSnapshotRow overview;
}


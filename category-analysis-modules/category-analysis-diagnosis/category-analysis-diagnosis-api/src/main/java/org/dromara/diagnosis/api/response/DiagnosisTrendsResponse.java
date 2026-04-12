package org.dromara.diagnosis.api.response;

import lombok.Data;
import org.dromara.diagnosis.infrastructure.model.DiagnosisTrendSnapshotRow;

import java.util.List;

/**
 * 诊断趋势响应.
 */
@Data
public class DiagnosisTrendsResponse {

    private String sessionId;

    private String metricCode;

    private String dataVersion;

    private Boolean cacheHit;

    private List<DiagnosisTrendSnapshotRow> trends;
}


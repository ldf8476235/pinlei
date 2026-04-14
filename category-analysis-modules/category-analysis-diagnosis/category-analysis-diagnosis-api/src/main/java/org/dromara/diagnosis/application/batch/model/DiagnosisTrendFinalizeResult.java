package org.dromara.diagnosis.application.batch.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiagnosisTrendFinalizeResult {

    private long basicTrendRows;
    private long categoryPerformanceTrendRows;
}

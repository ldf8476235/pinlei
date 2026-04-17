package org.dromara.diagnosis.application.batch.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiagnosisBrandFinalizeResult {

    private long overviewRows;
    private long metricRows;
    private long jsonRows;
}

package org.dromara.diagnosis.application.batch.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiagnosisSubclassFinalizeResult {

    private long contributionRows;

    private long trendRows;
}

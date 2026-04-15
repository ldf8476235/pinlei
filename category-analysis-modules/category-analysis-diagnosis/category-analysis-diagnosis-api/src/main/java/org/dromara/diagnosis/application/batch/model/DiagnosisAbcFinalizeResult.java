package org.dromara.diagnosis.application.batch.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiagnosisAbcFinalizeResult {

    private long paramsRows;
    private long bucketRows;
    private long matrixRows;
    private long skuRows;
}

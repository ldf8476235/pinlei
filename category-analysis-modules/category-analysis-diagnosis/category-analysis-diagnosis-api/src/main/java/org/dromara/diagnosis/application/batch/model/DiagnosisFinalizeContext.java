package org.dromara.diagnosis.application.batch.model;

import lombok.Builder;
import lombok.Data;
import org.dromara.diagnosis.api.request.DiagnosisSessionCreateRequest;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceShardParam;

import java.time.LocalDate;

@Data
@Builder
public class DiagnosisFinalizeContext {

    private Long jobId;
    private Long windowId;
    private String dataVersion;
    private String requestJson;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private long periodDays;
    private LocalDate compareStart;
    private LocalDate compareEnd;
    private long compareDays;
    private DiagnosisSourceShardParam param;
    private DiagnosisSourceShardParam compareParam;
    private DiagnosisSessionCreateRequest hashRequest;
    private String queryHash;
}

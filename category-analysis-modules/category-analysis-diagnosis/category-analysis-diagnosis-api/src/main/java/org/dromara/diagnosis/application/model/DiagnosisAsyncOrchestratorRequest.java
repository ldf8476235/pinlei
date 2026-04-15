package org.dromara.diagnosis.application.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DiagnosisAsyncOrchestratorRequest {

    private Long jobId;

    private String queryHash;

    private String dataVersion;

    private String requestJson;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private LocalDate compareStart;

    private LocalDate compareEnd;
}

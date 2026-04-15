package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiagnosisSessionStatusResponse {

    private String sessionId;

    private String queryHash;

    private String dataVersion;

    private Long jobId;

    private Boolean ready;

    private String status;

    private BigDecimal progressPercent;

    private String currentStage;

    private String orchestratorStatus;

    private String moduleProgressJson;
}

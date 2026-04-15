package org.dromara.diagnosis.application.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class OrchestratorResult {

    private boolean success;

    private Map<String, Long> moduleRows;

    private Map<String, String> moduleStatus;

    private Map<String, String> moduleErrors;
}

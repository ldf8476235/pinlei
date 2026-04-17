package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DiagnosisTagJsonRow {

    private Long id;
    private String tenantId;
    private String queryHash;
    private String dataVersion;
    private String payloadCode;
    private String payloadName;
    private String payloadJson;
    private LocalDateTime snapshotTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

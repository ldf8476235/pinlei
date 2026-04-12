package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 诊断洞察快照行模型.
 */
@Data
public class DiagnosisInsightSnapshotRow {

    private Long id;

    private String tenantId;

    private String queryHash;

    private String insightType;

    private String insightCode;

    private String title;

    private String content;

    private String severity;

    private Integer sortNo;

    private String dataVersion;

    private LocalDateTime snapshotTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}


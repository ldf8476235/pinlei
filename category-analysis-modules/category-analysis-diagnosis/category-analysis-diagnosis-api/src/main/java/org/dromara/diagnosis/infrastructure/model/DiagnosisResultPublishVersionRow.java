package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DiagnosisResultPublishVersionRow {

    private Long id;

    private String tenantId;

    private String queryHash;

    private String dataVersion;

    private String publishStatus;

    private Long jobId;

    private String errorSummary;

    private LocalDateTime publishedTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

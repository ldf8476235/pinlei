package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.time.LocalDate;

/**
 * 批处理分片查询参数.
 */
@Data
public class DiagnosisSourceShardParam {

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private Long startId;

    private Long endId;
}

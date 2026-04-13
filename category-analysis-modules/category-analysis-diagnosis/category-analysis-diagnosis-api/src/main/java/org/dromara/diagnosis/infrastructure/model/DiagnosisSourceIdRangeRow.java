package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

/**
 * 源数据主键范围.
 */
@Data
public class DiagnosisSourceIdRangeRow {

    private Long minId;

    private Long maxId;
}

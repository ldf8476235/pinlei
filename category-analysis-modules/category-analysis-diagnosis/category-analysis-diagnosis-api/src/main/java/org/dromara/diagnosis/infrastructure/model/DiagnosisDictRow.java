package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

/**
 * 诊断字典行.
 */
@Data
public class DiagnosisDictRow {

    private Long dictCode;

    private Long dictId;

    private Integer dictSort;

    private String dictLabel;

    private String dictValue;

    private String createTime;
}


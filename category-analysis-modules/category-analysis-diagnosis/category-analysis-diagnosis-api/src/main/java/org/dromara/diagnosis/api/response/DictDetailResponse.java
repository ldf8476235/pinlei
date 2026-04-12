package org.dromara.diagnosis.api.response;

import lombok.Data;

/**
 * 字典明细响应.
 */
@Data
public class DictDetailResponse {

    private String createTime;

    private Long dictId;

    private Long id;

    private String label;

    private String sort;

    private String value;
}


package org.dromara.diagnosis.api.request;

import lombok.Data;

/**
 * 门店查询请求.
 */
@Data
public class StoreFindRequest {

    private String keyword;

    private String retailTypeId;

    private String businessCircleId;

    private String deptGroupId;

    /**
     * 最大返回条数，默认 20，最大 200.
     */
    private Integer limit;
}


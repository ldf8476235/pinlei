package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

/**
 * SQL 查询过滤参数.
 */
@Data
public class CategoryTreeQueryParam {

    private String retailTypeId;

    private String businessCircleId;

    private String deptGroupId;

    private String storeNo;
}


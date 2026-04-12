package org.dromara.diagnosis.api.request;

import lombok.Data;

import java.util.List;

/**
 * 品类树查询参数.
 */
@Data
public class CategoryTreeQueryRequest {

    private Long deptId;

    private String retailTypeId;

    private String businessCircleId;

    private String deptGroupId;

    private String storeNo;

    private Integer classLevel;

    private List<String> classNo;

    private List<String> classRole;

    private List<String> skuAbnormal;
}


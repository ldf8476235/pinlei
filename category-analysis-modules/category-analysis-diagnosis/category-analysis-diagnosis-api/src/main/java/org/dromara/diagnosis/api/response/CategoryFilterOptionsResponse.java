package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.List;

/**
 * 品类树筛选项响应.
 */
@Data
public class CategoryFilterOptionsResponse {

    private List<DictOptionResponse> categoryLevels;

    private List<DictOptionResponse> categoryRoles;

    private List<DictDetailResponse> classSalesStatusNo;
}


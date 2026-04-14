package org.dromara.diagnosis.application.service;

import org.dromara.diagnosis.api.request.CategoryTreeQueryRequest;
import org.dromara.diagnosis.api.response.CategoryClassTreeNodeResponse;
import org.dromara.diagnosis.api.response.CategoryFilterOptionsResponse;
import org.dromara.diagnosis.api.request.CategoryNodeConfigUpdateRequest;
import org.dromara.diagnosis.api.response.CategoryNodeConfigUpdateResponse;
import org.dromara.diagnosis.api.response.CategoryTreeNodeResponse;

import java.util.List;

/**
 * 品类树服务.
 */
public interface CategoryTreeService {

    List<CategoryTreeNodeResponse> queryTree(CategoryTreeQueryRequest request);

    List<CategoryClassTreeNodeResponse> queryClassTree(Integer level);

    CategoryFilterOptionsResponse queryFilterOptions();

    CategoryNodeConfigUpdateResponse updateNodeConfig(CategoryNodeConfigUpdateRequest request);
}

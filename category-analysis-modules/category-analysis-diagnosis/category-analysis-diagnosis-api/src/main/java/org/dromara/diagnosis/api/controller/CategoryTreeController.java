package org.dromara.diagnosis.api.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.request.CategoryClassTreeRequest;
import org.dromara.diagnosis.api.response.CategoryClassTreeResponse;
import org.dromara.diagnosis.api.response.CategoryFilterOptionsResponse;
import org.dromara.diagnosis.api.request.CategoryTreeQueryRequest;
import org.dromara.diagnosis.api.response.CategoryTreeNodeResponse;
import org.dromara.diagnosis.api.response.CategoryTreeResponse;
import org.dromara.diagnosis.application.service.CategoryTreeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 品类树接口.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryTreeController {

    private final CategoryTreeService categoryTreeService;

    @PostMapping({"/tree", "/tree/query"})
    public CategoryTreeResponse<List<CategoryTreeNodeResponse>> queryTree(@RequestBody(required = false) CategoryTreeQueryRequest request) {
        return CategoryTreeResponse.ok(categoryTreeService.queryTree(request));
    }

    @PostMapping("/filter-options")
    public CategoryTreeResponse<CategoryFilterOptionsResponse> queryFilterOptions() {
        return CategoryTreeResponse.ok(categoryTreeService.queryFilterOptions());
    }

    @PostMapping("/class-tree")
    public CategoryClassTreeResponse queryClassTree(@RequestBody(required = false) CategoryClassTreeRequest request) {
        CategoryClassTreeResponse response = new CategoryClassTreeResponse();
        response.setContent(categoryTreeService.queryClassTree(request == null ? null : request.getLevel()));
        return response;
    }
}

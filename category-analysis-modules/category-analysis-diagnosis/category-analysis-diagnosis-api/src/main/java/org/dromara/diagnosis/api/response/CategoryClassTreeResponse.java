package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.List;

/**
 * 品类树响应(兼容旧版结构).
 */
@Data
public class CategoryClassTreeResponse {

    private List<CategoryClassTreeNodeResponse> content;
}


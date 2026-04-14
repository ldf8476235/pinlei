package org.dromara.diagnosis.api.request;

import lombok.Data;

/**
 * Category tree node config update request.
 */
@Data
public class CategoryNodeConfigUpdateRequest {

    private String storeNo;

    private String classNo;

    private String roleNo;

    private Integer suggestSaleSku;
}


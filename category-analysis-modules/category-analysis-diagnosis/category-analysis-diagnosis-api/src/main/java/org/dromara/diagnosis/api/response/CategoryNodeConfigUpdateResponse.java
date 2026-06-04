package org.dromara.diagnosis.api.response;

import lombok.Data;

/**
 * Category tree node config update response.
 */
@Data
public class CategoryNodeConfigUpdateResponse {

    private String storeNo;

    private String classNo;

    private String roleNo;

    private String roleType;

    private Integer suggestSaleSku;

    private Integer sysSuggestSaleSku;
}

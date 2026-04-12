package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

/**
 * 建议SKU与角色行.
 */
@Data
public class CategorySkuMetricRow {

    private String classNo;

    private Integer suggestSaleSku;

    private Integer sysSuggestSaleSku;

    private String roleNo;
}


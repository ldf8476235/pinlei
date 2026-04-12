package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.List;

/**
 * 品类树节点.
 */
@Data
public class CategoryTreeNodeResponse {

    private String classNo;

    private String className;

    private String parentClassNo;

    private Integer classLevel;

    private Integer suggestSaleSku;

    private Integer saleSku;

    private String roleNo;

    private String roleType;

    private Integer skuDiffer;

    private Integer sysSuggestSaleSku;

    private List<CategoryTreeNodeResponse> subClass;
}


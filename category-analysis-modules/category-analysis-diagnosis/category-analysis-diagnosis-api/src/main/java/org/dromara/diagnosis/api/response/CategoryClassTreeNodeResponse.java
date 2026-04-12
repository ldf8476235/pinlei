package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.List;

/**
 * 品类树节点(兼容旧版字段).
 */
@Data
public class CategoryClassTreeNodeResponse {

    private String level;

    private String flevel;

    private Integer levelFlag;

    private String className;

    private String labelName;

    private Integer stateFlag;

    private Integer sku;

    private String updateFlag;

    private String updateTime;

    private Boolean canChoose;

    private List<CategoryClassTreeNodeResponse> children;

    private String id;

    private String label;
}


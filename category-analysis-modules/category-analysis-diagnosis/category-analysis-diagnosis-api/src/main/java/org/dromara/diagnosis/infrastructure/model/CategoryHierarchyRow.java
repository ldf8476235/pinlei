package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

/**
 * 品类层级基础行.
 */
@Data
public class CategoryHierarchyRow {

    private String classNo;

    private String className;

    private String parentClassNo;

    private Integer classLevel;
}


package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AllClassCheckSkuDifferItemResponse {

    private String classNo;
    private String className;
    private Integer classLevel;
    private Integer suggestSaleSku;
    private Integer saleSku;
    private Integer skuDiffer;
    private String classRole;
    private String classRoleName;
    private String classRoleType;
    private String classRoleTypeDescribe;
}

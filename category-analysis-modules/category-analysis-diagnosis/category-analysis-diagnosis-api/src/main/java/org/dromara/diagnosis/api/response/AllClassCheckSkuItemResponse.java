package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AllClassCheckSkuItemResponse {

    private String classNo;
    private String className;
    private Integer classLevel;
    private BigDecimal sales;
    private BigDecimal salesPer;
    private BigDecimal skuPer;
    private BigDecimal skuDifference;
    private Integer classSku;
    private String classRole;
    private String classRoleName;
    private String classRoleType;
    private String classRoleTypeDescribe;
}

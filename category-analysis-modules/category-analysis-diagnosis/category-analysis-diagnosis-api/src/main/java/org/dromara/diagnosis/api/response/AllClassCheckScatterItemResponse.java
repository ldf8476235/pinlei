package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AllClassCheckScatterItemResponse {

    private String classNo;
    private String className;
    private Integer classLevel;
    private BigDecimal sales;
    private BigDecimal salesCompare;
    private BigDecimal gross;
    private BigDecimal saleQuantity;
    private BigDecimal salesCompareRate;
    private BigDecimal contributionRatePer;
    private BigDecimal salesPer;
    private String classRole;
    private String classRoleName;
    private String classRoleType;
    private String classRoleTypeDescribe;
}

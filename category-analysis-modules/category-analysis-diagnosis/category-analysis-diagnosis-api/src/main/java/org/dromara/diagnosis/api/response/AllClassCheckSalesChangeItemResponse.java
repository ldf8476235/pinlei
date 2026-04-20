package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AllClassCheckSalesChangeItemResponse {

    private String classNo;
    private String className;
    private Integer classLevel;
    private BigDecimal sales;
    private BigDecimal salesCompare;
    private BigDecimal salesCompareRate;
    private BigDecimal contributionRatePer;
    private BigDecimal salesPer;
    private String classRole;
    private String classRoleName;
    private String classRoleType;
    private String classRoleTypeDescribe;
}

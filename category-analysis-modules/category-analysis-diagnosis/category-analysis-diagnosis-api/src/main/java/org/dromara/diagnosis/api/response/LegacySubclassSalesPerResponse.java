package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LegacySubclassSalesPerResponse {

    private String classNo;
    private String className;
    private Integer classLevel;
    private String parentClassNo;
    private BigDecimal sales;
    private BigDecimal salesPer;
    private String dataDate;
}

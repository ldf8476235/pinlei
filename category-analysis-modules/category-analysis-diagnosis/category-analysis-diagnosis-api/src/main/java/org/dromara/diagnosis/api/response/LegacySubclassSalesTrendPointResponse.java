package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LegacySubclassSalesTrendPointResponse {

    private String dataDate;
    private String classNo;
    private String className;
    private BigDecimal sales;
}

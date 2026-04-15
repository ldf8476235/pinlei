package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AbcTypeParamResponse {

    private String abcType;
    private String abcTypeName;
    private BigDecimal salesPer;
    private BigDecimal grossPer;
    private BigDecimal salesQuantityPer;
    private BigDecimal crate;
    private BigDecimal arate;
    private BigDecimal brate;
    private BigDecimal askuRate;
    private BigDecimal bskuRate;
    private BigDecimal cskuRate;
}

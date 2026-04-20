package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AllClassCheckSkuYDataResponse {

    private BigDecimal yTwoSkuDifferenceMin;
    private BigDecimal yTwoSkuDifferenceMax;
    private BigDecimal yOneSkuPerMin;
    private BigDecimal yOneSalesPerMin;
    private BigDecimal yOneSkuPerMax;
    private BigDecimal yOneSalesPerMax;
}

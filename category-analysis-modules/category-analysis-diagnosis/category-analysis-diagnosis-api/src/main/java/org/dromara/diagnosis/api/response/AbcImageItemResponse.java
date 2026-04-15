package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AbcImageItemResponse {

    private String abcType;
    private BigDecimal currentSalesPer;
    private BigDecimal currentSkuPer;
    private BigDecimal compareSalesPer;
    private BigDecimal compareSkuPer;
    private BigDecimal setSalesPer;
    private BigDecimal setSkuPer;
}

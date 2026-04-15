package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AbcDetailsItemResponse {

    private String abcType;
    private BigDecimal setSalesPer;
    private BigDecimal setSales;
    private BigDecimal setSkuPer;
    private BigDecimal setSku;
    private BigDecimal currentSku;
    private BigDecimal currentSkuPer;
    private BigDecimal stockQuantity;
    private BigDecimal stockQuantityPer;
    private BigDecimal compareSku;
    private BigDecimal compareSkuPer;
    private BigDecimal changeSku;
    private BigDecimal currentSales;
}

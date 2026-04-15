package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceBandRangeSummaryItemResponse {

    private BigDecimal priceBandMin;
    private BigDecimal priceBandMax;
    private String priceBand;
    private Integer sku;
    private BigDecimal skuPer;
    private BigDecimal saleQuantity;
    private BigDecimal saleQuantityPer;
    private BigDecimal saleQuantityUnit;
    private BigDecimal sales;
    private BigDecimal salesPer;
    private Integer activitySku;
    private Integer suggestSku;
    private BigDecimal suggestSkuPer;
    private BigDecimal salePrice;
}

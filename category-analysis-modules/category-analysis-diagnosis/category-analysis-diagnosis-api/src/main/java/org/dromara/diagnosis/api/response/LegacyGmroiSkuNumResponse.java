package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LegacyGmroiSkuNumResponse {

    private Integer sku_1;
    private Integer sku_2;
    private Integer sku_3;
    private Integer sku_4;
    private BigDecimal skuPer_1;
    private BigDecimal skuPer_2;
    private BigDecimal skuPer_3;
    private BigDecimal skuPer_4;
}


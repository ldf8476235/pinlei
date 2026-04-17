package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TagSalesSkuItemResponse {

    private String tagName;
    private BigDecimal sales;
    private BigDecimal salesPer;
    private Integer sku;
    private BigDecimal skuPer;
}

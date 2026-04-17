package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BrandSalesShareItemResponse {

    private String productBrand;
    private BigDecimal sales;
    private BigDecimal salesPer;
}

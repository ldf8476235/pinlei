package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpecSkuSalesChangeItemResponse {

    private String productSpec;
    private String productNo;
    private Integer sku;
    private BigDecimal sales;
    private BigDecimal currentSales;
    private BigDecimal compareSales;
}

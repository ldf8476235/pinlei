package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BrandSkuSalesChangeItemResponse {

    private String brandNo;
    private String productBrand;
    private String ownBrandNo;
    private String ownBrandName;
    private String productNo;
    private Integer sku;
    private BigDecimal sales;
    private BigDecimal currentSales;
    private BigDecimal compareSales;
}

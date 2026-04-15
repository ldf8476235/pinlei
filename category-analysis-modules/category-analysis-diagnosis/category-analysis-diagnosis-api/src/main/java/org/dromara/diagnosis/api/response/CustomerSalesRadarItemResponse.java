package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerSalesRadarItemResponse {

    private Integer gender;
    private String ageCode;
    private String ageName;
    private Integer ageStart;
    private Integer ageEnd;

    private BigDecimal currentSales;
    private BigDecimal currentCustomerCount;
    private BigDecimal currentCustomerPrice;
    private BigDecimal currentUnitPrice;
    private BigDecimal currentCountAve;
    private BigDecimal currentSaleQuantity;

    private BigDecimal compareSales;
    private BigDecimal compareCustomerCount;
    private BigDecimal compareCustomerPrice;
    private BigDecimal compareUnitPrice;
    private BigDecimal compareCountAve;
    private BigDecimal compareSaleQuantity;

    private BigDecimal salesGrowth;
    private BigDecimal customerGrowth;
}

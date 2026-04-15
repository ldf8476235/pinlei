package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LegacySubclassSalesListItemResponse {

    private String classNo;
    private String className;
    private Integer classLevel;
    private String parentClassNo;
    private BigDecimal currentSales;
    private BigDecimal currentSalesPer;
    private BigDecimal currentGross;
    private BigDecimal currentGrossPer;
    private BigDecimal currentGrossRate;
    private BigDecimal currentSaleQuantity;
    private BigDecimal currentCustomerCount;
    private BigDecimal currentCustomerPrice;
    private BigDecimal compareSales;
    private BigDecimal compareSalesPer;
    private BigDecimal compareSalesAddRate;
    private BigDecimal compareGross;
    private BigDecimal compareGrossPer;
    private BigDecimal compareGrossAddRate;
    private BigDecimal compareGrossRate;
    private BigDecimal compareSaleQuantity;
    private BigDecimal compareSaleQuantityAddRate;
    private BigDecimal compareCustomerCount;
    private BigDecimal compareCustomerPrice;
    private BigDecimal compareCustomerPriceAddRate;
    private BigDecimal currentTurnoverRate;
    private BigDecimal currentTurnoverDays;
    private BigDecimal gmroi;
    private BigDecimal saleCost;
}

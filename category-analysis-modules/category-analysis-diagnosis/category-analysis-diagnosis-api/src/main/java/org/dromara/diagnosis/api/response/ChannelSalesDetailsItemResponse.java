package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChannelSalesDetailsItemResponse {

    private String channelName;
    private Integer saleChannel;
    private String onlineType;
    private String onlineName;

    private BigDecimal currentSales;
    private BigDecimal currentSalesPer;
    private BigDecimal currentGross;
    private BigDecimal currentGrossPer;
    private BigDecimal currentGrossRate;
    private BigDecimal currentCustomerCount;
    private BigDecimal currentCustomerPrice;

    private BigDecimal compareSales;
    private BigDecimal compareSalesPer;
    private BigDecimal compareSalesInc;
    private BigDecimal compareGross;
    private BigDecimal compareGrossPer;
    private BigDecimal compareGrossInc;
    private BigDecimal compareGrossRate;
    private BigDecimal compareCustomerCount;
    private BigDecimal compareCustomerCountInc;
    private BigDecimal compareCustomerPrice;
    private BigDecimal compareCustomerPriceInc;
}

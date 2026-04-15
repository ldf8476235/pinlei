package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiagnosisCustomerAgeSummaryRow {

    private String ageCode;
    private String ageName;
    private Integer ageStart;
    private Integer ageEnd;
    private Integer ageOrder;

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
    private BigDecimal customerPriceGrowth;
    private BigDecimal unitPriceGrowth;
    private BigDecimal countAveGrowth;
    private BigDecimal saleQuantityGrowth;

    private BigDecimal currentManSales;
    private BigDecimal currentManCustomerCount;
    private BigDecimal currentManCustomerPrice;
    private BigDecimal currentManUnitPrice;
    private BigDecimal currentManCountAve;

    private BigDecimal currentWomanSales;
    private BigDecimal currentWomanCustomerCount;
    private BigDecimal currentWomanCustomerPrice;
    private BigDecimal currentWomanUnitPrice;
    private BigDecimal currentWomanCountAve;

    private BigDecimal currentUnknownSales;
    private BigDecimal currentUnknownCustomerCount;
    private BigDecimal currentUnknownCustomerPrice;
    private BigDecimal currentUnknownUnitPrice;
    private BigDecimal currentUnknownCountAve;
}

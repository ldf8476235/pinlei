package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ObsoleteGoodsItemResponse {

    private String productNo;
    private String productName;
    private String handleStatus;
    private String dealStatus;
    private String dealStatusName;
    private String productStatus;
    private String productStatusNo;
    private Integer storeNum;
    private String currentSalesAbc;
    private String compareSalesAbc;
    private String currentGrossAbc;
    private String compareGrossAbc;
    private String currentContributionAbc;
    private String compareContributionAbc;
    private String currentGrossRole;
    private String currentGrossRoleName;
    private String compareGrossRole;
    private String compareGrossRoleName;
    private String currentGmroiRole;
    private String currentGmroiRoleName;
    private String compareGmroiRole;
    private String compareGmroiRoleName;
    private BigDecimal saleQuantity;
    private BigDecimal saleQuantityPsd;
    private BigDecimal sales;
    private BigDecimal salesPer;
    private BigDecimal salesPsd;
    private BigDecimal gross;
    private BigDecimal grossGrowth;
    private BigDecimal grossPer;
    private BigDecimal grossPsd;
    private BigDecimal grossRate;
    private BigDecimal grossRateGrowth;
    private BigDecimal stockQuantity;
    private BigDecimal turnoverRate;
    private BigDecimal turnoverDays;
    private BigDecimal stockSalesRate;
    private BigDecimal contributionRate;
    private BigDecimal gmroi;
    private BigDecimal salesRate;
    private String activity;
    private String activitySku;
    private LocalDate firstSaleDate;
    private String newProduct;
    private String keyProduct;
    private String seasonableFlag;
    private String seasonableFlagName;
    private LocalDate seasonableStartDate;
    private LocalDate seasonableEndDate;
    private String productTags;
    private String allTagName;
    private String classNo;
    private String className;
    private Integer classLevel;
    private String productBarcode;
    private String brandName;
    private String spec;
    private BigDecimal inPrice;
    private BigDecimal salesPrice;
    private String productVendorNo;
    private String productVendorName;
    private String productVendorNoName;
}

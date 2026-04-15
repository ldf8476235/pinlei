package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AbcSalesListItemResponse {

    private String productNo;
    private String productName;
    private String productStatus;
    private String productStatusNo;
    private Integer storeNum;
    private String currentAbc;
    private String compareAbc;
    private BigDecimal contribution;
    private BigDecimal contributionPer;
    private BigDecimal saleQuantity;
    private BigDecimal saleQuantityPsd;
    private BigDecimal sales;
    private BigDecimal salesPer;
    private BigDecimal salesPsd;
    private BigDecimal gross;
    private BigDecimal grossPer;
    private BigDecimal grossPsd;
    private BigDecimal grossRate;
    private BigDecimal stockQuantity;
    private BigDecimal turnoverRate;
    private BigDecimal turnoverDays;
    private BigDecimal stockSalesRate;
    private BigDecimal contributionRate;
    private BigDecimal gmroi;
    private BigDecimal salesRate;
    private String activity;
    private LocalDate firstSaleDate;
    private String newProduct;
    private String keyProduct;
    private String seasonableFlag;
    private String seasonableFlagName;
    private LocalDate seasonableStartDate;
    private LocalDate seasonableEndDate;
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

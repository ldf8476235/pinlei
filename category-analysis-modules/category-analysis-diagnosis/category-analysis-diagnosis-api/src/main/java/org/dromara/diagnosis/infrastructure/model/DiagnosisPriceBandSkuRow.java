package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DiagnosisPriceBandSkuRow {

    private Long id;
    private String tenantId;
    private String queryHash;
    private String dataVersion;
    private BigDecimal priceBandMin;
    private BigDecimal priceBandMax;
    private String priceBandLabel;
    private String promotionFlag;
    private String productStatus;
    private String productStatusNo;
    private String productNo;
    private String productName;
    private Integer storeNum;
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
    private BigDecimal avgDealPrice;
    private String productVendorNo;
    private String productVendorName;
    private String productVendorNoName;
    private LocalDateTime snapshotTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

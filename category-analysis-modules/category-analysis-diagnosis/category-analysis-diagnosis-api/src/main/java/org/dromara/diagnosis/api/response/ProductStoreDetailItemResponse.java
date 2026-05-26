package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProductStoreDetailItemResponse {

    private String storeNo;
    private String storeName;
    private String productStatus;
    private String productStatusNo;
    private BigDecimal saleQuantity;
    private BigDecimal sales;
    private BigDecimal gross;
    private BigDecimal grossRate;
    private BigDecimal stockQuantity;
    private BigDecimal turnoverRate;
    private BigDecimal turnoverDays;
    private BigDecimal contributionRate;
    private BigDecimal gmroi;
    private BigDecimal salesRate;
    private String activity;
    private LocalDate firstSaleDate;
}

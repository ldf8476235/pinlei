package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpecDetailItemResponse {

    private String specName;
    private String specType;
    private String specTypeName;
    private String newSpecType;
    private String newSpecTypeName;
    private Integer sku;
    private Integer skuChange;
    private BigDecimal skuInc;
    private BigDecimal skuPer;
    private BigDecimal saleQuantity;
    private BigDecimal saleQuantityChange;
    private BigDecimal saleQuantityInc;
    private BigDecimal saleQuantityPer;
    private BigDecimal saleQuantityPsd;
    private BigDecimal sales;
    private BigDecimal salesChange;
    private BigDecimal salesInc;
    private BigDecimal salesPer;
    private BigDecimal salesPsd;
    private BigDecimal gross;
    private BigDecimal grossChange;
    private BigDecimal grossInc;
    private BigDecimal grossPer;
    private BigDecimal grossPsd;
    private BigDecimal grossRate;
    private BigDecimal grossRateInc;
    private BigDecimal stockQuantity;
    private BigDecimal turnoverRate;
    private BigDecimal turnoverDays;
    private BigDecimal stockSalesRate;
    private BigDecimal contributionRate;
    private BigDecimal gmroi;
    private BigDecimal salesRate;
    private Integer activitySku;
}

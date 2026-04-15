package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceBandRangePerformanceResponse {

    private BigDecimal priceBandMin;
    private BigDecimal priceBandMax;
    private Integer sku;
    private BigDecimal sales;
    private BigDecimal saleQuantity;
    private BigDecimal priceBandAve;
    private BigDecimal salePrice;
    private BigDecimal minSalePrice;
    private BigDecimal totalSales;
    private String waveType;
}

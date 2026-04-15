package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceBandLinePerformanceResponse {

    private BigDecimal priceLine;
    private Integer sku;
    private BigDecimal sales;
}

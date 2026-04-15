package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceBandConfigItemResponse {

    private BigDecimal priceBandMin;
    private BigDecimal priceBandMax;
    private BigDecimal sales;
}

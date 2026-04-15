package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LegacyGrossSalesPerResponse {

    private BigDecimal currentSalesPer_1;
    private BigDecimal currentSalesPer_2;
    private BigDecimal currentSalesPer_3;
    private BigDecimal currentSalesPer_4;
    private BigDecimal compareSalesPer_1;
    private BigDecimal compareSalesPer_2;
    private BigDecimal compareSalesPer_3;
    private BigDecimal compareSalesPer_4;
}


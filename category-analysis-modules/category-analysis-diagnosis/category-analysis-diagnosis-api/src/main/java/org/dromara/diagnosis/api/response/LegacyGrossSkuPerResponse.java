package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LegacyGrossSkuPerResponse {

    private BigDecimal currentSkuPer_1;
    private BigDecimal currentSkuPer_2;
    private BigDecimal currentSkuPer_3;
    private Integer currentSku_3;
    private BigDecimal currentSkuPer_4;
    private BigDecimal compareSkuPer_1;
    private BigDecimal compareSkuPer_2;
    private BigDecimal compareSkuPer_3;
    private BigDecimal compareSkuPer_4;
}


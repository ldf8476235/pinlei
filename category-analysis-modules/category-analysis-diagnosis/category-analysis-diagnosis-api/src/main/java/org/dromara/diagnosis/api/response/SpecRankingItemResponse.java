package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpecRankingItemResponse {

    private String productSpec;
    private BigDecimal data;
}

package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BrandRankingItemResponse {

    private String brandNo;
    private String productBrand;
    private BigDecimal data;
}

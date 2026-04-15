package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LegacyGrossFourQuadrantItemResponse {

    private Integer contributionType;
    private String productNo;
    private String productName;
    private BigDecimal sales;
    private BigDecimal salesPer;
    private BigDecimal gross;
    private BigDecimal grossRate;
}


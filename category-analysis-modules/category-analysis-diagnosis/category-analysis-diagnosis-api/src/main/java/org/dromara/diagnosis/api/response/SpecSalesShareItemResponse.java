package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpecSalesShareItemResponse {

    private String productSpec;
    private BigDecimal sales;
    private BigDecimal salesPer;
}

package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LegacyGmroiFourQuadrantItemResponse {

    private Integer gmroiType;
    private String productNo;
    private String productName;
    private BigDecimal grossRate;
    private BigDecimal turnoverRate;
}


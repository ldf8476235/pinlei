package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TagTypeItemResponse {

    private String tagNo;
    private String tagName;
    private BigDecimal sales;
}

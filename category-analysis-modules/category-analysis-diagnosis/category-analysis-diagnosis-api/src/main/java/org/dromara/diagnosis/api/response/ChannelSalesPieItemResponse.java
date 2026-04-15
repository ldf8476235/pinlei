package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChannelSalesPieItemResponse {

    private String name;

    private BigDecimal value;

    private BigDecimal per;
}

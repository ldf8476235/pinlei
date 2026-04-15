package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChannelSalesTrendPointResponse {

    private String dataDate;

    private Integer saleChannel;

    private String onlineType;

    private String onlineName;

    private BigDecimal sales;
}

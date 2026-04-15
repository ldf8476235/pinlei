package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiagnosisSourceChannelContributionAggRow {

    private Integer saleChannel;

    private String onlineType;

    private String onlineName;

    private BigDecimal totalSales;

    private BigDecimal totalGross;

    private BigDecimal totalCustomerCount;
}

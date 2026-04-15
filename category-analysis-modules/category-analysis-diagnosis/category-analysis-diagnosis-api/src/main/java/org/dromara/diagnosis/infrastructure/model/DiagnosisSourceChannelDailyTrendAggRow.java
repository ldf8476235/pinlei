package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DiagnosisSourceChannelDailyTrendAggRow {

    private LocalDate pointDate;

    private Integer saleChannel;

    private String onlineType;

    private String onlineName;

    private BigDecimal totalSales;
}

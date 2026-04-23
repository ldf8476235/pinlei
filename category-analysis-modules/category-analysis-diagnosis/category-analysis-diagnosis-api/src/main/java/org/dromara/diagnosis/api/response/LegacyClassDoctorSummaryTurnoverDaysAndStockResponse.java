package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LegacyClassDoctorSummaryTurnoverDaysAndStockResponse {

    private String turnoverType;

    private String stockType;

    private BigDecimal turnoverDays;

    private BigDecimal turnoverDaysInc;

    private BigDecimal turnoverAveInc;

    private BigDecimal stockSales;
}

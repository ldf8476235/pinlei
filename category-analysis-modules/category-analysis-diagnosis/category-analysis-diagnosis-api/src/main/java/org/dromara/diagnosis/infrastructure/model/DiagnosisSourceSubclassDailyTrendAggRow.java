package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Raw aggregation row for subclass daily trend.
 */
@Data
public class DiagnosisSourceSubclassDailyTrendAggRow {

    private Integer subClassLevel;
    private String subClassNo;
    private String subClassName;
    private LocalDate pointDate;
    private BigDecimal totalSales;
}

package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 源数据按日趋势聚合.
 */
@Data
public class DiagnosisSourceTrendAggRow {

    private LocalDate saleDate;

    private BigDecimal totalSales;

    private BigDecimal totalGross;
}

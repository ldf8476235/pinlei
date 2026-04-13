package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 诊断按日趋势原料行.
 */
@Data
public class DiagnosisSourceDailyTrendRow {

    private LocalDate pointDate;

    private BigDecimal totalSales;

    private BigDecimal totalSaleQuantity;

    private BigDecimal totalGross;

    private BigDecimal totalSaleCost;

    private Long totalCustomerCount;

    private BigDecimal totalStockCost;
}

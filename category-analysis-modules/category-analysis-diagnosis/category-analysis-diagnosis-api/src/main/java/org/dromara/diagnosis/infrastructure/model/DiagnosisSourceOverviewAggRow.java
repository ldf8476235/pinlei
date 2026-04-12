package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 源数据概览聚合.
 */
@Data
public class DiagnosisSourceOverviewAggRow {

    private BigDecimal totalSales;

    private BigDecimal totalGross;

    private Integer totalSku;

    private Integer activeSku;
}

package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 品类业绩趋势点响应.
 */
@Data
public class DiagnosisCategoryPerformanceTrendPointResponse {

    private Integer pointIndex;

    private String dataDate;

    private BigDecimal sales;

    private BigDecimal saleQuantity;

    private BigDecimal gross;

    private BigDecimal grossRate;

    private BigDecimal customerCount;

    private BigDecimal customerPrice;

    private BigDecimal saleCost;

    private BigDecimal stockCost;

    private BigDecimal stockCostRate;

    /**
     * 当前期固定 1，对比期固定 2.
     */
    private String flag;
}

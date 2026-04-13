package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 旧版 trendChanges 单点响应.
 */
@Data
public class LegacyCategoryTrendChangesLineResponse {

    private String dataDate;

    private String dataYearMonth;

    private BigDecimal sales;

    private BigDecimal saleQuantity;

    private BigDecimal gross;

    private BigDecimal grossRate;

    private BigDecimal customerCount;

    private BigDecimal customerPrice;

    private BigDecimal stockCostRate;

    private BigDecimal stockCost;

    private BigDecimal saleCost;

    private String flag;

    private String type;
}

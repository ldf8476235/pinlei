package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 品类业绩趋势结果行.
 */
@Data
public class DiagnosisCategoryPerformanceTrendRow {

    private Long id;

    private String tenantId;

    private String queryHash;

    private String dataVersion;

    private String periodFlag;

    private Integer pointIndex;

    private LocalDate pointDate;

    private Integer classLevel;

    private String classNo;

    private String className;

    private String retailTypeId;

    private Long deptId;

    private String businessCircleId;

    private String deptGroupId;

    private String storeNo;

    private BigDecimal sales;

    private BigDecimal saleQuantity;

    private BigDecimal gross;

    private BigDecimal grossRate;

    private BigDecimal customerCount;

    private BigDecimal customerPrice;

    private BigDecimal saleCost;

    private BigDecimal stockCost;

    private BigDecimal stockCostRate;

    private LocalDateTime snapshotTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

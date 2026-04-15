package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DiagnosisCustomerContributionRow {

    private Long id;
    private String tenantId;
    private String queryHash;
    private String dataVersion;

    private Integer gender;
    private String ageBucketCode;
    private String ageBucketName;
    private Integer ageStart;
    private Integer ageEnd;
    private Integer ageOrder;

    private String retailTypeId;
    private Long deptId;
    private String businessCircleId;
    private String deptGroupId;
    private String storeNo;

    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate compareStart;
    private LocalDate compareEnd;

    private BigDecimal currentSales;
    private BigDecimal currentCustomerCount;
    private BigDecimal currentCustomerPrice;
    private BigDecimal currentUnitPrice;
    private BigDecimal currentCountAve;
    private BigDecimal currentSaleQuantity;

    private BigDecimal compareSales;
    private BigDecimal compareCustomerCount;
    private BigDecimal compareCustomerPrice;
    private BigDecimal compareUnitPrice;
    private BigDecimal compareCountAve;
    private BigDecimal compareSaleQuantity;

    private BigDecimal salesGrowth;
    private BigDecimal customerGrowth;
    private BigDecimal customerPriceGrowth;
    private BigDecimal unitPriceGrowth;
    private BigDecimal countAveGrowth;
    private BigDecimal saleQuantityGrowth;

    private LocalDateTime snapshotTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

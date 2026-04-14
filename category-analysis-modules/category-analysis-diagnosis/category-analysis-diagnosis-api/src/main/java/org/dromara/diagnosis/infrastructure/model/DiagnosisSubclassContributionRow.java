package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Snapshot row for subclass contribution summary.
 */
@Data
public class DiagnosisSubclassContributionRow {

    private Long id;
    private String tenantId;
    private String queryHash;
    private String dataVersion;
    private Integer parentClassLevel;
    private String parentClassNo;
    private Integer subClassLevel;
    private String subClassNo;
    private String subClassName;
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
    private BigDecimal currentSalesPer;
    private BigDecimal currentGross;
    private BigDecimal currentGrossPer;
    private BigDecimal currentGrossRate;
    private BigDecimal currentSaleQuantity;
    private BigDecimal currentCustomerCount;
    private BigDecimal currentCustomerPrice;
    private BigDecimal currentAvgInventory;
    private BigDecimal currentTurnoverRate;
    private BigDecimal currentTurnoverDays;
    private BigDecimal currentGmroi;
    private BigDecimal currentSaleCost;
    private Integer currentTotalSku;
    private Integer currentActiveSku;
    private BigDecimal compareSales;
    private BigDecimal compareSalesPer;
    private BigDecimal compareSalesAddRate;
    private BigDecimal compareGross;
    private BigDecimal compareGrossPer;
    private BigDecimal compareGrossAddRate;
    private BigDecimal compareGrossRate;
    private BigDecimal compareSaleQuantity;
    private BigDecimal compareSaleQuantityAddRate;
    private BigDecimal compareCustomerCount;
    private BigDecimal compareCustomerPrice;
    private BigDecimal compareCustomerPriceAddRate;
    private LocalDateTime snapshotTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

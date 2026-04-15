package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DiagnosisChannelContributionRow {

    private Long id;
    private String tenantId;
    private String queryHash;
    private String dataVersion;

    private Integer saleChannel;
    private String onlineType;
    private String onlineName;
    private String channelName;

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
    private BigDecimal currentCustomerCount;
    private BigDecimal currentCustomerPrice;

    private BigDecimal compareSales;
    private BigDecimal compareSalesPer;
    private BigDecimal compareSalesInc;
    private BigDecimal compareGross;
    private BigDecimal compareGrossPer;
    private BigDecimal compareGrossInc;
    private BigDecimal compareGrossRate;
    private BigDecimal compareCustomerCount;
    private BigDecimal compareCustomerCountInc;
    private BigDecimal compareCustomerPrice;
    private BigDecimal compareCustomerPriceInc;

    private LocalDateTime snapshotTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

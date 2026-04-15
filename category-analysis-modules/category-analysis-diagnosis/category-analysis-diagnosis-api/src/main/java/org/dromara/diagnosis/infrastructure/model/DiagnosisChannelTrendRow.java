package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DiagnosisChannelTrendRow {

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

    private Integer pointIndex;
    private LocalDate pointDate;
    private BigDecimal currentSales;

    private LocalDateTime snapshotTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

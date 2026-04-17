package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DiagnosisBrandOverviewRow {

    private Long id;
    private String tenantId;
    private String queryHash;
    private String dataVersion;
    private Integer totalNum;
    private Integer newNum;
    private Integer ownNum;
    private LocalDateTime snapshotTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

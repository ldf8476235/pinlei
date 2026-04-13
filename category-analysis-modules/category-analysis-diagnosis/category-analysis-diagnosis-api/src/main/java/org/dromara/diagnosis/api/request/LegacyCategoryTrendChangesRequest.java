package org.dromara.diagnosis.api.request;

import lombok.Data;

import java.time.LocalDate;

/**
 * 旧版 /salesStoreClass/trendChanges 请求.
 */
@Data
public class LegacyCategoryTrendChangesRequest {

    private String deptId;

    private String retailTypeId;

    private String businessCircleId;

    private String deptGroupId;

    private String storeNo;

    private Integer classLevel;

    private String classNo;

    private LocalDate currentStartDate;

    private LocalDate currentEndDate;

    private LocalDate compareStartDate;

    private LocalDate compareEndDate;

    private String tabType;
}

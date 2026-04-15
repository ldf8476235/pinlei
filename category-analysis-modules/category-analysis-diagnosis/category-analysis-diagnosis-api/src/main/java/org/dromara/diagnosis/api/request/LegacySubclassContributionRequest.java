package org.dromara.diagnosis.api.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LegacySubclassContributionRequest {

    private String sessionId;

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
}

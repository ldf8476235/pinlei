package org.dromara.diagnosis.api.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LegacyClassDoctorSummaryRequest {

    private String sessionId;

    private Long id;

    private String maxLevel;

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

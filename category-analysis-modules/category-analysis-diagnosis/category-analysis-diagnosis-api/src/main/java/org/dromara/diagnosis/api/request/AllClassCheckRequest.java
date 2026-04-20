package org.dromara.diagnosis.api.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AllClassCheckRequest {

    private String deptId;
    private String retailTypeId;
    private String businessCircleId;
    private String deptGroupId;
    private String storeNo;
    private Integer classLevel = 1;
    private List<String> classNo;
    private List<String> classRole;
    private LocalDate currentStartDate;
    private LocalDate currentEndDate;
    private LocalDate compareStartDate;
    private LocalDate compareEndDate;
}

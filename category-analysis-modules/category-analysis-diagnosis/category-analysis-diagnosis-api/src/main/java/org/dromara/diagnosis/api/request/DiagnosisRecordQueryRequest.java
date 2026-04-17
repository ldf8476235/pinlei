package org.dromara.diagnosis.api.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DiagnosisRecordQueryRequest {

    private Integer pageNum;

    private Integer pageSize;

    private String storeScope;

    private Integer classLevel;

    private List<String> classNos;

    private String deptId;

    private String retailTypeId;

    private String businessCircleId;

    private String deptGroupId;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private LocalDate compareStart;

    private LocalDate compareEnd;

    private String orderByColumn;

    private String isAsc;
}

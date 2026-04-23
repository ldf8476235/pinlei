package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.List;

@Data
public class LegacyClassDoctorSummarySonClassSalesDataResponse {

    private LegacySubclassSalesPerResponse maxClass;

    private LegacySubclassSalesPerResponse minClass;

    private List<String> top;

    private List<String> down;
}

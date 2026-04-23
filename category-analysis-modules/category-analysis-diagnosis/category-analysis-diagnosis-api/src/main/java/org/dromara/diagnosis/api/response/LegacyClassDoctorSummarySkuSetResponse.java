package org.dromara.diagnosis.api.response;

import lombok.Data;

@Data
public class LegacyClassDoctorSummarySkuSetResponse {

    private String type;

    private LegacyClassDoctorSummarySkuSetDataResponse data;
}

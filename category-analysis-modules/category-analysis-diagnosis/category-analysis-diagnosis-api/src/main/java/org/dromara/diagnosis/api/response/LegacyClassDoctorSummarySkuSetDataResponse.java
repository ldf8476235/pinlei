package org.dromara.diagnosis.api.response;

import lombok.Data;

@Data
public class LegacyClassDoctorSummarySkuSetDataResponse {

    private String roleFlag;

    private String roleNow;

    private String roleSet;

    private String skuNow;

    private String skuSet;
}

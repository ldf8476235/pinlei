package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LegacyClassDoctorSummarySingleValueResponse {

    private String type;

    private BigDecimal data;
}

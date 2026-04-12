package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.time.LocalDate;

/**
 * 可选日期范围响应.
 */
@Data
public class DiagnosisDateRangesResponse {

    private LocalDate minDate;

    private LocalDate maxDate;

    private Integer defaultRecentDays;
}


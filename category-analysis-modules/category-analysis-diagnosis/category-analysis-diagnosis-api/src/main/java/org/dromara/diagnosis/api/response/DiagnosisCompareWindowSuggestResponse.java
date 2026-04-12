package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.time.LocalDate;

/**
 * 对比窗口建议响应.
 */
@Data
public class DiagnosisCompareWindowSuggestResponse {

    private String strategy;

    private LocalDate compareStart;

    private LocalDate compareEnd;
}


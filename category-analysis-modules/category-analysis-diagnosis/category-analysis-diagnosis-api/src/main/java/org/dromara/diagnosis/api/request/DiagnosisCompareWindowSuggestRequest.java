package org.dromara.diagnosis.api.request;

import lombok.Data;

import java.time.LocalDate;

/**
 * 对比窗口建议请求.
 */
@Data
public class DiagnosisCompareWindowSuggestRequest {

    private LocalDate periodStart;

    private LocalDate periodEnd;

    /**
     * YOY / MOM / CUSTOM
     */
    private String strategy;
}


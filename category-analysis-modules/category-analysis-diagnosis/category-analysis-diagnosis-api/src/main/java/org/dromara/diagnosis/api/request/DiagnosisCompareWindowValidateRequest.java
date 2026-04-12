package org.dromara.diagnosis.api.request;

import lombok.Data;

import java.time.LocalDate;

/**
 * 对比窗口校验请求.
 */
@Data
public class DiagnosisCompareWindowValidateRequest {

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private LocalDate compareStart;

    private LocalDate compareEnd;
}


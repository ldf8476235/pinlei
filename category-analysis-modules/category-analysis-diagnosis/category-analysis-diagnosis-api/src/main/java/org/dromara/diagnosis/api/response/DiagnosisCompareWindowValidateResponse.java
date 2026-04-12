package org.dromara.diagnosis.api.response;

import lombok.Data;

/**
 * 对比窗口校验响应.
 */
@Data
public class DiagnosisCompareWindowValidateResponse {

    private Boolean valid;

    private String message;
}


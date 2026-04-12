package org.dromara.diagnosis.api;

import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.dromara.diagnosis.common.model.DiagnosisApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

/**
 * Global exception handler for diagnosis APIs.
 */
@RestControllerAdvice(basePackages = "org.dromara.diagnosis")
public class DiagnosisGlobalExceptionHandler {

    @ExceptionHandler(DiagnosisBizException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public DiagnosisApiResponse<Void> handleBizException(DiagnosisBizException ex) {
        return DiagnosisApiResponse.fail(ex.getCode(), ex.getMessage(), UUID.randomUUID().toString());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public DiagnosisApiResponse<Void> handleException(Exception ex) {
        return DiagnosisApiResponse.fail(DiagnosisErrorCode.INTERNAL_ERROR.getCode(), ex.getMessage(), UUID.randomUUID().toString());
    }
}

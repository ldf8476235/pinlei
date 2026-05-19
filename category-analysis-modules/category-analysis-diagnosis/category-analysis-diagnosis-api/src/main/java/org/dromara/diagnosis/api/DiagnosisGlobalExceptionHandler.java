package org.dromara.diagnosis.api;

import cn.dev33.satoken.exception.NotLoginException;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RestControllerAdvice(basePackages = "org.dromara.diagnosis")
public class DiagnosisGlobalExceptionHandler {

    @ExceptionHandler(DiagnosisBizException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public DiagnosisApiResponse<Void> handleBizException(DiagnosisBizException ex) {
        return DiagnosisApiResponse.fail(ex.getCode(), ex.getMessage(), UUID.randomUUID().toString());
    }

    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public DiagnosisApiResponse<Void> handleNotLoginException(NotLoginException ex) {
        String requestId = UUID.randomUUID().toString();
        log.warn("diagnosis api authentication failed, requestId={}, reason={}", requestId, ex.getType());
        return DiagnosisApiResponse.fail("401", "认证失败，无法访问系统资源", requestId);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public DiagnosisApiResponse<Void> handleException(Exception ex) {
        String requestId = UUID.randomUUID().toString();
        log.error("diagnosis api unexpected exception, requestId={}", requestId, ex);
        String message = ex.getMessage() == null ? DiagnosisErrorCode.INTERNAL_ERROR.getMessage() : ex.getMessage();
        return DiagnosisApiResponse.fail(DiagnosisErrorCode.INTERNAL_ERROR.getCode(), message, requestId);
    }
}

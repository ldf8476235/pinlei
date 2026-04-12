package org.dromara.diagnosis.common.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Unified API response contract for diagnosis module.
 */
public class DiagnosisApiResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String code;
    private String message;
    private T data;
    private String requestId;
    private LocalDateTime timestamp;

    public static <T> DiagnosisApiResponse<T> ok(T data, String requestId) {
        DiagnosisApiResponse<T> response = new DiagnosisApiResponse<>();
        response.setSuccess(true);
        response.setCode("0");
        response.setMessage("OK");
        response.setData(data);
        response.setRequestId(requestId);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    public static <T> DiagnosisApiResponse<T> fail(String code, String message, String requestId) {
        DiagnosisApiResponse<T> response = new DiagnosisApiResponse<>();
        response.setSuccess(false);
        response.setCode(code);
        response.setMessage(message);
        response.setRequestId(requestId);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

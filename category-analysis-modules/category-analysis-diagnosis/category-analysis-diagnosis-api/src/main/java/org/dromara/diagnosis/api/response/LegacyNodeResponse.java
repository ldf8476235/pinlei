package org.dromara.diagnosis.api.response;

import lombok.Data;

/**
 * 与旧 Node 接口兼容的通用响应.
 */
@Data
public class LegacyNodeResponse<T> {

    private boolean success;

    private String message;

    private int code;

    private T result;

    private long timestamp;

    public static <T> LegacyNodeResponse<T> ok(T result) {
        LegacyNodeResponse<T> response = new LegacyNodeResponse<>();
        response.setSuccess(true);
        response.setMessage("");
        response.setCode(200);
        response.setResult(result);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
}

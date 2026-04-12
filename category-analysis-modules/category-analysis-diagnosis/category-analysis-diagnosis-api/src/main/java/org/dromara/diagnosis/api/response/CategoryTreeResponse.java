package org.dromara.diagnosis.api.response;

import lombok.Data;

/**
 * 与历史 Node 接口兼容的响应结构.
 */
@Data
public class CategoryTreeResponse<T> {

    private boolean success;

    private String message;

    private int code;

    private T result;

    public static <T> CategoryTreeResponse<T> ok(T result) {
        CategoryTreeResponse<T> response = new CategoryTreeResponse<>();
        response.setSuccess(true);
        response.setMessage("");
        response.setCode(200);
        response.setResult(result);
        return response;
    }
}


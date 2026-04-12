package org.dromara.diagnosis.common.exception;

/**
 * Error codes reserved for diagnosis module.
 */
public enum DiagnosisErrorCode {
    INVALID_ARGUMENT("DIAG-400", "Invalid argument"),
    DATA_PREPARING("DIAG-425", "Data is preparing"),
    INTERNAL_ERROR("DIAG-500", "Internal error");

    private final String code;
    private final String message;

    DiagnosisErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

package org.dromara.diagnosis.common.exception;

/**
 * Business exception for diagnosis module.
 */
public class DiagnosisBizException extends RuntimeException {

    private final String code;

    public DiagnosisBizException(DiagnosisErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public DiagnosisBizException(DiagnosisErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public String getCode() {
        return code;
    }
}

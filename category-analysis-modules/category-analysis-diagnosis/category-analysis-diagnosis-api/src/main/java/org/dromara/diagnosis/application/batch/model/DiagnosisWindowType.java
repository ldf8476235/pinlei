package org.dromara.diagnosis.application.batch.model;

/**
 * 预计算窗口类型.
 */
public enum DiagnosisWindowType {

    MONTH,
    YOY;

    public static DiagnosisWindowType parse(String value) {
        if (value == null) {
            return MONTH;
        }
        try {
            return DiagnosisWindowType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return MONTH;
        }
    }
}


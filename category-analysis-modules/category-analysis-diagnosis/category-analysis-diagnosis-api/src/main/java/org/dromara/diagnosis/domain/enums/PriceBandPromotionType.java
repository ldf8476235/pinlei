package org.dromara.diagnosis.domain.enums;

import lombok.Getter;

@Getter
public enum PriceBandPromotionType {
    ALL("0"),
    YES("1"),
    NO("2");

    private final String code;

    PriceBandPromotionType(String code) {
        this.code = code;
    }

    public static boolean isValid(String code) {
        if (code == null) {
            return false;
        }
        for (PriceBandPromotionType item : values()) {
            if (item.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}

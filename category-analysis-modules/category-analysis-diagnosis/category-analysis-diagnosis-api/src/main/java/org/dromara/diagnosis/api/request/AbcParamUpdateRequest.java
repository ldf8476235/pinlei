package org.dromara.diagnosis.api.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AbcParamUpdateRequest {

    private String sessionId;
    private List<AbcParamItem> abcList;

    @Data
    public static class AbcParamItem {
        private String abcType;
        private String abcTypeName;
        private BigDecimal salesPer;
        private BigDecimal grossPer;
        private BigDecimal salesQuantityPer;
        private BigDecimal crate;
        private BigDecimal arate;
        private BigDecimal brate;
        private BigDecimal askuRate;
        private BigDecimal bskuRate;
        private BigDecimal cskuRate;
    }
}

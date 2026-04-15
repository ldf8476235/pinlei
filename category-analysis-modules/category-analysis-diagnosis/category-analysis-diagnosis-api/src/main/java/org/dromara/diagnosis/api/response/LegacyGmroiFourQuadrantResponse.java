package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class LegacyGmroiFourQuadrantResponse {

    private BigDecimal grossRate;
    private BigDecimal turnoverRate;
    private List<LegacyGmroiFourQuadrantItemResponse> list;
    private List<Integer> scaleX;
    private List<Integer> scaleY;
}


package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class LegacyGrossFourQuadrantResponse {

    private BigDecimal salesPer;
    private BigDecimal gross;
    private List<LegacyGrossFourQuadrantItemResponse> list;
    private List<Integer> scaleX;
}


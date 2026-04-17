package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BrandRankingResponse {

    private BigDecimal ave;
    private BigDecimal maxData;
    private BigDecimal minData;
    private Long total;
    private List<BrandRankingItemResponse> list;
}

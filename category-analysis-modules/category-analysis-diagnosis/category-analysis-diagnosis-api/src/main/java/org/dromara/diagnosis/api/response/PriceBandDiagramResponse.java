package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PriceBandDiagramResponse {

    private List<PriceBandRangePerformanceResponse> rangePerformanceList;
    private List<PriceBandLinePerformanceResponse> linePerformanceList;
    private List<PriceBandConfigItemResponse> priceRangeList;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private Integer priceLineNum;
    private List<PriceBandPointResponse> pricePointList;
}

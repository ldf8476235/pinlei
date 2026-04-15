package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.List;

@Data
public class PriceBandRangeSummaryResponse {

    private List<PriceBandRangeSummaryItemResponse> list;
    private List<PriceBandConfigItemResponse> summaryTwo;
    private List<PriceBandConfigItemResponse> summaryThree;
}

package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AllClassCheckScatterResponse {

    private Integer warn;
    private List<Integer> scaleY;
    private AllClassCheckScatterXYResponse xyData;
    private List<AllClassCheckScatterItemResponse> list;
}

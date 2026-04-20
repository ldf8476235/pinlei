package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AllClassCheckSkuResponse {

    private Integer warn;
    private AllClassCheckSkuYDataResponse yData;
    private List<AllClassCheckSkuItemResponse> list;
}

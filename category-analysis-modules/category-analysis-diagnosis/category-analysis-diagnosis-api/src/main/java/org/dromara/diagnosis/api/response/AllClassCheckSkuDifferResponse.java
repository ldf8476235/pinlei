package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.List;

@Data
public class AllClassCheckSkuDifferResponse {

    private Integer warn;
    private List<AllClassCheckSkuDifferItemResponse> list;
}

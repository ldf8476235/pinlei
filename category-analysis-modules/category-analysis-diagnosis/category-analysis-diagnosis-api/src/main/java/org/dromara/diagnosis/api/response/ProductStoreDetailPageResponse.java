package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.List;

@Data
public class ProductStoreDetailPageResponse {

    private List<ProductStoreDetailItemResponse> records;
    private Long total;
    private Integer size;
    private Integer current;
    private Integer pages;
}

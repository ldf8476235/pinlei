package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.List;

@Data
public class AbcSalesListResponse {

    private List<AbcSalesListItemResponse> records;
    private Long total;
    private Integer size;
    private Integer current;
    private Integer pages;
}

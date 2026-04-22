package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.List;

@Data
public class LegacyClassSalesListResponse {

    private List<LegacyClassSalesListItemResponse> content;
    private Long totalElements;
    private List<LegacyClassSalesListItemResponse> list;
    private Long total;
}

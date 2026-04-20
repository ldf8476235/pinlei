package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.List;

@Data
public class AllClassCheckSalesChangeResponse {

    private List<AllClassCheckSalesChangeItemResponse> content;
    private Long totalElements;
    private List<AllClassCheckSalesChangeItemResponse> list;
    private Long total;
}

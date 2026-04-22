package org.dromara.diagnosis.api.request;

import lombok.Data;

import java.util.List;

@Data
public class LegacyClassSalesListRequest {

    private String sessionId;
    private List<String> status;
    private String promotion;
    private Integer page;
    private Integer size;
    private String order;
    private String orderType;
}

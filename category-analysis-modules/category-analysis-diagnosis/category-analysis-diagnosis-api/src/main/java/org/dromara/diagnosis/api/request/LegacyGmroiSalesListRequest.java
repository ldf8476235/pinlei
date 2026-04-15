package org.dromara.diagnosis.api.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class LegacyGmroiSalesListRequest extends LegacyGmroiSessionRequest {

    private List<String> status;
    private String promotion;
    private String currentGmroi;
    private String compareGmroi;
    private List<String> gmroiList;
    private Integer page;
    private Integer size;
    private String order;
    private String orderType;
}


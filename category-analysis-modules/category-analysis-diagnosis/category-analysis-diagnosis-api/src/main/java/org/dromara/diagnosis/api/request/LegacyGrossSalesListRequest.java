package org.dromara.diagnosis.api.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class LegacyGrossSalesListRequest extends LegacyGrossSessionRequest {

    private List<String> status;
    private String promotion;
    private String currentGross;
    private String compareGross;
    private Integer page;
    private Integer size;
    private String order;
    private String orderType;
}


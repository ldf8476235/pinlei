package org.dromara.diagnosis.api.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AllClassCheckListRequest extends AllClassCheckRequest {

    private Integer page = 1;
    private Integer size = 10;
    private String order;
    private String orderType;
}

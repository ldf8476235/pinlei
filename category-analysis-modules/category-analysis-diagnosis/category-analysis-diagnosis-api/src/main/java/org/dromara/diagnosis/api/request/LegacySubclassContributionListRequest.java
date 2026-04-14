package org.dromara.diagnosis.api.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LegacySubclassContributionListRequest extends LegacySubclassContributionRequest {

    private Integer page;
    private Integer size;
    private String order;
    private String orderType;
}

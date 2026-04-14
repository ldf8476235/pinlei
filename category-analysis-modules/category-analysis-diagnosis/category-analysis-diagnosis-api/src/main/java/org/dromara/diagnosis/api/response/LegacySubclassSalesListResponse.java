package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.List;

@Data
public class LegacySubclassSalesListResponse {

    private List<LegacySubclassSalesListItemResponse> content;
    private Long totalElements;
    private List<LegacySubclassSalesListItemResponse> list;
    private Long total;
}

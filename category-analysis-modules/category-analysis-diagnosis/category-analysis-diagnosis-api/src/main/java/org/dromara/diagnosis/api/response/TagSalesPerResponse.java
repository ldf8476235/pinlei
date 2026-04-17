package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.List;

@Data
public class TagSalesPerResponse {

    private List<TagSalesSkuItemResponse> salesAndSkuList;
    private List<String> goodTagList;
    private List<String> badTagList;
}

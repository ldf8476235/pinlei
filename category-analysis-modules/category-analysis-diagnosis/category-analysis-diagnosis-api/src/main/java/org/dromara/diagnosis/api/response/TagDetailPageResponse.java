package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.List;

@Data
public class TagDetailPageResponse {

    private List<TagDetailItemResponse> records;
    private Long total;
    private Integer size;
    private Integer current;
    private Integer pages;
}

package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.List;

@Data
public class TagTypeGroupResponse {

    private String tagType;
    private String tagTypeName;
    private List<TagTypeItemResponse> tagList;
}

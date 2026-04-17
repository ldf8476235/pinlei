package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.List;

@Data
public class SpecFilterOptionsResponse {

    private List<SpecFilterOptionItemResponse> specList;
}

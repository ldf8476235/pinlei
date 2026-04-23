package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.List;

@Data
public class SpecFilterOptionsResponse {

    private List<SpecFilterOptionItemResponse> specList;

    private List<String> summaryOne;

    private List<String> summaryTwo;

    private List<String> summaryThree;

    private List<String> summaryFour;
}

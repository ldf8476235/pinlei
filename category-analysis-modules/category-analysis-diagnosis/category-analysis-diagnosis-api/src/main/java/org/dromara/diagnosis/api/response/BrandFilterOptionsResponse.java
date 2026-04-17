package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.List;

@Data
public class BrandFilterOptionsResponse {

    private List<BrandFilterOptionItemResponse> brandList;
    private List<String> summaryOne;
    private List<String> summaryTwo;
    private List<String> summaryThree;
    private List<String> summaryFour;
}

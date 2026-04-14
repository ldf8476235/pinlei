package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.List;

@Data
public class LegacySubclassSalesTrendResponse {

    private List<String> legend;
    private List<LegacySubclassSalesTrendPointResponse> lineDate;
    private List<String> xdata;
}

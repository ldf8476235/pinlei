package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.List;

/**
 * 旧版 trendChanges 结果体.
 */
@Data
public class LegacyCategoryTrendChangesResultResponse {

    private Boolean flag;

    private List<LegacyCategoryTrendChangesLineResponse> lineDate;

    private List<LegacyCategoryTrendChangesLineResponse> lineDateTQ;

    private List<String> xdata;

    private List<String> xdataDB;
}

package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.List;

/**
 * 品类业绩趋势响应.
 */
@Data
public class DiagnosisCategoryPerformanceTrendResponse {

    private String sessionId;

    private String dataVersion;

    private Boolean cacheHit;

    private List<String> xdata;

    private List<String> xdataDB;

    private List<DiagnosisCategoryPerformanceTrendPointResponse> lineDate;
}

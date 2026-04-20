package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AllClassCheckScatterXYResponse {

    private BigDecimal avgPointY;
    private BigDecimal yMin;
    private BigDecimal avgPointX;
    private BigDecimal yMax;
    private String yName;
    private BigDecimal xMax;
    private String xName;
    private BigDecimal xMin;
}

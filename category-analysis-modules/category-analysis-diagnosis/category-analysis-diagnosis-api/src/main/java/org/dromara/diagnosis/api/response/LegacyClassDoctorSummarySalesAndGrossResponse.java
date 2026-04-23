package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LegacyClassDoctorSummarySalesAndGrossResponse {

    private String salesType;

    private String grossType;

    private String penetrateRateType;

    private BigDecimal sales;

    private BigDecimal salesInc;

    private BigDecimal gross;

    private BigDecimal grossInc;

    private BigDecimal grossRateInc;

    private BigDecimal penetrateRate;

    private BigDecimal penetrateRateInc;

    private BigDecimal customerCountInc;

    private BigDecimal customerPriceInc;

    private BigDecimal customerAvgQuantityInc;

    private BigDecimal pieceAvgPriceInc;
}

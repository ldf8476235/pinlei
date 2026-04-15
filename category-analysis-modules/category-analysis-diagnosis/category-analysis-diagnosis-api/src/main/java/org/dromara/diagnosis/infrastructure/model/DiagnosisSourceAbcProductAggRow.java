package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DiagnosisSourceAbcProductAggRow {

    private String productNo;
    private String productBarcode;
    private String productName;
    private Integer storeNum;
    private BigDecimal saleQuantity;
    private BigDecimal sales;
    private BigDecimal gross;
    private BigDecimal saleCost;
    private String promotionFlag;
    private LocalDate firstSaleDate;
}

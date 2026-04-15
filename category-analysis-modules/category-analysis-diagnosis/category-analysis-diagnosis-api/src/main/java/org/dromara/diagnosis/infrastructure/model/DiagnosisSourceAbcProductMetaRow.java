package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DiagnosisSourceAbcProductMetaRow {

    private String productNo;
    private String productBarcode;
    private String productName;
    private String productStatus;
    private String productStatusNo;
    private String brandName;
    private String spec;
    private BigDecimal inPrice;
    private BigDecimal salesPrice;
    private String seasonableFlag;
    private String seasonableFlagName;
    private LocalDate seasonableStartDate;
    private LocalDate seasonableEndDate;
    private LocalDate firstSaleDate;
    private String classNo;
    private String className;
    private Integer classLevel;
}

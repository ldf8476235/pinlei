package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DiagnosisProductStoreDetailRow {

    private String storeNo;
    private String storeName;
    private String productStatus;
    private String productStatusNo;
    private BigDecimal saleQuantity;
    private BigDecimal sales;
    private BigDecimal gross;
    private BigDecimal saleCost;
    private BigDecimal stockQuantity;
    private String activity;
    private LocalDate firstSaleDate;
}

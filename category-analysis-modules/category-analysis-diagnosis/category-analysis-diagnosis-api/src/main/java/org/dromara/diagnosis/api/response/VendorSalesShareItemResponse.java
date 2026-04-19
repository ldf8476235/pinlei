package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VendorSalesShareItemResponse {

    private String productVendorNo;
    private String productVendorName;
    private BigDecimal sales;
    private BigDecimal salesPer;
}

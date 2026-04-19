package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VendorRankingItemResponse {

    private String productVendorNo;
    private String productVendorName;
    private BigDecimal data;
}

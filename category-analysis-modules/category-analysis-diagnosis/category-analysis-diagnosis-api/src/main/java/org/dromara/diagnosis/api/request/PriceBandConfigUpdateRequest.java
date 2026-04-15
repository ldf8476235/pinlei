package org.dromara.diagnosis.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PriceBandConfigUpdateRequest {

    @NotBlank
    private String sessionId;

    @NotEmpty
    private List<PriceRangeItem> priceRange;

    @Data
    public static class PriceRangeItem {
        private BigDecimal priceBandMin;
        private BigDecimal priceBandMax;
    }
}

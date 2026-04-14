package org.dromara.diagnosis.application.batch.model;

import lombok.Builder;
import lombok.Data;
import org.dromara.diagnosis.infrastructure.model.DiagnosisOverviewSnapshotRow;

import java.math.BigDecimal;

@Data
@Builder
public class DiagnosisOverviewFinalizeResult {

    private DiagnosisOverviewSnapshotRow overview;
    private BigDecimal sales;
    private BigDecimal gross;
    private BigDecimal saleQuantity;
    private BigDecimal salesCost;
    private BigDecimal avgInventory;
    private BigDecimal customerCount;
    private BigDecimal customerCountTotal;
    private Integer totalSku;
    private Integer activeSku;
}

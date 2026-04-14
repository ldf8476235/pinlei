package org.dromara.diagnosis.application.batch.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.application.batch.model.DiagnosisFinalizeContext;
import org.dromara.diagnosis.application.batch.model.DiagnosisOverviewFinalizeResult;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisExtendedSnapshotMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisInsightSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisRoleDistributionSnapshotRow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiagnosisExtendedSnapshotFinalizeService {

    private static final String TENANT_ID = "000000";

    private final DiagnosisExtendedSnapshotMapper extendedSnapshotMapper;
    private final DiagnosisFinalizeSupport finalizeSupport;

    public void finalizeSnapshots(DiagnosisFinalizeContext context, DiagnosisOverviewFinalizeResult overviewResult) {
        extendedSnapshotMapper.deleteRoleDistributionByVersion(TENANT_ID, context.getQueryHash(), context.getDataVersion());
        DiagnosisRoleDistributionSnapshotRow roleRow = new DiagnosisRoleDistributionSnapshotRow();
        roleRow.setTenantId(TENANT_ID);
        roleRow.setQueryHash(context.getQueryHash());
        roleRow.setRoleCode("ALL");
        roleRow.setRoleName("全部");
        roleRow.setSalesAmount(overviewResult.getSales());
        roleRow.setSkuCount(overviewResult.getTotalSku());
        roleRow.setSalesRatio(new BigDecimal("100"));
        roleRow.setDataVersion(context.getDataVersion());
        roleRow.setSnapshotTime(LocalDateTime.now());
        extendedSnapshotMapper.batchInsertRoleDistribution(List.of(roleRow));

        extendedSnapshotMapper.deleteInsightsByVersion(TENANT_ID, context.getQueryHash(), context.getDataVersion());
        DiagnosisInsightSnapshotRow insightRow = new DiagnosisInsightSnapshotRow();
        insightRow.setTenantId(TENANT_ID);
        insightRow.setQueryHash(context.getQueryHash());
        insightRow.setInsightType("SUMMARY");
        insightRow.setInsightCode("profit_margin");
        insightRow.setTitle("毛利率诊断");
        BigDecimal margin = finalizeSupport.calcMargin(overviewResult.getSales(), overviewResult.getGross());
        insightRow.setContent("当前窗口毛利率 " + margin + "%");
        insightRow.setSeverity(margin.compareTo(BigDecimal.ZERO) < 0 ? "WARN" : "INFO");
        insightRow.setSortNo(1);
        insightRow.setDataVersion(context.getDataVersion());
        insightRow.setSnapshotTime(LocalDateTime.now());
        extendedSnapshotMapper.batchInsertInsights(List.of(insightRow));
    }
}

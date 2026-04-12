package org.dromara.diagnosis.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.diagnosis.infrastructure.model.DiagnosisInsightSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisRoleDistributionSnapshotRow;

import java.util.List;

/**
 * 诊断扩展快照 Mapper（角色分布/洞察）.
 */
@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface DiagnosisExtendedSnapshotMapper {

    int deleteRoleDistributionByVersion(@Param("tenantId") String tenantId,
                                        @Param("queryHash") String queryHash,
                                        @Param("dataVersion") String dataVersion);

    int batchInsertRoleDistribution(@Param("rows") List<DiagnosisRoleDistributionSnapshotRow> rows);

    int deleteInsightsByVersion(@Param("tenantId") String tenantId,
                                @Param("queryHash") String queryHash,
                                @Param("dataVersion") String dataVersion);

    int batchInsertInsights(@Param("rows") List<DiagnosisInsightSnapshotRow> rows);
}


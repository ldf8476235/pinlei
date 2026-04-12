package org.dromara.diagnosis.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.diagnosis.infrastructure.model.DiagnosisOverviewSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisTrendSnapshotRow;

import java.util.List;

/**
 * 诊断快照 Mapper.
 */
@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface DiagnosisSnapshotMapper {

    int upsertOverviewSnapshot(@Param("row") DiagnosisOverviewSnapshotRow row);

    DiagnosisOverviewSnapshotRow selectLatestOverviewByQuery(@Param("tenantId") String tenantId,
                                                             @Param("queryHash") String queryHash);

    int deleteTrendsByVersion(@Param("tenantId") String tenantId,
                              @Param("queryHash") String queryHash,
                              @Param("dataVersion") String dataVersion);

    int batchInsertTrends(@Param("rows") List<DiagnosisTrendSnapshotRow> rows);

    List<DiagnosisTrendSnapshotRow> selectLatestTrendsByQuery(@Param("tenantId") String tenantId,
                                                              @Param("queryHash") String queryHash,
                                                              @Param("metricCode") String metricCode);
}

package org.dromara.diagnosis.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSubclassContributionRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSubclassTrendRow;

import java.util.List;

/**
 * Mapper for subclass contribution result tables.
 */
@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface DiagnosisSubclassContributionMapper {

    int deleteContributionByVersion(@Param("tenantId") String tenantId,
                                    @Param("queryHash") String queryHash,
                                    @Param("dataVersion") String dataVersion);

    int batchInsertContribution(@Param("rows") List<DiagnosisSubclassContributionRow> rows);

    List<DiagnosisSubclassContributionRow> selectContributionByVersion(@Param("tenantId") String tenantId,
                                                                       @Param("queryHash") String queryHash,
                                                                       @Param("dataVersion") String dataVersion);

    List<DiagnosisSubclassContributionRow> selectContributionPageByVersion(@Param("tenantId") String tenantId,
                                                                           @Param("queryHash") String queryHash,
                                                                           @Param("dataVersion") String dataVersion,
                                                                           @Param("orderBy") String orderBy,
                                                                           @Param("orderType") String orderType,
                                                                           @Param("offset") Integer offset,
                                                                           @Param("pageSize") Integer pageSize);

    Long countContributionByVersion(@Param("tenantId") String tenantId,
                                    @Param("queryHash") String queryHash,
                                    @Param("dataVersion") String dataVersion);

    int deleteTrendByVersion(@Param("tenantId") String tenantId,
                             @Param("queryHash") String queryHash,
                             @Param("dataVersion") String dataVersion);

    int batchInsertTrend(@Param("rows") List<DiagnosisSubclassTrendRow> rows);

    List<DiagnosisSubclassTrendRow> selectTrendByVersion(@Param("tenantId") String tenantId,
                                                         @Param("queryHash") String queryHash,
                                                         @Param("dataVersion") String dataVersion);
}

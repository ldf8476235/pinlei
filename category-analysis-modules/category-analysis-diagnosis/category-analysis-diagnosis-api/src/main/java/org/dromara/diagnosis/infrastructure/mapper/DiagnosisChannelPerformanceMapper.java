package org.dromara.diagnosis.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.diagnosis.infrastructure.model.DiagnosisChannelContributionRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisChannelTrendRow;

import java.util.List;

@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface DiagnosisChannelPerformanceMapper {

    int deleteContributionByVersion(@Param("tenantId") String tenantId,
                                    @Param("queryHash") String queryHash,
                                    @Param("dataVersion") String dataVersion);

    int batchInsertContribution(@Param("rows") List<DiagnosisChannelContributionRow> rows);

    List<DiagnosisChannelContributionRow> selectContributionByVersion(@Param("tenantId") String tenantId,
                                                                      @Param("queryHash") String queryHash,
                                                                      @Param("dataVersion") String dataVersion);

    List<DiagnosisChannelContributionRow> selectContributionPageByVersion(@Param("tenantId") String tenantId,
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

    int batchInsertTrend(@Param("rows") List<DiagnosisChannelTrendRow> rows);

    List<DiagnosisChannelTrendRow> selectTrendByVersion(@Param("tenantId") String tenantId,
                                                        @Param("queryHash") String queryHash,
                                                        @Param("dataVersion") String dataVersion);
}

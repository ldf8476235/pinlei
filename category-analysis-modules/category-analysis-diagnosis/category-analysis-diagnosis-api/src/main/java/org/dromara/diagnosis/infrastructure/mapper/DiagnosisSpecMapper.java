package org.dromara.diagnosis.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSpecJsonRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSpecMetricRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSpecOverviewRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSpecRankingItemRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSpecRankingSummaryRow;

import java.util.List;

@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface DiagnosisSpecMapper {

    int deleteByVersion(@Param("tenantId") String tenantId,
                        @Param("queryHash") String queryHash,
                        @Param("dataVersion") String dataVersion);

    int insertOverview(DiagnosisSpecOverviewRow row);

    int batchInsertMetrics(@Param("rows") List<DiagnosisSpecMetricRow> rows);

    int batchInsertJson(@Param("rows") List<DiagnosisSpecJsonRow> rows);

    DiagnosisSpecOverviewRow selectOverviewByVersion(@Param("tenantId") String tenantId,
                                                     @Param("queryHash") String queryHash,
                                                     @Param("dataVersion") String dataVersion);

    List<DiagnosisSpecMetricRow> selectMetricsByVersion(@Param("tenantId") String tenantId,
                                                        @Param("queryHash") String queryHash,
                                                        @Param("dataVersion") String dataVersion);

    DiagnosisSpecMetricRow selectMetricBySpecNo(@Param("tenantId") String tenantId,
                                                @Param("queryHash") String queryHash,
                                                @Param("dataVersion") String dataVersion,
                                                @Param("specNo") String specNo);

    List<DiagnosisSpecJsonRow> selectJsonByVersion(@Param("tenantId") String tenantId,
                                                   @Param("queryHash") String queryHash,
                                                   @Param("dataVersion") String dataVersion);

    DiagnosisSpecJsonRow selectJsonByCode(@Param("tenantId") String tenantId,
                                          @Param("queryHash") String queryHash,
                                          @Param("dataVersion") String dataVersion,
                                          @Param("payloadCode") String payloadCode);

    DiagnosisSpecRankingSummaryRow selectRankingSummaryByVersion(@Param("tenantId") String tenantId,
                                                                 @Param("queryHash") String queryHash,
                                                                 @Param("dataVersion") String dataVersion,
                                                                 @Param("metricColumn") String metricColumn);

    List<DiagnosisSpecRankingItemRow> selectRankingPageByVersion(@Param("tenantId") String tenantId,
                                                                 @Param("queryHash") String queryHash,
                                                                 @Param("dataVersion") String dataVersion,
                                                                 @Param("metricColumn") String metricColumn,
                                                                 @Param("orderType") String orderType,
                                                                 @Param("offset") int offset,
                                                                 @Param("size") int size);

    Long countMetricByVersion(@Param("tenantId") String tenantId,
                              @Param("queryHash") String queryHash,
                              @Param("dataVersion") String dataVersion,
                              @Param("specTypeList") List<String> specTypeList,
                              @Param("specList") List<String> specList,
                              @Param("newSpecType") String newSpecType);

    List<DiagnosisSpecMetricRow> selectMetricPageByVersion(@Param("tenantId") String tenantId,
                                                           @Param("queryHash") String queryHash,
                                                           @Param("dataVersion") String dataVersion,
                                                           @Param("specTypeList") List<String> specTypeList,
                                                           @Param("specList") List<String> specList,
                                                           @Param("newSpecType") String newSpecType,
                                                           @Param("orderBy") String orderBy,
                                                           @Param("orderType") String orderType,
                                                           @Param("offset") int offset,
                                                           @Param("size") int size);
}

package org.dromara.diagnosis.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.diagnosis.infrastructure.model.DiagnosisBrandJsonRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisBrandMetricRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisBrandOverviewRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisBrandRankingItemRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisBrandRankingSummaryRow;

import java.util.List;

@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface DiagnosisBrandMapper {

    int deleteByVersion(@Param("tenantId") String tenantId,
                        @Param("queryHash") String queryHash,
                        @Param("dataVersion") String dataVersion);

    int insertOverview(DiagnosisBrandOverviewRow row);

    int batchInsertMetrics(@Param("rows") List<DiagnosisBrandMetricRow> rows);

    int batchInsertJson(@Param("rows") List<DiagnosisBrandJsonRow> rows);

    DiagnosisBrandOverviewRow selectOverviewByVersion(@Param("tenantId") String tenantId,
                                                      @Param("queryHash") String queryHash,
                                                      @Param("dataVersion") String dataVersion);

    DiagnosisBrandRankingSummaryRow selectRankingSummaryByVersion(@Param("tenantId") String tenantId,
                                                                  @Param("queryHash") String queryHash,
                                                                  @Param("dataVersion") String dataVersion,
                                                                  @Param("metricColumn") String metricColumn);

    List<DiagnosisBrandRankingItemRow> selectRankingPageByVersion(@Param("tenantId") String tenantId,
                                                                  @Param("queryHash") String queryHash,
                                                                  @Param("dataVersion") String dataVersion,
                                                                  @Param("metricColumn") String metricColumn,
                                                                  @Param("orderType") String orderType,
                                                                  @Param("offset") Integer offset,
                                                                  @Param("size") Integer size);

    List<DiagnosisBrandMetricRow> selectMetricsByVersion(@Param("tenantId") String tenantId,
                                                         @Param("queryHash") String queryHash,
                                                         @Param("dataVersion") String dataVersion);

    Long countMetricByVersion(@Param("tenantId") String tenantId,
                              @Param("queryHash") String queryHash,
                              @Param("dataVersion") String dataVersion,
                              @Param("brandTypeList") List<String> brandTypeList,
                              @Param("brandList") List<String> brandList,
                              @Param("newBrandType") String newBrandType);

    List<DiagnosisBrandMetricRow> selectMetricPageByVersion(@Param("tenantId") String tenantId,
                                                            @Param("queryHash") String queryHash,
                                                            @Param("dataVersion") String dataVersion,
                                                            @Param("brandTypeList") List<String> brandTypeList,
                                                            @Param("brandList") List<String> brandList,
                                                            @Param("newBrandType") String newBrandType,
                                                            @Param("orderBy") String orderBy,
                                                            @Param("orderType") String orderType,
                                                            @Param("offset") Integer offset,
                                                            @Param("size") Integer size);

    DiagnosisBrandMetricRow selectMetricByBrandNo(@Param("tenantId") String tenantId,
                                                  @Param("queryHash") String queryHash,
                                                  @Param("dataVersion") String dataVersion,
                                                  @Param("brandNo") String brandNo);

    List<DiagnosisBrandJsonRow> selectJsonByVersion(@Param("tenantId") String tenantId,
                                                    @Param("queryHash") String queryHash,
                                                    @Param("dataVersion") String dataVersion);

    DiagnosisBrandJsonRow selectJsonByCode(@Param("tenantId") String tenantId,
                                           @Param("queryHash") String queryHash,
                                           @Param("dataVersion") String dataVersion,
                                           @Param("payloadCode") String payloadCode);
}

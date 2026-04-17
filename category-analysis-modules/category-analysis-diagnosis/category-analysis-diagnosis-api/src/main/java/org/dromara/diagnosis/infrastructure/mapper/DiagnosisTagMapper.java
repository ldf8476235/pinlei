package org.dromara.diagnosis.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.diagnosis.infrastructure.model.DiagnosisTagJsonRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisTagMetricRow;

import java.util.List;

@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface DiagnosisTagMapper {

    int deleteByVersion(@Param("tenantId") String tenantId,
                        @Param("queryHash") String queryHash,
                        @Param("dataVersion") String dataVersion);

    int batchInsertMetrics(@Param("rows") List<DiagnosisTagMetricRow> rows);

    int batchInsertJson(@Param("rows") List<DiagnosisTagJsonRow> rows);

    List<DiagnosisTagMetricRow> selectMetricsByVersion(@Param("tenantId") String tenantId,
                                                       @Param("queryHash") String queryHash,
                                                       @Param("dataVersion") String dataVersion);

    DiagnosisTagJsonRow selectJsonByCode(@Param("tenantId") String tenantId,
                                         @Param("queryHash") String queryHash,
                                         @Param("dataVersion") String dataVersion,
                                         @Param("payloadCode") String payloadCode);

    Long countMetricByVersion(@Param("tenantId") String tenantId,
                              @Param("queryHash") String queryHash,
                              @Param("dataVersion") String dataVersion,
                              @Param("tagType") String tagType,
                              @Param("tagList") List<String> tagList,
                              @Param("ignoreTagFilter") boolean ignoreTagFilter);

    List<DiagnosisTagMetricRow> selectMetricPageByVersion(@Param("tenantId") String tenantId,
                                                          @Param("queryHash") String queryHash,
                                                          @Param("dataVersion") String dataVersion,
                                                          @Param("tagType") String tagType,
                                                          @Param("tagList") List<String> tagList,
                                                          @Param("ignoreTagFilter") boolean ignoreTagFilter,
                                                          @Param("orderBy") String orderBy,
                                                          @Param("orderType") String orderType,
                                                          @Param("offset") int offset,
                                                          @Param("size") int size);
}

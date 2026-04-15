package org.dromara.diagnosis.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.diagnosis.infrastructure.model.DiagnosisGmroiSkuNumAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisGmroiSkuPerAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisGmroiSkuRow;

import java.util.List;

@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface DiagnosisGmroiContributionMapper {

    int deleteByVersion(@Param("tenantId") String tenantId,
                        @Param("queryHash") String queryHash,
                        @Param("dataVersion") String dataVersion);

    int batchInsertSku(@Param("rows") List<DiagnosisGmroiSkuRow> rows);

    List<DiagnosisGmroiSkuRow> selectQuadrantList(@Param("tenantId") String tenantId,
                                                  @Param("queryHash") String queryHash,
                                                  @Param("dataVersion") String dataVersion);

    DiagnosisGmroiSkuNumAggRow aggregateSkuNum(@Param("tenantId") String tenantId,
                                               @Param("queryHash") String queryHash,
                                               @Param("dataVersion") String dataVersion);

    DiagnosisGmroiSkuPerAggRow aggregateSkuPer(@Param("tenantId") String tenantId,
                                               @Param("queryHash") String queryHash,
                                               @Param("dataVersion") String dataVersion);

    Integer countSkuChangeFromSuccess(@Param("tenantId") String tenantId,
                                      @Param("queryHash") String queryHash,
                                      @Param("dataVersion") String dataVersion,
                                      @Param("currentRole") String currentRole);

    Long countSku(@Param("tenantId") String tenantId,
                  @Param("queryHash") String queryHash,
                  @Param("dataVersion") String dataVersion,
                  @Param("currentGmroi") String currentGmroi,
                  @Param("compareGmroi") String compareGmroi,
                  @Param("promotion") String promotion,
                  @Param("statusList") List<String> statusList,
                  @Param("gmroiList") List<String> gmroiList);

    List<DiagnosisGmroiSkuRow> selectSkuPage(@Param("tenantId") String tenantId,
                                             @Param("queryHash") String queryHash,
                                             @Param("dataVersion") String dataVersion,
                                             @Param("currentGmroi") String currentGmroi,
                                             @Param("compareGmroi") String compareGmroi,
                                             @Param("promotion") String promotion,
                                             @Param("statusList") List<String> statusList,
                                             @Param("gmroiList") List<String> gmroiList,
                                             @Param("orderBy") String orderBy,
                                             @Param("orderType") String orderType,
                                             @Param("offset") Integer offset,
                                             @Param("pageSize") Integer pageSize);
}


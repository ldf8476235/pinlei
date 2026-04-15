package org.dromara.diagnosis.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.diagnosis.infrastructure.model.DiagnosisGrossSalesPerAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisGrossSkuPerAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisGrossSkuRow;

import java.util.List;

@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface DiagnosisGrossContributionMapper {

    int deleteByVersion(@Param("tenantId") String tenantId,
                        @Param("queryHash") String queryHash,
                        @Param("dataVersion") String dataVersion);

    int batchInsertSku(@Param("rows") List<DiagnosisGrossSkuRow> rows);

    List<DiagnosisGrossSkuRow> selectQuadrantList(@Param("tenantId") String tenantId,
                                                  @Param("queryHash") String queryHash,
                                                  @Param("dataVersion") String dataVersion);

    DiagnosisGrossSalesPerAggRow aggregateSalesPer(@Param("tenantId") String tenantId,
                                                   @Param("queryHash") String queryHash,
                                                   @Param("dataVersion") String dataVersion);

    DiagnosisGrossSkuPerAggRow aggregateSkuPer(@Param("tenantId") String tenantId,
                                               @Param("queryHash") String queryHash,
                                               @Param("dataVersion") String dataVersion);

    Integer countSkuChangeFromRunner(@Param("tenantId") String tenantId,
                                     @Param("queryHash") String queryHash,
                                     @Param("dataVersion") String dataVersion,
                                     @Param("currentRole") String currentRole);

    Long countSku(@Param("tenantId") String tenantId,
                  @Param("queryHash") String queryHash,
                  @Param("dataVersion") String dataVersion,
                  @Param("currentGross") String currentGross,
                  @Param("compareGross") String compareGross,
                  @Param("promotion") String promotion,
                  @Param("statusList") List<String> statusList);

    List<DiagnosisGrossSkuRow> selectSkuPage(@Param("tenantId") String tenantId,
                                             @Param("queryHash") String queryHash,
                                             @Param("dataVersion") String dataVersion,
                                             @Param("currentGross") String currentGross,
                                             @Param("compareGross") String compareGross,
                                             @Param("promotion") String promotion,
                                             @Param("statusList") List<String> statusList,
                                             @Param("orderBy") String orderBy,
                                             @Param("orderType") String orderType,
                                             @Param("offset") Integer offset,
                                             @Param("pageSize") Integer pageSize);
}


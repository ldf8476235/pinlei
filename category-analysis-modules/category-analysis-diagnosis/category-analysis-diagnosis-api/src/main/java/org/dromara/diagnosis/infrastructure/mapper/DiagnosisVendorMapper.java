package org.dromara.diagnosis.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.diagnosis.infrastructure.model.DiagnosisVendorJsonRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisVendorMetricRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisVendorRankingItemRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisVendorRankingSummaryRow;

import java.util.List;

@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface DiagnosisVendorMapper {

    int deleteByVersion(@Param("tenantId") String tenantId,
                        @Param("queryHash") String queryHash,
                        @Param("dataVersion") String dataVersion);

    int batchInsertMetrics(@Param("rows") List<DiagnosisVendorMetricRow> rows);

    int batchInsertJson(@Param("rows") List<DiagnosisVendorJsonRow> rows);

    DiagnosisVendorRankingSummaryRow selectRankingSummaryByVersion(@Param("tenantId") String tenantId,
                                                                   @Param("queryHash") String queryHash,
                                                                   @Param("dataVersion") String dataVersion,
                                                                   @Param("metricColumn") String metricColumn);

    List<DiagnosisVendorRankingItemRow> selectRankingPageByVersion(@Param("tenantId") String tenantId,
                                                                   @Param("queryHash") String queryHash,
                                                                   @Param("dataVersion") String dataVersion,
                                                                   @Param("metricColumn") String metricColumn,
                                                                   @Param("orderType") String orderType,
                                                                   @Param("offset") Integer offset,
                                                                   @Param("size") Integer size);

    DiagnosisVendorJsonRow selectJsonByCode(@Param("tenantId") String tenantId,
                                            @Param("queryHash") String queryHash,
                                            @Param("dataVersion") String dataVersion,
                                            @Param("payloadCode") String payloadCode);
}

package org.dromara.diagnosis.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.diagnosis.infrastructure.model.DiagnosisCustomerAgeSummaryRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisCustomerContributionRow;

import java.util.List;

@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface DiagnosisCustomerAnalysisMapper {

    int deleteContributionByVersion(@Param("tenantId") String tenantId,
                                    @Param("queryHash") String queryHash,
                                    @Param("dataVersion") String dataVersion);

    int batchInsertContribution(@Param("rows") List<DiagnosisCustomerContributionRow> rows);

    List<DiagnosisCustomerContributionRow> selectContributionByVersion(@Param("tenantId") String tenantId,
                                                                       @Param("queryHash") String queryHash,
                                                                       @Param("dataVersion") String dataVersion);

    List<DiagnosisCustomerContributionRow> selectContributionPageByVersion(@Param("tenantId") String tenantId,
                                                                           @Param("queryHash") String queryHash,
                                                                           @Param("dataVersion") String dataVersion,
                                                                           @Param("orderBy") String orderBy,
                                                                           @Param("orderType") String orderType,
                                                                           @Param("offset") Integer offset,
                                                                           @Param("pageSize") Integer pageSize);

    Long countContributionByVersion(@Param("tenantId") String tenantId,
                                    @Param("queryHash") String queryHash,
                                    @Param("dataVersion") String dataVersion);

    DiagnosisCustomerAgeSummaryRow selectSummaryTotalByVersion(@Param("tenantId") String tenantId,
                                                               @Param("queryHash") String queryHash,
                                                               @Param("dataVersion") String dataVersion);

    List<DiagnosisCustomerAgeSummaryRow> selectSummaryPageByVersion(@Param("tenantId") String tenantId,
                                                                    @Param("queryHash") String queryHash,
                                                                    @Param("dataVersion") String dataVersion,
                                                                    @Param("orderBy") String orderBy,
                                                                    @Param("orderType") String orderType,
                                                                    @Param("offset") Integer offset,
                                                                    @Param("pageSize") Integer pageSize);

    Long countSummaryByVersion(@Param("tenantId") String tenantId,
                               @Param("queryHash") String queryHash,
                               @Param("dataVersion") String dataVersion);
}

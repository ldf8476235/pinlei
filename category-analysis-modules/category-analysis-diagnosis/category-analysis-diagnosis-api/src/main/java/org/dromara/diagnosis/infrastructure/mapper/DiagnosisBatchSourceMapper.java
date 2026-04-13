package org.dromara.diagnosis.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceIdRangeRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceOverviewAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceShardParam;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceTrendAggRow;

import java.math.BigDecimal;
import java.util.List;

/**
 * 诊断批处理源数据 Mapper.
 */
@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface DiagnosisBatchSourceMapper {

    DiagnosisSourceIdRangeRow selectIdRange(@Param("param") DiagnosisSourceShardParam param);

    Long countRows(@Param("param") DiagnosisSourceShardParam param);

    DiagnosisSourceOverviewAggRow aggregateOverview(@Param("param") DiagnosisSourceShardParam param);

    BigDecimal selectAvgInventory(@Param("param") DiagnosisSourceShardParam param);

    Long countCustomerByClass(@Param("param") DiagnosisSourceShardParam param);

    Long countCustomerTotal(@Param("param") DiagnosisSourceShardParam param);

    List<DiagnosisSourceTrendAggRow> aggregateTrendsByDate(@Param("param") DiagnosisSourceShardParam param);
}

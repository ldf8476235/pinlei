package org.dromara.diagnosis.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceDailyTrendRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceChannelContributionAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceChannelDailyTrendAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceIdRangeRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceOverviewAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceShardParam;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceSubclassContributionAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceSubclassDailyTrendAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceTrendAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceVipGenderAgeAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceAbcProductAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceAbcProductStockRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceAbcProductMetaRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceBrandAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceBrandMetaRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceBrandStockRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceSpecAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceSpecMetaRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceSpecStockRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceTagAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceTagMetaRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceTagStockRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisDictRow;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Mapper for batch source aggregation queries.
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
    List<DiagnosisSourceDailyTrendRow> aggregateDailySalesFacts(@Param("param") DiagnosisSourceShardParam param);
    List<DiagnosisSourceDailyTrendRow> aggregateDailyCustomerCounts(@Param("param") DiagnosisSourceShardParam param);
    List<DiagnosisSourceDailyTrendRow> aggregateDailyStockCosts(@Param("param") DiagnosisSourceShardParam param);
    List<DiagnosisSourceSubclassContributionAggRow> aggregateSubclassContribution(@Param("param") DiagnosisSourceShardParam param);
    List<DiagnosisSourceSubclassDailyTrendAggRow> aggregateSubclassDailySales(@Param("param") DiagnosisSourceShardParam param);
    List<DiagnosisSourceChannelContributionAggRow> aggregateChannelContribution(@Param("param") DiagnosisSourceShardParam param);
    List<DiagnosisSourceChannelDailyTrendAggRow> aggregateChannelDailySales(@Param("param") DiagnosisSourceShardParam param);
    List<DiagnosisSourceVipGenderAgeAggRow> aggregateVipByGenderAndAge(@Param("param") DiagnosisSourceShardParam param,
                                                                       @Param("referenceDate") LocalDate referenceDate);
    List<DiagnosisSourceAbcProductAggRow> aggregateAbcProductMetrics(@Param("param") DiagnosisSourceShardParam param);
    List<DiagnosisSourceAbcProductStockRow> aggregateAbcProductStock(@Param("param") DiagnosisSourceShardParam param,
                                                                     @Param("stockDate") LocalDate stockDate);
    List<DiagnosisSourceAbcProductMetaRow> selectAbcProductMeta(@Param("param") DiagnosisSourceShardParam param);
    List<DiagnosisSourceBrandAggRow> aggregateBrandMetrics(@Param("param") DiagnosisSourceShardParam param);
    List<DiagnosisSourceBrandStockRow> aggregateBrandStock(@Param("param") DiagnosisSourceShardParam param);
    List<DiagnosisSourceBrandMetaRow> selectBrandMeta(@Param("param") DiagnosisSourceShardParam param);
    List<DiagnosisSourceSpecAggRow> aggregateSpecMetrics(@Param("param") DiagnosisSourceShardParam param);
    List<DiagnosisSourceSpecStockRow> aggregateSpecStock(@Param("param") DiagnosisSourceShardParam param);
    List<DiagnosisSourceSpecMetaRow> selectSpecMeta(@Param("param") DiagnosisSourceShardParam param);
    List<DiagnosisSourceTagAggRow> aggregateTagMetrics(@Param("param") DiagnosisSourceShardParam param);
    List<DiagnosisSourceTagStockRow> aggregateTagStock(@Param("param") DiagnosisSourceShardParam param);
    List<DiagnosisSourceTagMetaRow> selectTagMeta(@Param("param") DiagnosisSourceShardParam param);
    List<DiagnosisDictRow> selectOnlineChannelDictRows(@Param("dictTypes") List<String> dictTypes);
}

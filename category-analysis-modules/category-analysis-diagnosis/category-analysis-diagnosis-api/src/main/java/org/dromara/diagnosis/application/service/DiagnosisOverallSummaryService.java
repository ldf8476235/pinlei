package org.dromara.diagnosis.application.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.request.LegacyClassDoctorSummaryRequest;
import org.dromara.diagnosis.api.response.AbcImageItemResponse;
import org.dromara.diagnosis.api.response.AbcMatrixResponse;
import org.dromara.diagnosis.api.response.AbcTypeParamResponse;
import org.dromara.diagnosis.api.response.DiagnosisOverallSummaryResponse;
import org.dromara.diagnosis.api.response.DiagnosisOverviewResponse;
import org.dromara.diagnosis.api.response.LegacyClassDoctorSummarySkuSetDataResponse;
import org.dromara.diagnosis.api.response.LegacyClassDoctorSummarySkuSetResponse;
import org.dromara.diagnosis.api.response.LegacyGmroiSkuChangeResponse;
import org.dromara.diagnosis.api.response.LegacyGmroiSkuPerResponse;
import org.dromara.diagnosis.api.response.LegacyGrossSkuChangeResponse;
import org.dromara.diagnosis.api.response.LegacyGrossSkuPerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DiagnosisOverallSummaryService {

    private static final Logger log = LoggerFactory.getLogger(DiagnosisOverallSummaryService.class);

    private static final BigDecimal ABC_SKU_RATE_TOLERANCE = new BigDecimal("5");
    private static final BigDecimal TURNOVER_RATE_HEALTHY_LINE = new BigDecimal("80");
    private static final BigDecimal STOCK_SALES_LOW = new BigDecimal("0.5");
    private static final BigDecimal STOCK_SALES_HIGH = new BigDecimal("2");

    private final DiagnosisSessionService diagnosisSessionService;
    private final LegacyClassDoctorSummaryService legacySummaryService;
    private final AbcStructureService abcStructureService;
    private final GrossContributionService grossContributionService;
    private final GmroiContributionService gmroiContributionService;

    public DiagnosisOverallSummaryResponse getOverallSummary(String sessionId) {
        long start = System.currentTimeMillis();
        log.info("build diagnosis overall summary start, sessionId={}", sessionId);

        DiagnosisOverviewResponse overview = diagnosisSessionService.getOverview(sessionId);
        DiagnosisOverallSummaryResponse response = new DiagnosisOverallSummaryResponse();
        response.setSessionId(sessionId);
        response.setClassNo(overview.getClassNo());
        response.setClassName(overview.getClassName());

        response.getSections().add(buildRoleSection(sessionId));
        response.getSections().add(buildSkuSection(sessionId, overview));
        response.getSections().add(buildSalesSection(overview));
        response.getSections().add(buildGrossSection(overview));
        response.getSections().add(buildTurnoverRateSection(overview));
        response.getSections().add(buildPenetrateSection(overview));
        response.getSections().add(buildTurnoverDaysSection(overview));
        response.getSections().add(buildStockSalesSection(overview));
        appendOptional(response.getSections(), () -> buildAbcSection(sessionId), sessionId, "ABC summary");
        appendOptional(response.getSections(), () -> buildGrossContributionSection(sessionId), sessionId, "gross contribution summary");
        appendOptional(response.getSections(), () -> buildGmroiSection(sessionId), sessionId, "GMROI summary");

        log.info("build diagnosis overall summary finish, sessionId={}, sectionCount={}, costMs={}",
            sessionId, response.getSections().size(), System.currentTimeMillis() - start);
        return response;
    }

    private DiagnosisOverallSummaryResponse.SummarySection buildRoleSection(String sessionId) {
        LegacyClassDoctorSummarySkuSetDataResponse data = resolveSkuSet(sessionId);
        String roleNow = roleName(data == null ? null : data.getRoleNow());
        String roleSet = roleName(data == null ? null : data.getRoleSet());
        boolean same = hasText(roleNow) && Objects.equals(roleNow, roleSet);
        DiagnosisOverallSummaryResponse.SummarySection section = section(
            "品类角色",
            same ? "NORMAL" : "ABNORMAL",
            same ? "合理" : "不合理"
        );
        if (same) {
            section.getDescriptions().add("基于本期查询日期范围下的业绩表现评估出品类角色为\"" + roleNow + "\"与预设角色一致，请继续保持。");
        } else {
            section.getDescriptions().add("基于本期查询日期范围下的业绩表现评估出品类角色为\"" + emptyToDash(roleNow)
                + "\"，与预设角色\"" + emptyToDash(roleSet) + "\"不一致，建议结合品类定位和经营目标重新确认。");
        }
        return section;
    }

    private DiagnosisOverallSummaryResponse.SummarySection buildSkuSection(String sessionId, DiagnosisOverviewResponse overview) {
        LegacyClassDoctorSummarySkuSetDataResponse data = resolveSkuSet(sessionId);
        Integer skuNow = parseInteger(data == null ? null : data.getSkuNow());
        Integer skuSet = parseInteger(data == null ? null : data.getSkuSet());
        if (skuNow == null) {
            skuNow = overview.getCurrentClassSku();
        }
        boolean same = skuSet != null && Objects.equals(nvl(skuNow), nvl(skuSet));
        DiagnosisOverallSummaryResponse.SummarySection section = section("SKU数配置", same ? "NORMAL" : "ABNORMAL", same ? "合理" : "不合理");
        if (same) {
            section.getDescriptions().add("当前品类本期在售SKU数为\"" + formatInteger(skuNow) + "\"，与预设SKU数一致，请继续保持。");
        } else {
            section.getDescriptions().add("当前品类本期在售SKU数为\"" + formatInteger(skuNow) + "\"与预设的\"" + formatInteger(skuSet)
                + "\"不一致，可结合建议淘汰商品和引品方向完成SKU的调整。");
        }
        return section;
    }

    private DiagnosisOverallSummaryResponse.SummarySection buildSalesSection(DiagnosisOverviewResponse overview) {
        BigDecimal growth = nvl(overview.getComparativeSales());
        DiagnosisOverallSummaryResponse.SummarySection section = section("销售情况", statusByGrowth(growth), salesConclusion(growth));
        section.getDescriptions().add("本期销售额为\"" + formatMoney(overview.getCurrentSales()) + "元\"，对比增长率为\"" + formatPercent(growth) + "\"。");
        section.getDescriptions().add("影响销售额的指标表现如下，本期客数对比增长率为\"" + formatPercent(overview.getComparativeCustomerCount())
            + "\"、客单价对比增长率为\"" + formatPercent(overview.getComparativeCustomerPrice())
            + "\"、客均件数对比增长率为\"" + formatPercent(overview.getComparativeCustomerAvgQuantity())
            + "\"、件均价对比增长率为\"" + formatPercent(overview.getComparativePieceAvgPrice()) + "\"。"
            + buildDriverSentence(growth, "销售额", List.of(
                driver("客数", overview.getComparativeCustomerCount()),
                driver("客均件数", overview.getComparativeCustomerAvgQuantity()),
                driver("件均价", overview.getComparativePieceAvgPrice())
            )));
        return section;
    }

    private DiagnosisOverallSummaryResponse.SummarySection buildGrossSection(DiagnosisOverviewResponse overview) {
        BigDecimal growth = nvl(overview.getComparativeGross());
        DiagnosisOverallSummaryResponse.SummarySection section = section("毛利情况", statusByGrowth(growth), salesConclusion(growth));
        section.getDescriptions().add("本期毛利额为\"" + formatMoney(overview.getCurrentGross()) + "元\"，对比增长率为\"" + formatPercent(growth) + "\"。");
        section.getDescriptions().add("影响毛利额的指标表现如下，本期毛利率对比增长率为\"" + formatPercent(overview.getComparativeGrossRate())
            + "\"、销售额对比增长率为\"" + formatPercent(overview.getComparativeSales()) + "\"。"
            + buildDriverSentence(growth, "毛利额", List.of(
                driver("毛利率", overview.getComparativeGrossRate()),
                driver("销售额", overview.getComparativeSales())
            )));
        return section;
    }

    private DiagnosisOverallSummaryResponse.SummarySection buildTurnoverRateSection(DiagnosisOverviewResponse overview) {
        BigDecimal turnoverRate = nvl(overview.getCurrentTurnoverRate());
        boolean healthy = turnoverRate.compareTo(TURNOVER_RATE_HEALTHY_LINE) >= 0;
        DiagnosisOverallSummaryResponse.SummarySection section = section("动销率", healthy ? "NORMAL" : "ABNORMAL", healthy ? "合理" : "需关注");
        if (healthy) {
            section.getDescriptions().add("本期动销率为\"" + formatPercent(turnoverRate) + "\"，处于较高水平，请继续保持。");
        } else {
            section.getDescriptions().add("本期动销率为\"" + formatPercent(turnoverRate) + "\"，低于80%的参考水平，建议关注不动销商品并结合建议淘汰商品优化。");
        }
        return section;
    }

    private DiagnosisOverallSummaryResponse.SummarySection buildPenetrateSection(DiagnosisOverviewResponse overview) {
        BigDecimal diff = nvl(overview.getComparativePenetrateRate());
        DiagnosisOverallSummaryResponse.SummarySection section = section("渗透率", statusByGrowth(diff), diff.compareTo(BigDecimal.ZERO) >= 0 ? "提升" : "下降");
        section.getDescriptions().add("本期渗透率为\"" + formatPercent(overview.getCurrentPenetrateRate()) + "\"，较对比日期"
            + directionText(diff) + "\"" + formatPercent(diff) + "\"。");
        section.getDescriptions().add("渗透率用于衡量品类覆盖到的消费客群，若持续下降，建议结合渠道、客群和引品方向补充需求覆盖。");
        return section;
    }

    private DiagnosisOverallSummaryResponse.SummarySection buildTurnoverDaysSection(DiagnosisOverviewResponse overview) {
        BigDecimal growth = nvl(overview.getComparativeTurnoverDays());
        boolean improved = growth.compareTo(BigDecimal.ZERO) <= 0;
        DiagnosisOverallSummaryResponse.SummarySection section = section("周转天数", improved ? "NORMAL" : "ABNORMAL", improved ? "优化" : "需关注");
        section.getDescriptions().add("本期库存周转天数为\"" + formatNumber(overview.getCurrentTurnoverDays()) + "\"，较对比日期"
            + directionText(growth) + "\"" + formatPercent(growth) + "\"。");
        section.getDescriptions().add("本期平均库存对比增长率为\"" + formatPercent(overview.getComparativeAvgInventory())
            + "\"，可结合库存结构和滞销商品进一步校验库存占用是否合理。");
        return section;
    }

    private DiagnosisOverallSummaryResponse.SummarySection buildStockSalesSection(DiagnosisOverviewResponse overview) {
        BigDecimal stockSales = nvl(overview.getCurrentInventorySales());
        boolean healthy = stockSales.compareTo(STOCK_SALES_LOW) >= 0 && stockSales.compareTo(STOCK_SALES_HIGH) <= 0;
        DiagnosisOverallSummaryResponse.SummarySection section = section("库销比", healthy ? "NORMAL" : "ABNORMAL", healthy ? "合理" : "需关注");
        if (healthy) {
            section.getDescriptions().add("本期库销比为\"" + formatNumber(stockSales) + "\"，当前库销比较为合理。");
        } else {
            section.getDescriptions().add("本期库销比为\"" + formatNumber(stockSales) + "\"，偏离0.5-2的参考范围，建议结合销售趋势和库存周转做结构调整。");
        }
        return section;
    }

    private DiagnosisOverallSummaryResponse.SummarySection buildAbcSection(String sessionId) {
        DiagnosisOverallSummaryResponse.SummarySection section = section("ABC结构分析", "OTHER", "结构观察");
        List<AbcTypeParamResponse> types = abcStructureService.getAbcParams(sessionId);
        if (types == null || types.isEmpty()) {
            section.getDescriptions().add("暂无ABC结构快照数据。");
            return section;
        }
        for (AbcTypeParamResponse type : types) {
            String abcType = type.getAbcType();
            List<AbcImageItemResponse> buckets = abcStructureService.getAbcImage(sessionId, abcType);
            AbcMatrixResponse matrix = abcStructureService.getAbcMatrix(sessionId, abcType);
            List<String> parts = new ArrayList<>();
            appendSkuRateJudgement(parts, findBucket(buckets, "A"), "A类");
            appendSkuRateJudgement(parts, findBucket(buckets, "B"), "B类");
            int cc = nvl(matrix.getCcNum());
            int ac = nvl(matrix.getAcNum());
            int bc = nvl(matrix.getBcNum());
            if (cc > 0) {
                parts.add("存在CC商品" + formatInteger(cc) + "个，建议将其中近似品优先淘汰");
            }
            if (ac > 0 || bc > 0) {
                parts.add("存在" + buildAcBcLabel(ac, bc) + formatInteger(ac + bc) + "个，需进一步分析确认是否需要淘汰");
            }
            if (parts.isEmpty()) {
                parts.add(type.getAbcTypeName() + "结构整体合理，请继续保持");
            }
            section.getDescriptions().add(type.getAbcTypeName() + "：" + String.join("；", parts) + "。");
        }
        return section;
    }

    private DiagnosisOverallSummaryResponse.SummarySection buildGrossContributionSection(String sessionId) {
        LegacyGrossSkuPerResponse skuPer = grossContributionService.getGrossSkuPer(sessionId);
        LegacyGrossSkuChangeResponse change = grossContributionService.getGrossSkuChange(sessionId);
        int problemSku = nvl(skuPer.getCurrentSku_3());
        BigDecimal problemPer = nvl(skuPer.getCurrentSkuPer_3());
        int downgradeSku = nvl(change.getSku_2()) + nvl(change.getSku_3()) + nvl(change.getSku_4());
        DiagnosisOverallSummaryResponse.SummarySection section = section("毛利贡献率分析", problemSku > 0 ? "ABNORMAL" : "NORMAL", problemSku > 0 ? "需关注" : "合理");
        if (problemSku > 0) {
            section.getDescriptions().add("该品类存在问题商品(低销售低毛利率)" + formatInteger(problemSku)
                + "个，占比" + formatPercent(problemPer) + "，建议重点关注。");
        } else {
            section.getDescriptions().add("该品类暂无明显问题商品(低销售低毛利率)，请继续保持当前毛利结构。");
        }
        if (downgradeSku > 0) {
            section.getDescriptions().add("存在由对比周期的第一象限降为本期较差象限的商品" + formatInteger(downgradeSku) + "个，请加以关注和分析。");
        }
        section.getDescriptions().add("点击毛利贡献率四象限名称可查看对应的商品策略。");
        return section;
    }

    private DiagnosisOverallSummaryResponse.SummarySection buildGmroiSection(String sessionId) {
        LegacyGmroiSkuPerResponse skuPer = gmroiContributionService.getGmroiSkuPer(sessionId);
        LegacyGmroiSkuChangeResponse change = gmroiContributionService.getGmroiSkuChange(sessionId);
        int problemSku = nvl(skuPer.getCurrentSku_3());
        BigDecimal problemPer = nvl(skuPer.getCurrentSkuPer_3());
        int downgradeSku = nvl(change.getSku_2()) + nvl(change.getSku_3()) + nvl(change.getSku_4());
        DiagnosisOverallSummaryResponse.SummarySection section = section("GMROI分析", problemSku > 0 ? "ABNORMAL" : "NORMAL", problemSku > 0 ? "需关注" : "合理");
        if (problemSku > 0) {
            section.getDescriptions().add("该品类存在问题商品(低毛利率低周转)" + formatInteger(problemSku)
                + "个，占比" + formatPercent(problemPer) + "，建议结合销售、毛利与库存周转综合优化。");
        } else {
            section.getDescriptions().add("该品类暂无明显低毛利率低周转商品，请继续保持。");
        }
        if (downgradeSku > 0) {
            section.getDescriptions().add("存在由对比周期的成功商品降为本期较差象限的商品" + formatInteger(downgradeSku) + "个，请加以关注和分析。");
        }
        section.getDescriptions().add("点击GMROI四象限名称可查看对应的商品策略。");
        return section;
    }

    private LegacyClassDoctorSummarySkuSetDataResponse resolveSkuSet(String sessionId) {
        LegacyClassDoctorSummaryRequest request = new LegacyClassDoctorSummaryRequest();
        request.setSessionId(sessionId);
        LegacyClassDoctorSummarySkuSetResponse response = legacySummaryService.querySkuSet(request);
        return response == null ? null : response.getData();
    }

    private void appendOptional(List<DiagnosisOverallSummaryResponse.SummarySection> sections,
                                SummarySupplier supplier,
                                String sessionId,
                                String name) {
        try {
            sections.add(supplier.get());
        } catch (Exception ex) {
            log.warn("skip optional diagnosis overall section, sessionId={}, section={}, reason={}",
                sessionId, name, ex.getMessage());
        }
    }

    private DiagnosisOverallSummaryResponse.SummarySection section(String title, String status, String conclusion) {
        DiagnosisOverallSummaryResponse.SummarySection section = new DiagnosisOverallSummaryResponse.SummarySection();
        section.setTitle(title);
        section.setStatus(status);
        section.setConclusion(conclusion);
        return section;
    }

    private void appendSkuRateJudgement(List<String> parts, AbcImageItemResponse bucket, String bucketName) {
        if (bucket == null) {
            return;
        }
        BigDecimal diff = nvl(bucket.getCurrentSkuPer()).subtract(nvl(bucket.getSetSkuPer()));
        if (diff.compareTo(ABC_SKU_RATE_TOLERANCE) > 0) {
            parts.add(bucketName + "SKU占比偏高，本期占比" + formatPercent(bucket.getCurrentSkuPer()));
        } else if (diff.compareTo(ABC_SKU_RATE_TOLERANCE.negate()) < 0) {
            parts.add(bucketName + "SKU占比偏低，本期占比" + formatPercent(bucket.getCurrentSkuPer()));
        }
    }

    private AbcImageItemResponse findBucket(List<AbcImageItemResponse> buckets, String bucket) {
        if (buckets == null) {
            return null;
        }
        for (AbcImageItemResponse item : buckets) {
            if (item != null && bucket.equalsIgnoreCase(item.getAbcType())) {
                return item;
            }
        }
        return null;
    }

    private String buildAcBcLabel(int ac, int bc) {
        if (ac > 0 && bc > 0) {
            return "AC和BC品";
        }
        if (ac > 0) {
            return "AC品";
        }
        return "BC品";
    }

    private String buildDriverSentence(BigDecimal growth, String metricName, List<Driver> drivers) {
        if (growth == null || growth.compareTo(BigDecimal.ZERO) <= 0) {
            return "";
        }
        List<String> positiveDrivers = drivers.stream()
            .filter(driver -> nvl(driver.value()).compareTo(BigDecimal.ZERO) > 0)
            .map(Driver::name)
            .toList();
        if (positiveDrivers.isEmpty()) {
            return "";
        }
        return "由此可知，本期" + String.join("、", positiveDrivers) + "对比上涨是" + metricName + "上升的主要原因。";
    }

    private Driver driver(String name, BigDecimal value) {
        return new Driver(name, value);
    }

    private String statusByGrowth(BigDecimal value) {
        return nvl(value).compareTo(BigDecimal.ZERO) >= 0 ? "NORMAL" : "ABNORMAL";
    }

    private String salesConclusion(BigDecimal value) {
        return nvl(value).compareTo(BigDecimal.ZERO) >= 0 ? "提升" : "下降";
    }

    private String directionText(BigDecimal value) {
        return nvl(value).compareTo(BigDecimal.ZERO) >= 0 ? "提升" : "下降";
    }

    private String roleName(String roleNo) {
        if (!hasText(roleNo)) {
            return "";
        }
        return switch (roleNo.trim()) {
            case "1" -> "明星品类";
            case "2" -> "幼童品类";
            case "3" -> "结构品类";
            case "4" -> "金牛品类";
            case "5" -> "战略品类";
            default -> roleNo;
        };
    }

    private Integer parseInteger(String value) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.replace(",", "").trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private int nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String formatInteger(Integer value) {
        return value == null ? "--" : String.format("%,d", value);
    }

    private String formatMoney(BigDecimal value) {
        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        return formatter.format(nvl(value).setScale(2, RoundingMode.HALF_UP));
    }

    private String formatNumber(BigDecimal value) {
        return nvl(value).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private String formatPercent(BigDecimal value) {
        return formatNumber(value) + "%";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String emptyToDash(String value) {
        return hasText(value) ? value : "--";
    }

    @FunctionalInterface
    private interface SummarySupplier {
        DiagnosisOverallSummaryResponse.SummarySection get();
    }

    private record Driver(String name, BigDecimal value) {
    }
}

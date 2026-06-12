package org.dromara.diagnosis.application.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.request.LegacyClassDoctorSummaryRequest;
import org.dromara.diagnosis.api.response.CustomerSalesRadarItemResponse;
import org.dromara.diagnosis.api.response.DiagnosisOverviewResponse;
import org.dromara.diagnosis.api.response.DiagnosisSessionStatusResponse;
import org.dromara.diagnosis.api.response.LegacyClassDoctorSummarySalesAndGrossResponse;
import org.dromara.diagnosis.api.response.LegacyClassDoctorSummarySingleValueResponse;
import org.dromara.diagnosis.api.response.LegacyClassDoctorSummarySkuSetDataResponse;
import org.dromara.diagnosis.api.response.LegacyClassDoctorSummarySkuSetResponse;
import org.dromara.diagnosis.api.response.LegacyClassDoctorSummarySonClassSalesDataResponse;
import org.dromara.diagnosis.api.response.LegacyClassDoctorSummarySonClassSalesResponse;
import org.dromara.diagnosis.api.response.LegacyClassDoctorSummaryTurnoverDaysAndStockResponse;
import org.dromara.diagnosis.api.response.LegacyClassDoctorSummaryVipSalesDataResponse;
import org.dromara.diagnosis.api.response.LegacyClassDoctorSummaryVipSalesResponse;
import org.dromara.diagnosis.api.response.LegacySubclassSalesPerResponse;
import org.dromara.diagnosis.api.response.LegacySubclassSalesTrendPointResponse;
import org.dromara.diagnosis.api.response.LegacySubclassSalesTrendResponse;
import org.dromara.diagnosis.application.config.DiagnosisCacheProperties;
import org.dromara.diagnosis.application.model.DiagnosisSessionCacheModel;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.dromara.diagnosis.infrastructure.mapper.CategoryTreeMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.model.CategorySaleSkuRow;
import org.dromara.diagnosis.infrastructure.model.CategorySkuMetricRow;
import org.dromara.diagnosis.infrastructure.model.CategoryTreeQueryParam;
import org.dromara.diagnosis.infrastructure.model.DiagnosisOverviewSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeWindowRow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LegacyClassDoctorSummaryService {

    private static final String TENANT_ID = "000000";

    private final DiagnosisSessionService diagnosisSessionService;
    private final DiagnosisSnapshotMapper snapshotMapper;
    private final DiagnosisSessionCacheStore sessionCacheStore;
    private final DiagnosisCacheProperties cacheProperties;
    private final DiagnosisPrecomputeMapper precomputeMapper;
    private final CategoryTreeMapper categoryTreeMapper;
    private final CategorySaleSkuCacheService categorySaleSkuCacheService;
    private final SubClassContributionService subClassContributionService;
    private final CustomerAnalysisService customerAnalysisService;

    public LegacyClassDoctorSummarySkuSetResponse querySkuSet(LegacyClassDoctorSummaryRequest request) {
        DiagnosisOverviewSnapshotRow overview = resolveOverviewSnapshot(requireSessionId(request));
        CategoryTreeQueryParam param = toCategoryQueryParam(overview);
        Map<String, Integer> saleSkuMap = safeSaleSkuRows(categorySaleSkuCacheService.selectCategorySaleSku(param)).stream()
            .filter(Objects::nonNull)
            .filter(v -> hasText(v.getClassNo()))
            .collect(Collectors.toMap(CategorySaleSkuRow::getClassNo, v -> nvl(v.getSaleSku()), Integer::sum));
        Map<String, CategorySkuMetricRow> metricMap = safeMetricRows(categoryTreeMapper.selectCategorySkuMetrics(param)).stream()
            .filter(Objects::nonNull)
            .filter(v -> hasText(v.getClassNo()))
            .collect(Collectors.toMap(CategorySkuMetricRow::getClassNo, v -> v, (a, b) -> a));

        LegacyClassDoctorSummarySkuSetDataResponse data = new LegacyClassDoctorSummarySkuSetDataResponse();
        data.setSkuNow(String.valueOf(saleSkuMap.getOrDefault(overview.getClassNo(), nvl(overview.getMetricActiveSku()))));
        CategorySkuMetricRow metricRow = metricMap.get(overview.getClassNo());
        data.setSkuSet(String.valueOf(metricRow == null ? 0 : nvl(metricRow.getSuggestSaleSku())));
        data.setRoleNow(metricRow == null ? null : metricRow.getRoleNo());
        data.setRoleSet(metricRow == null ? null : metricRow.getRoleNo());
        data.setRoleFlag(null);

        LegacyClassDoctorSummarySkuSetResponse response = new LegacyClassDoctorSummarySkuSetResponse();
        response.setType("2");
        response.setData(data);
        return response;
    }

    public LegacyClassDoctorSummarySalesAndGrossResponse querySalesAndGross(LegacyClassDoctorSummaryRequest request) {
        DiagnosisOverviewResponse overview = diagnosisSessionService.getOverview(requireSessionId(request));
        LegacyClassDoctorSummarySalesAndGrossResponse response = new LegacyClassDoctorSummarySalesAndGrossResponse();
        response.setSalesType(typeByGrowth(overview.getComparativeSales()));
        response.setGrossType(typeByGrowth(overview.getComparativeGross()));
        response.setPenetrateRateType(typeByDiff(overview.getComparativePenetrateRate()));
        response.setSales(scale2(overview.getCurrentSales()));
        response.setSalesInc(scale2(overview.getComparativeSales()));
        response.setGross(scale2(overview.getCurrentGross()));
        response.setGrossInc(scale2(overview.getComparativeGross()));
        response.setGrossRateInc(scale2(overview.getComparativeGrossRate()));
        response.setPenetrateRate(scale4(overview.getCurrentPenetrateRate()));
        response.setPenetrateRateInc(scale4(overview.getComparativePenetrateRate()));
        response.setCustomerCountInc(scale2(overview.getComparativeCustomerCount()));
        response.setCustomerPriceInc(scale2(overview.getComparativeCustomerPrice()));
        response.setCustomerAvgQuantityInc(scale2(overview.getComparativeCustomerAvgQuantity()));
        response.setPieceAvgPriceInc(scale2(overview.getComparativePieceAvgPrice()));
        return response;
    }

    public LegacyClassDoctorSummarySingleValueResponse querySalesTurnoverRate(LegacyClassDoctorSummaryRequest request) {
        DiagnosisOverviewResponse overview = diagnosisSessionService.getOverview(requireSessionId(request));
        LegacyClassDoctorSummarySingleValueResponse response = new LegacyClassDoctorSummarySingleValueResponse();
        response.setType(typeByGrowth(overview.getComparativeTurnoverRate()));
        response.setData(scale2(overview.getCurrentTurnoverRate()));
        return response;
    }

    public LegacyClassDoctorSummaryTurnoverDaysAndStockResponse queryTurnoverDaysAndStock(LegacyClassDoctorSummaryRequest request) {
        DiagnosisOverviewResponse overview = diagnosisSessionService.getOverview(requireSessionId(request));
        LegacyClassDoctorSummaryTurnoverDaysAndStockResponse response = new LegacyClassDoctorSummaryTurnoverDaysAndStockResponse();
        response.setTurnoverType(typeByGrowth(overview.getComparativeTurnoverDays()));
        response.setStockType(typeByGrowth(overview.getComparativeAvgInventory()));
        response.setTurnoverDays(scale2(overview.getCurrentTurnoverDays()));
        response.setTurnoverDaysInc(scale2(overview.getComparativeTurnoverDays()));
        response.setTurnoverAveInc(scale2(overview.getComparativeAvgInventory()));
        response.setStockSales(scale2(overview.getCurrentInventorySales()));
        return response;
    }

    public LegacyClassDoctorSummarySonClassSalesResponse querySonClassSales(LegacyClassDoctorSummaryRequest request) {
        String sessionId = requireSessionId(request);
        List<LegacySubclassSalesPerResponse> salesPerRows = safeList(subClassContributionService.getSalesPer(sessionId));
        LegacySubclassSalesTrendResponse trendResponse = subClassContributionService.getTrendChart(sessionId);

        LegacyClassDoctorSummarySonClassSalesDataResponse data = new LegacyClassDoctorSummarySonClassSalesDataResponse();
        data.setMaxClass(salesPerRows.stream()
            .max(Comparator.comparing(v -> nvl(v.getSalesPer())))
            .orElse(null));
        data.setMinClass(salesPerRows.stream()
            .min(Comparator.comparing(v -> nvl(v.getSalesPer())))
            .orElse(null));
        data.setTop(resolveTrendClasses(trendResponse, true));
        data.setDown(resolveTrendClasses(trendResponse, false));

        LegacyClassDoctorSummarySonClassSalesResponse response = new LegacyClassDoctorSummarySonClassSalesResponse();
        response.setType("3");
        response.setData(data);
        return response;
    }

    public LegacyClassDoctorSummaryVipSalesResponse queryVipSales(LegacyClassDoctorSummaryRequest request) {
        List<CustomerSalesRadarItemResponse> rows = safeList(customerAnalysisService.getSalesRadar(requireSessionId(request)));
        LegacyClassDoctorSummaryVipSalesDataResponse data = new LegacyClassDoctorSummaryVipSalesDataResponse();
        data.setAgeVip(resolveTopAge(rows));
        data.setSexVip(resolveTopSex(rows));
        data.setAgeAndSexVip(resolveTopAgeAndSex(rows));

        LegacyClassDoctorSummaryVipSalesResponse response = new LegacyClassDoctorSummaryVipSalesResponse();
        response.setType("3");
        response.setData(data);
        return response;
    }

    private String requireSessionId(LegacyClassDoctorSummaryRequest request) {
        if (request == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "request must not be null");
        }
        if (!hasText(request.getSessionId())) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "sessionId is required");
        }
        DiagnosisSessionStatusResponse status = diagnosisSessionService.getSessionStatus(request.getSessionId());
        if (!Boolean.TRUE.equals(status.getReady())) {
            throw new DiagnosisBizException(DiagnosisErrorCode.DATA_PREPARING, "summary snapshot is preparing");
        }
        return request.getSessionId();
    }

    private DiagnosisOverviewSnapshotRow resolveOverviewSnapshot(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        if (hasText(session.getDataVersion())) {
            DiagnosisOverviewSnapshotRow overview = snapshotMapper.selectOverviewByQueryAndVersion(
                TENANT_ID, session.getQueryHash(), session.getDataVersion());
            if (overview != null) {
                return overview;
            }
        }
        if (session.getJobId() != null) {
            DiagnosisPrecomputeJobRow job = precomputeMapper.selectJobById(TENANT_ID, session.getJobId());
            if (job != null && "SUCCESS".equalsIgnoreCase(job.getStatusCode())) {
                List<DiagnosisPrecomputeWindowRow> windows = precomputeMapper.selectWindowsByJobId(TENANT_ID, session.getJobId());
                String dataVersion = null;
                if (windows != null) {
                    for (DiagnosisPrecomputeWindowRow window : windows) {
                        if (window != null && hasText(window.getDataVersion())) {
                            dataVersion = window.getDataVersion();
                        }
                    }
                }
                if (hasText(dataVersion)) {
                    DiagnosisOverviewSnapshotRow overview = snapshotMapper.selectOverviewByQueryAndVersion(
                        TENANT_ID, session.getQueryHash(), dataVersion);
                    if (overview != null) {
                        session.setDataVersion(dataVersion);
                        sessionCacheStore.save(sessionId, session, Duration.ofMinutes(Math.max(1, cacheProperties.getResultTtlMinutes())));
                        return overview;
                    }
                }
            }
        }
        DiagnosisOverviewSnapshotRow latest = snapshotMapper.selectLatestOverviewByQuery(TENANT_ID, session.getQueryHash());
        if (latest == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "no diagnosis snapshot found for current query");
        }
        if (hasText(latest.getDataVersion())) {
            session.setDataVersion(latest.getDataVersion());
            sessionCacheStore.save(sessionId, session, Duration.ofMinutes(Math.max(1, cacheProperties.getResultTtlMinutes())));
        }
        return latest;
    }

    private DiagnosisSessionCacheModel getSession(String sessionId) {
        DiagnosisSessionCacheModel model = sessionCacheStore.get(sessionId);
        if (model == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "session not found or expired");
        }
        if (!hasText(model.getQueryHash())) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "session payload corrupted");
        }
        return model;
    }

    private CategoryTreeQueryParam toCategoryQueryParam(DiagnosisOverviewSnapshotRow overview) {
        CategoryTreeQueryParam param = new CategoryTreeQueryParam();
        param.setRetailTypeId(overview.getRetailTypeId());
        param.setBusinessCircleId(overview.getBusinessCircleId());
        param.setDeptGroupId(overview.getDeptGroupId());
        param.setStoreNo(overview.getStoreNo());
        return param;
    }

    private List<String> resolveTrendClasses(LegacySubclassSalesTrendResponse trendResponse, boolean ascending) {
        Map<String, List<LegacySubclassSalesTrendPointResponse>> grouped = new LinkedHashMap<>();
        if (trendResponse != null && trendResponse.getLineDate() != null) {
            for (LegacySubclassSalesTrendPointResponse point : trendResponse.getLineDate()) {
                if (point == null || !hasText(point.getClassNo())) {
                    continue;
                }
                grouped.computeIfAbsent(point.getClassNo() + defaultString(point.getClassName()), key -> new ArrayList<>()).add(point);
            }
        }
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, List<LegacySubclassSalesTrendPointResponse>> entry : grouped.entrySet()) {
            List<LegacySubclassSalesTrendPointResponse> points = entry.getValue();
            points.sort(Comparator.comparing(LegacySubclassSalesTrendPointResponse::getDataDate, Comparator.nullsLast(String::compareTo)));
            boolean matched = true;
            for (int i = 1; i < points.size(); i++) {
                BigDecimal prev = nvl(points.get(i - 1).getSales());
                BigDecimal curr = nvl(points.get(i).getSales());
                int cmp = curr.compareTo(prev);
                if (ascending ? cmp < 0 : cmp > 0) {
                    matched = false;
                    break;
                }
            }
            if (matched) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    private String resolveTopAge(List<CustomerSalesRadarItemResponse> rows) {
        return rows.stream()
            .filter(Objects::nonNull)
            .filter(v -> Integer.valueOf(3).equals(v.getGender()))
            .filter(v -> !"ALL".equalsIgnoreCase(defaultString(v.getAgeCode())))
            .max(Comparator.comparing(v -> nvl(v.getCurrentSales())))
            .map(CustomerSalesRadarItemResponse::getAgeName)
            .orElse(null);
    }

    private String resolveTopSex(List<CustomerSalesRadarItemResponse> rows) {
        return rows.stream()
            .filter(Objects::nonNull)
            .filter(v -> Integer.valueOf(1).equals(v.getGender()) || Integer.valueOf(2).equals(v.getGender()))
            .filter(v -> "ALL".equalsIgnoreCase(defaultString(v.getAgeCode())))
            .max(Comparator.comparing(v -> nvl(v.getCurrentSales())))
            .map(v -> Objects.equals(v.getGender(), 1) ? "男性" : "女性")
            .orElse(null);
    }

    private String resolveTopAgeAndSex(List<CustomerSalesRadarItemResponse> rows) {
        return rows.stream()
            .filter(Objects::nonNull)
            .filter(v -> Integer.valueOf(1).equals(v.getGender()) || Integer.valueOf(2).equals(v.getGender()))
            .filter(v -> !"ALL".equalsIgnoreCase(defaultString(v.getAgeCode())))
            .max(Comparator.comparing(v -> nvl(v.getCurrentSales())))
            .map(v -> v.getAgeName() + "、" + (Objects.equals(v.getGender(), 1) ? "男性" : "女性"))
            .orElse(null);
    }

    private List<CategorySaleSkuRow> safeSaleSkuRows(List<CategorySaleSkuRow> rows) {
        return rows == null ? List.of() : rows;
    }

    private List<CategorySkuMetricRow> safeMetricRows(List<CategorySkuMetricRow> rows) {
        return rows == null ? List.of() : rows;
    }

    private <T> List<T> safeList(List<T> rows) {
        return rows == null ? List.of() : rows;
    }

    private String typeByGrowth(BigDecimal value) {
        return nvl(value).compareTo(BigDecimal.ZERO) >= 0 ? "1" : "2";
    }

    private String typeByDiff(BigDecimal value) {
        return nvl(value).compareTo(BigDecimal.ZERO) >= 0 ? "1" : "2";
    }

    private Integer nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal scale2(BigDecimal value) {
        return nvl(value).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scale4(BigDecimal value) {
        return nvl(value).setScale(4, RoundingMode.HALF_UP);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}

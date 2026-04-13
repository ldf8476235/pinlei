package org.dromara.diagnosis.application.service;

import lombok.RequiredArgsConstructor;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.diagnosis.api.response.DiagnosisCategoryPerformanceTrendPointResponse;
import org.dromara.diagnosis.api.response.DiagnosisCategoryPerformanceTrendResponse;
import org.dromara.diagnosis.api.request.DiagnosisSessionCreateRequest;
import org.dromara.diagnosis.api.request.PrecomputeJobCreateRequest;
import org.dromara.diagnosis.api.response.DiagnosisOverviewResponse;
import org.dromara.diagnosis.api.response.DiagnosisSessionCreateResponse;
import org.dromara.diagnosis.api.response.DiagnosisTrendsResponse;
import org.dromara.diagnosis.application.config.DiagnosisCacheProperties;
import org.dromara.diagnosis.application.config.DiagnosisRedisKeyPrefixProperties;
import org.dromara.diagnosis.application.model.DiagnosisSessionCacheModel;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisCategoryPerformanceTrendMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisCategoryPerformanceTrendRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisOverviewSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisTrendSnapshotRow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 诊断会话服务.
 */
@Service
@RequiredArgsConstructor
public class DiagnosisSessionService {

    private static final String TENANT_ID = "000000";

    private final DiagnosisSnapshotMapper snapshotMapper;

    private final DiagnosisCategoryPerformanceTrendMapper categoryPerformanceTrendMapper;

    private final DiagnosisQueryHashService queryHashService;

    private final PrecomputeJobService precomputeJobService;

    private final DiagnosisCacheProperties cacheProperties;

    private final DiagnosisRedisKeyPrefixProperties keyPrefixProperties;

    public DiagnosisSessionCreateResponse createSession(DiagnosisSessionCreateRequest request) {
        String queryHash = queryHashService.buildQueryHash(request);
        DiagnosisOverviewSnapshotRow overview = snapshotMapper.selectLatestOverviewByQuery(TENANT_ID, queryHash);
        String sessionId = "S-" + UUID.randomUUID().toString().replace("-", "");
        Long triggeredJobId = null;
        String source = "SNAPSHOT_HIT";

        boolean triggerIfMissing = request.getTriggerIfMissing() == null || Boolean.TRUE.equals(request.getTriggerIfMissing());
        int waitSeconds = request.getWaitSeconds() == null ? 0 : Math.max(0, request.getWaitSeconds());

        if (overview == null && triggerIfMissing) {
            PrecomputeJobCreateRequest jobRequest = new PrecomputeJobCreateRequest();
            jobRequest.setModule("DIAGNOSIS");
            jobRequest.setReadRangeType("CUSTOM");
            jobRequest.setReadStart(request.getPeriodStart());
            jobRequest.setReadEnd(request.getPeriodEnd());
            jobRequest.setCompareStart(request.getCompareStart());
            jobRequest.setCompareEnd(request.getCompareEnd());
            jobRequest.setWindowTypes(request.getCompareStart() == null ? "MONTH" : "YOY");
            jobRequest.setForceRebuild(Boolean.FALSE);
            jobRequest.setPriority(5);
            jobRequest.setRequestJson(buildRequestJson(request));
            triggeredJobId = precomputeJobService.createJob(jobRequest).getJobId();
            source = "TRIGGERED";

            for (int i = 0; i < waitSeconds; i++) {
                sleepOneSecond();
                overview = snapshotMapper.selectLatestOverviewByQuery(TENANT_ID, queryHash);
                if (overview != null) {
                    source = "WAIT_READY";
                    break;
                }
            }
        }

        DiagnosisSessionCacheModel cacheModel = new DiagnosisSessionCacheModel();
        cacheModel.setSessionId(sessionId);
        cacheModel.setQueryHash(queryHash);
        cacheModel.setDataVersion(overview == null ? null : overview.getDataVersion());
        persistSession(sessionId, cacheModel);

        DiagnosisSessionCreateResponse response = new DiagnosisSessionCreateResponse();
        response.setSessionId(sessionId);
        response.setQueryHash(queryHash);
        response.setDataVersion(overview == null ? null : overview.getDataVersion());
        response.setCacheHit(overview != null);
        response.setReady(overview != null);
        response.setTriggeredJobId(triggeredJobId);
        response.setSource(source);
        return response;
    }

    public DiagnosisOverviewResponse getOverview(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        DiagnosisOverviewSnapshotRow overview = resolveOverviewSnapshot(sessionId, session);
        if (overview == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "会话对应结果不存在，请先触发预计算");
        }

        DiagnosisOverviewResponse response = new DiagnosisOverviewResponse();
        response.setClassNo(overview.getClassNo());
        response.setClassName(overview.getClassName());

        response.setCurrentClassSku(overview.getMetricTotalSku());
        response.setCompareClassSku(overview.getMetricCompareTotalSku());
        response.setComparativeGrowthRate(calcGrowthPercent(toBigDecimal(response.getCurrentClassSku()), toBigDecimal(response.getCompareClassSku())));

        response.setCurrentSales(nvl(overview.getMetricTotalSales()));
        response.setCompareSales(overview.getMetricCompareSales());
        response.setComparativeSales(calcGrowthPercent(response.getCurrentSales(), response.getCompareSales()));

        response.setCurrentGross(nvl(overview.getMetricTotalProfit()));
        response.setCompareGross(overview.getMetricCompareGross());
        response.setComparativeGross(calcGrowthPercent(response.getCurrentGross(), response.getCompareGross()));

        response.setCurrentGrossRate(overview.getMetricProfitMargin());
        response.setCompareGrossRate(calcMarginPercent(response.getCompareGross(), response.getCompareSales()));
        response.setComparativeGrossRate(calcDiff(response.getCurrentGrossRate(), response.getCompareGrossRate()));

        response.setCurrentSaleQuantity(overview.getMetricSaleQuantity());
        response.setCompareSaleQuantity(overview.getMetricCompareSaleQuantity());
        response.setComparativeSaleQuantity(calcGrowthPercent(response.getCurrentSaleQuantity(), response.getCompareSaleQuantity()));

        response.setCurrentSalesCost(overview.getMetricSalesCost());
        response.setCompareSalesCost(overview.getMetricCompareSalesCost());

        response.setCurrentCustomerCount(overview.getMetricCustomerCount());
        response.setCompareCustomerCount(overview.getMetricCompareCustomerCount());
        response.setComparativeCustomerCount(calcGrowthPercent(response.getCurrentCustomerCount(), response.getCompareCustomerCount()));

        response.setCurrentCustomerCountTotal(overview.getMetricCustomerCountTotal());
        response.setCompareCustomerCountTotal(overview.getMetricCompareCustomerCountTotal());

        response.setCurrentCustomerPrice(overview.getMetricCustomerPrice());
        response.setCompareCustomerPrice(safeDivide(response.getCompareSales(), response.getCompareCustomerCount()));
        response.setComparativeCustomerPrice(calcGrowthPercent(response.getCurrentCustomerPrice(), response.getCompareCustomerPrice()));

        response.setCurrentCustomerAvgQuantity(overview.getMetricCustomerAvgQuantity());
        response.setCompareCustomerAvgQuantity(safeDivide(response.getCompareSaleQuantity(), response.getCompareCustomerCount()));
        response.setComparativeCustomerAvgQuantity(calcGrowthPercent(response.getCurrentCustomerAvgQuantity(), response.getCompareCustomerAvgQuantity()));

        response.setCurrentPieceAvgPrice(overview.getMetricPieceAvgPrice());
        response.setComparePieceAvgPrice(safeDivide(response.getCompareSales(), response.getCompareSaleQuantity()));
        response.setComparativePieceAvgPrice(calcGrowthPercent(response.getCurrentPieceAvgPrice(), response.getComparePieceAvgPrice()));

        response.setCurrentAvgInventory(overview.getMetricAvgInventory());
        response.setCompareAvgInventory(overview.getMetricCompareAvgInventory());
        response.setComparativeAvgInventory(calcGrowthPercent(response.getCurrentAvgInventory(), response.getCompareAvgInventory()));

        response.setCurrentInventorySales(overview.getMetricInventorySalesRatio());
        response.setCompareInventorySales(overview.getMetricCompareInventorySalesRatio());
        response.setComparativeInventorySales(calcGrowthPercent(response.getCurrentInventorySales(), response.getCompareInventorySales()));

        response.setCurrentTurnoverDays(overview.getMetricInventoryTurnoverDays());
        response.setCompareTurnoverDays(overview.getMetricCompareInventoryTurnoverDays());
        response.setComparativeTurnoverDays(calcGrowthPercent(response.getCurrentTurnoverDays(), response.getCompareTurnoverDays()));

        response.setCurrentPenetrateRate(overview.getMetricPenetrateRate());
        response.setComparePenetrateRate(overview.getMetricComparePenetrateRate());
        response.setComparativePenetrateRate(calcDiff(response.getCurrentPenetrateRate(), response.getComparePenetrateRate()));

        response.setCurrentTurnoverRate(overview.getMetricSalesRate());
        response.setCompareTurnoverRate(overview.getMetricCompareSalesRate());
        response.setComparativeTurnoverRate(calcGrowthPercent(response.getCurrentTurnoverRate(), response.getCompareTurnoverRate()));

        return response;
    }

    public DiagnosisTrendsResponse getTrends(String sessionId, String metricCode) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        DiagnosisOverviewSnapshotRow overview = resolveOverviewSnapshot(sessionId, session);
        if (overview == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "会话对应结果不存在，请先触发预计算");
        }

        List<DiagnosisTrendSnapshotRow> trends = snapshotMapper.selectTrendsByQueryAndVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), metricCode);

        DiagnosisTrendsResponse response = new DiagnosisTrendsResponse();
        response.setSessionId(sessionId);
        response.setMetricCode(metricCode);
        response.setDataVersion(session.getDataVersion());
        response.setCacheHit(Boolean.TRUE);
        response.setTrends(trends);
        return response;
    }

    public DiagnosisCategoryPerformanceTrendResponse getTrendChanges(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        DiagnosisOverviewSnapshotRow overview = resolveOverviewSnapshot(sessionId, session);
        if (overview == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "会话对应结果不存在，请先触发预计算");
        }

        List<DiagnosisCategoryPerformanceTrendRow> rows = categoryPerformanceTrendMapper.selectByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion());

        DiagnosisCategoryPerformanceTrendResponse response = new DiagnosisCategoryPerformanceTrendResponse();
        response.setSessionId(sessionId);
        response.setDataVersion(session.getDataVersion());
        response.setCacheHit(Boolean.TRUE);
        response.setXdata(extractDates(rows, "1"));
        response.setXdataDB(extractDates(rows, "2"));
        response.setLineDate(mapTrendPoints(rows));
        return response;
    }

    private DiagnosisSessionCacheModel getSession(String sessionId) {
        String text = RedisUtils.getCacheObject(sessionKey(sessionId));
        if (text == null || text.isBlank()) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "会话不存在或已过期");
        }
        DiagnosisSessionCacheModel model = JsonUtils.parseObject(text, DiagnosisSessionCacheModel.class);
        if (model == null || model.getQueryHash() == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "会话数据损坏");
        }
        return model;
    }

    private DiagnosisOverviewSnapshotRow resolveOverviewSnapshot(String sessionId, DiagnosisSessionCacheModel session) {
        if (hasText(session.getDataVersion())) {
            return snapshotMapper.selectOverviewByQueryAndVersion(
                TENANT_ID, session.getQueryHash(), session.getDataVersion());
        }

        DiagnosisOverviewSnapshotRow overview = snapshotMapper.selectLatestOverviewByQuery(TENANT_ID, session.getQueryHash());
        if (overview == null) {
            return null;
        }

        if (hasText(overview.getDataVersion())) {
            session.setDataVersion(overview.getDataVersion());
            persistSession(sessionId, session);
        }
        return overview;
    }

    private void persistSession(String sessionId, DiagnosisSessionCacheModel session) {
        RedisUtils.setCacheObject(sessionKey(sessionId), JsonUtils.toJsonString(session),
            Duration.ofMinutes(Math.max(1, cacheProperties.getResultTtlMinutes())));
    }

    private void sleepOneSecond() {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String sessionKey(String sessionId) {
        return keyPrefixProperties.getResultQuery() + ":session:" + sessionId;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal calcGrowthPercent(BigDecimal current, BigDecimal compare) {
        if (current == null || compare == null || compare.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return current.subtract(compare)
            .multiply(new BigDecimal("100"))
            .divide(compare, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calcMarginPercent(BigDecimal gross, BigDecimal sales) {
        if (gross == null || sales == null || sales.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return gross.multiply(new BigDecimal("100"))
            .divide(sales, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calcDiff(BigDecimal current, BigDecimal compare) {
        if (current == null || compare == null) {
            return null;
        }
        return current.subtract(compare);
    }

    private BigDecimal toBigDecimal(Integer value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private BigDecimal safeDivide(BigDecimal dividend, BigDecimal divisor) {
        if (dividend == null || divisor == null || divisor.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return dividend.divide(divisor, 6, RoundingMode.HALF_UP);
    }

    private String buildRequestJson(DiagnosisSessionCreateRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("classLevel", request.getClassLevel());
        payload.put("classNo", request.getClassNo());
        payload.put("className", request.getClassName());
        payload.put("deptId", request.getDeptId());
        payload.put("retailTypeId", request.getRetailTypeId());
        payload.put("businessCircleId", request.getBusinessCircleId());
        payload.put("deptGroupId", request.getDeptGroupId());
        payload.put("storeNo", request.getStoreNo());
        payload.put("extraFilterJson", request.getExtraFilterJson());
        return JsonUtils.toJsonString(payload);
    }

    private List<String> extractDates(List<DiagnosisCategoryPerformanceTrendRow> rows, String periodFlag) {
        List<String> dates = new ArrayList<>();
        if (rows == null) {
            return dates;
        }
        for (DiagnosisCategoryPerformanceTrendRow row : rows) {
            if (row == null || !periodFlag.equals(row.getPeriodFlag()) || row.getPointDate() == null) {
                continue;
            }
            dates.add(row.getPointDate().toString());
        }
        return dates;
    }

    private List<DiagnosisCategoryPerformanceTrendPointResponse> mapTrendPoints(List<DiagnosisCategoryPerformanceTrendRow> rows) {
        List<DiagnosisCategoryPerformanceTrendPointResponse> points = new ArrayList<>();
        if (rows == null) {
            return points;
        }
        for (DiagnosisCategoryPerformanceTrendRow row : rows) {
            if (row == null) {
                continue;
            }
            DiagnosisCategoryPerformanceTrendPointResponse point = new DiagnosisCategoryPerformanceTrendPointResponse();
            point.setPointIndex(row.getPointIndex());
            point.setDataDate(row.getPointDate() == null ? null : row.getPointDate().toString());
            point.setSales(row.getSales());
            point.setSaleQuantity(row.getSaleQuantity());
            point.setGross(row.getGross());
            point.setGrossRate(row.getGrossRate());
            point.setCustomerCount(row.getCustomerCount());
            point.setCustomerPrice(row.getCustomerPrice());
            point.setSaleCost(row.getSaleCost());
            point.setStockCost(row.getStockCost());
            point.setStockCostRate(row.getStockCostRate());
            point.setFlag(row.getPeriodFlag());
            points.add(point);
        }
        return points;
    }
}

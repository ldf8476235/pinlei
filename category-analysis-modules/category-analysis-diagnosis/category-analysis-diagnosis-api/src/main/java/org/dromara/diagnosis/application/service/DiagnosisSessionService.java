package org.dromara.diagnosis.application.service;

import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dromara.diagnosis.api.response.DiagnosisCategoryPerformanceTrendPointResponse;
import org.dromara.diagnosis.api.response.DiagnosisCategoryPerformanceTrendResponse;
import org.dromara.diagnosis.api.request.DiagnosisSessionCreateRequest;
import org.dromara.diagnosis.api.request.PrecomputeJobCreateRequest;
import org.dromara.diagnosis.api.response.DiagnosisOverviewResponse;
import org.dromara.diagnosis.api.response.DiagnosisSessionCreateResponse;
import org.dromara.diagnosis.api.response.DiagnosisSessionStatusResponse;
import org.dromara.diagnosis.api.response.DiagnosisTrendsResponse;
import org.dromara.diagnosis.api.response.PrecomputeJobProgressResponse;
import org.dromara.diagnosis.api.response.PrecomputeJobResponse;
import org.dromara.diagnosis.application.config.DiagnosisCacheProperties;
import org.dromara.diagnosis.application.model.DiagnosisSessionCacheModel;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisCategoryPerformanceTrendMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisCategoryPerformanceTrendRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisOverviewSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeWindowRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisTrendSnapshotRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 闁荤姴娲ら敃銉╁蓟閸ャ劌顕辨慨妯虹－濡牓鏌￠崼婵埿㈠┑?
 */
@Service
@RequiredArgsConstructor
public class DiagnosisSessionService {

    private static final Logger log = LoggerFactory.getLogger(DiagnosisSessionService.class);

    private static final String TENANT_ID = "000000";
    private static final ObjectMapper LOCAL_JSON_MAPPER = new ObjectMapper();
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_RETRYING = "RETRYING";

    private final DiagnosisSnapshotMapper snapshotMapper;

    private final DiagnosisCategoryPerformanceTrendMapper categoryPerformanceTrendMapper;

    private final DiagnosisQueryHashService queryHashService;

    private final PrecomputeJobService precomputeJobService;

    private final DiagnosisPrecomputeMapper precomputeMapper;

    private final DiagnosisCacheProperties cacheProperties;

    private final DiagnosisSessionCacheStore sessionCacheStore;

    public DiagnosisSessionCreateResponse createSession(DiagnosisSessionCreateRequest request) {
        String queryHash = queryHashService.buildQueryHash(request);
        DiagnosisOverviewSnapshotRow overview = snapshotMapper.selectLatestOverviewByQuery(TENANT_ID, queryHash);
        DiagnosisSessionCacheModel existing = sessionCacheStore.getByQueryHash(queryHash);
        if (existing != null && hasText(existing.getSessionId()) && overview != null) {
            existing.setDataVersion(overview.getDataVersion());
            sessionCacheStore.save(existing.getSessionId(), existing, sessionTtl());
            return buildReadyResponse(existing, overview.getDataVersion(), "REUSED_READY_SESSION");
        }
        if (overview == null && isReusableExistingSession(existing)) {
            return buildResponseFromExistingSession(existing);
        }

        String sessionId = "S-" + UUID.randomUUID().toString().replace("-", "");
        Long triggeredJobId = null;
        PrecomputeJobResponse triggeredJob = null;
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
            triggeredJob = precomputeJobService.createJob(jobRequest);
            triggeredJobId = triggeredJob.getJobId();
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
        if (overview == null && !triggerIfMissing) {
            source = "MISS_NO_TRIGGER";
        }

        DiagnosisSessionCacheModel cacheModel = new DiagnosisSessionCacheModel();
        cacheModel.setSessionId(sessionId);
        cacheModel.setQueryHash(queryHash);
        cacheModel.setDataVersion(overview == null ? null : overview.getDataVersion());
        cacheModel.setJobId(triggeredJobId);
        sessionCacheStore.save(sessionId, cacheModel, sessionTtl());
        if (overview != null) {
            return buildReadyResponse(cacheModel, overview.getDataVersion(), source);
        }

        DiagnosisSessionCreateResponse response = new DiagnosisSessionCreateResponse();
        response.setSessionId(sessionId);
        response.setQueryHash(queryHash);
        response.setDataVersion(null);
        response.setCacheHit(Boolean.FALSE);
        response.setReady(Boolean.FALSE);
        response.setTriggeredJobId(triggeredJobId);
        response.setStatus(triggeredJob == null ? "PENDING" : triggeredJob.getStatus());
        response.setOrchestratorStatus(triggeredJob == null ? "PENDING" : (hasText(triggeredJob.getOrchestratorStatus())
            ? triggeredJob.getOrchestratorStatus()
            : triggeredJob.getStatus()));
        response.setSource(source);
        return response;
    }

    private DiagnosisSessionCreateResponse buildResponseFromExistingSession(DiagnosisSessionCacheModel existing) {
        DiagnosisSessionCreateResponse response = new DiagnosisSessionCreateResponse();
        response.setSessionId(existing.getSessionId());
        response.setQueryHash(existing.getQueryHash());
        response.setDataVersion(existing.getDataVersion());
        response.setCacheHit(Boolean.FALSE);
        response.setReady(Boolean.FALSE);
        response.setTriggeredJobId(existing.getJobId());
        response.setSource("REUSED_ACTIVE_SESSION");
        try {
            PrecomputeJobProgressResponse progress = precomputeJobService.getJobProgress(existing.getJobId());
            response.setStatus(progress.getStatus());
            response.setOrchestratorStatus(hasText(progress.getOrchestratorStatus())
                ? progress.getOrchestratorStatus()
                : progress.getStatus());
        } catch (Exception ex) {
            response.setStatus("RUNNING");
            response.setOrchestratorStatus("RUNNING");
        }
        return response;
    }

    private boolean isReusableExistingSession(DiagnosisSessionCacheModel existing) {
        if (existing == null || !hasText(existing.getSessionId()) || existing.getJobId() == null) {
            return false;
        }
        DiagnosisPrecomputeJobRow job = precomputeMapper.selectJobById(TENANT_ID, existing.getJobId());
        if (job == null || !isActiveJobStatus(job.getStatusCode())) {
            log.info("skip diagnosis cached session reuse, sessionId={}, jobId={}, status={}",
                existing.getSessionId(), existing.getJobId(), job == null ? null : job.getStatusCode());
            return false;
        }
        Long executionCount = precomputeMapper.countBatchExecutionsByJobId(existing.getJobId());
        if (executionCount != null && executionCount > 0) {
            return true;
        }
        LocalDateTime startTime = job.getStartedTime() == null ? job.getSubmittedTime() : job.getStartedTime();
        boolean freshPendingJob = startTime != null && !startTime.isBefore(LocalDateTime.now().minusMinutes(1));
        if (!freshPendingJob) {
            log.warn("skip diagnosis cached session reuse for zombie active job, sessionId={}, jobId={}, status={}",
                existing.getSessionId(), existing.getJobId(), job.getStatusCode());
        }
        return freshPendingJob;
    }

    private boolean isActiveJobStatus(String status) {
        return STATUS_PENDING.equalsIgnoreCase(status)
            || STATUS_RUNNING.equalsIgnoreCase(status)
            || STATUS_RETRYING.equalsIgnoreCase(status);
    }

    private DiagnosisSessionCreateResponse buildReadyResponse(DiagnosisSessionCacheModel session,
                                                              String dataVersion,
                                                              String source) {
        DiagnosisSessionCreateResponse response = new DiagnosisSessionCreateResponse();
        response.setSessionId(session.getSessionId());
        response.setQueryHash(session.getQueryHash());
        response.setDataVersion(dataVersion);
        response.setCacheHit(Boolean.TRUE);
        response.setReady(Boolean.TRUE);
        response.setTriggeredJobId(session.getJobId());
        response.setStatus("SUCCESS");
        response.setOrchestratorStatus("SUCCESS");
        response.setSource(source);
        return response;
    }

    public DiagnosisSessionStatusResponse getSessionStatus(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        renewSession(sessionId, session);
        DiagnosisOverviewSnapshotRow overview;
        try {
            overview = resolveOverviewSnapshot(sessionId, session);
        } catch (DiagnosisBizException ex) {
            overview = null;
        }

        DiagnosisSessionStatusResponse response = new DiagnosisSessionStatusResponse();
        response.setSessionId(sessionId);
        response.setQueryHash(session.getQueryHash());
        response.setDataVersion(session.getDataVersion());
        response.setJobId(session.getJobId());

        if (overview != null && hasText(overview.getDataVersion())) {
            response.setReady(Boolean.TRUE);
            response.setStatus("SUCCESS");
            response.setOrchestratorStatus("SUCCESS");
            response.setProgressPercent(new BigDecimal("100"));
            response.setCurrentStage("DONE");
            return response;
        }
        if (session.getJobId() == null) {
            response.setReady(Boolean.FALSE);
            response.setStatus("PENDING");
            response.setOrchestratorStatus("PENDING");
            response.setProgressPercent(BigDecimal.ZERO);
            response.setCurrentStage("WAIT_PRECOMPUTE");
            return response;
        }

        PrecomputeJobProgressResponse progress = precomputeJobService.getJobProgress(session.getJobId());
        response.setReady(Boolean.FALSE);
        response.setStatus(progress.getStatus());
        response.setOrchestratorStatus(progress.getOrchestratorStatus());
        response.setProgressPercent(progress.getProgressPercent());
        response.setCurrentStage(progress.getCurrentStage());
        response.setModuleProgressJson(progress.getModuleProgressJson());
        return response;
    }

    public DiagnosisOverviewResponse getOverview(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        DiagnosisOverviewSnapshotRow overview = resolveOverviewSnapshot(sessionId, session);
        if (overview == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "no diagnosis snapshot found for current query; trigger precompute first");
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

    public DiagnosisTrendsResponse getTrends(String sessionId, String metricCode, String tabType) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        DiagnosisOverviewSnapshotRow overview = resolveOverviewSnapshot(sessionId, session);
        if (overview == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "no diagnosis snapshot found for current query; trigger precompute first");
        }

        String actualMetricCode = resolveTrendMetricCode(metricCode, tabType);
        List<DiagnosisTrendSnapshotRow> trends = snapshotMapper.selectTrendsByQueryAndVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), actualMetricCode);

        DiagnosisTrendsResponse response = new DiagnosisTrendsResponse();
        response.setSessionId(sessionId);
        response.setMetricCode(actualMetricCode);
        response.setDataVersion(session.getDataVersion());
        response.setCacheHit(Boolean.TRUE);
        response.setTrends(trends);
        return response;
    }

    public DiagnosisCategoryPerformanceTrendResponse getTrendChanges(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        DiagnosisOverviewSnapshotRow overview = resolveOverviewSnapshot(sessionId, session);
        if (overview == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "no diagnosis snapshot found for current query; trigger precompute first");
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
        DiagnosisSessionCacheModel model = sessionCacheStore.get(sessionId);
        if (model == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "session not found or expired");
        }
        if (model.getQueryHash() == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "session payload corrupted");
        }
        return model;
    }

    private DiagnosisOverviewSnapshotRow resolveOverviewSnapshot(String sessionId, DiagnosisSessionCacheModel session) {
        if (hasText(session.getDataVersion())) {
            DiagnosisOverviewSnapshotRow overview = snapshotMapper.selectOverviewByQueryAndVersion(
                TENANT_ID, session.getQueryHash(), session.getDataVersion());
            if (overview != null) {
                return overview;
            }
        }

        if (session.getJobId() != null) {
            DiagnosisPrecomputeJobRow job = precomputeMapper.selectJobById(TENANT_ID, session.getJobId());
            if (job == null || !"SUCCESS".equalsIgnoreCase(job.getStatusCode())) {
                return null;
            }
            List<DiagnosisPrecomputeWindowRow> windows = precomputeMapper.selectWindowsByJobId(TENANT_ID, session.getJobId());
            String dataVersion = null;
            if (windows != null) {
                for (DiagnosisPrecomputeWindowRow window : windows) {
                    if (window != null && hasText(window.getDataVersion())) {
                        dataVersion = window.getDataVersion();
                    }
                }
            }
            if (!hasText(dataVersion)) {
                return null;
            }
            DiagnosisOverviewSnapshotRow overview = snapshotMapper.selectOverviewByQueryAndVersion(
                TENANT_ID, session.getQueryHash(), dataVersion);
            if (overview == null) {
                return null;
            }
            session.setDataVersion(dataVersion);
            sessionCacheStore.save(sessionId, session, sessionTtl());
            return overview;
        }

        DiagnosisOverviewSnapshotRow overview = snapshotMapper.selectLatestOverviewByQuery(TENANT_ID, session.getQueryHash());
        if (overview == null) {
            return null;
        }

        if (hasText(overview.getDataVersion())) {
            session.setDataVersion(overview.getDataVersion());
            sessionCacheStore.save(sessionId, session, sessionTtl());
        }
        return overview;
    }

    private void sleepOneSecond() {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Duration sessionTtl() {
        return Duration.ofMinutes(Math.max(1, cacheProperties.getSessionTtlMinutes()));
    }

    private void renewSession(String sessionId, DiagnosisSessionCacheModel session) {
        sessionCacheStore.save(sessionId, session, sessionTtl());
        log.debug("renew diagnosis session ttl, sessionId={}, queryHash={}, jobId={}",
            sessionId, session.getQueryHash(), session.getJobId());
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

    private String resolveTrendMetricCode(String metricCode, String tabType) {
        if (hasText(metricCode)) {
            return normalizeTrendMetricCode(metricCode);
        }
        if (!hasText(tabType)) {
            return "sales";
        }
        return switch (tabType) {
            case "0" -> "sales";
            case "1" -> "salesQuantity";
            case "2" -> "gross";
            case "3" -> "grossRate";
            case "4" -> "customerCount";
            case "5" -> "customerPrice";
            case "6" -> "inventorySales";
            default -> normalizeTrendMetricCode(tabType);
        };
    }

    private String normalizeTrendMetricCode(String code) {
        if ("saleQuantity".equalsIgnoreCase(code)) {
            return "salesQuantity";
        }
        return code;
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
        try {
            return LOCAL_JSON_MAPPER.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "failed to serialize request payload");
        }
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

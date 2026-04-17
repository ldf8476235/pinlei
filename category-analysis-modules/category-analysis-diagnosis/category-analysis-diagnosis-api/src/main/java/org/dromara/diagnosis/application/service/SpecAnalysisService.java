package org.dromara.diagnosis.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.diagnosis.api.response.SpecDetailItemResponse;
import org.dromara.diagnosis.api.response.SpecDetailPageResponse;
import org.dromara.diagnosis.api.response.SpecFilterOptionItemResponse;
import org.dromara.diagnosis.api.response.SpecFilterOptionsResponse;
import org.dromara.diagnosis.api.response.SpecOverviewResponse;
import org.dromara.diagnosis.api.response.SpecRankingItemResponse;
import org.dromara.diagnosis.api.response.SpecRankingResponse;
import org.dromara.diagnosis.api.response.SpecSalesShareItemResponse;
import org.dromara.diagnosis.api.response.SpecSkuSalesChangeItemResponse;
import org.dromara.diagnosis.application.config.DiagnosisCacheProperties;
import org.dromara.diagnosis.application.model.DiagnosisSessionCacheModel;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSpecMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisOverviewSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeWindowRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSpecJsonRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSpecMetricRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSpecOverviewRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSpecRankingItemRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSpecRankingSummaryRow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SpecAnalysisService {

    private static final String TENANT_ID = "000000";
    private static final Map<String, String> RANKING_MAPPING = buildRankingMapping();
    private static final Map<String, String> DETAIL_ORDER_MAPPING = buildDetailOrderMapping();

    private final DiagnosisSnapshotMapper snapshotMapper;
    private final DiagnosisSessionCacheStore sessionCacheStore;
    private final DiagnosisCacheProperties cacheProperties;
    private final DiagnosisPrecomputeMapper precomputeMapper;
    private final DiagnosisSpecMapper specMapper;

    public SpecOverviewResponse getOverview(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);

        DiagnosisSpecOverviewRow row = specMapper.selectOverviewByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion());
        SpecOverviewResponse response = new SpecOverviewResponse();
        response.setTotalNum(row == null || row.getTotalNum() == null ? 0 : row.getTotalNum());
        response.setNewNum(row == null || row.getNewNum() == null ? 0 : row.getNewNum());
        return response;
    }

    public List<SpecSalesShareItemResponse> getSalesShare(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);
        return parseJsonPayload(session, "salesShare", new TypeReference<List<SpecSalesShareItemResponse>>() {
        }, List.of());
    }

    public SpecRankingResponse getRanking(String sessionId, String type, Integer page, Integer size, String orderType) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);

        int actualPage = page == null || page < 1 ? 1 : page;
        int actualSize = size == null || size < 1 ? 10 : Math.min(size, 200);
        int offset = (actualPage - 1) * actualSize;
        String metricColumn = RANKING_MAPPING.getOrDefault(type, "sales");
        String actualOrderType = "asc".equalsIgnoreCase(orderType) ? "ASC" : "DESC";

        DiagnosisSpecRankingSummaryRow summary = specMapper.selectRankingSummaryByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), metricColumn);
        List<DiagnosisSpecRankingItemRow> rows = specMapper.selectRankingPageByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), metricColumn, actualOrderType, offset, actualSize);

        List<SpecRankingItemResponse> list = new ArrayList<>(rows == null ? 0 : rows.size());
        for (DiagnosisSpecRankingItemRow row : rows == null ? List.<DiagnosisSpecRankingItemRow>of() : rows) {
            SpecRankingItemResponse item = new SpecRankingItemResponse();
            item.setProductSpec(row.getSpecName());
            item.setData(scale4(row.getData()));
            list.add(item);
        }

        SpecRankingResponse response = new SpecRankingResponse();
        response.setAve(summary == null ? BigDecimal.ZERO : scale4(summary.getAve()));
        response.setMaxData(summary == null ? null : scale4(summary.getMaxData()));
        response.setMinData(summary == null ? null : scale4(summary.getMinData()));
        response.setTotal(summary == null || summary.getTotal() == null ? 0L : summary.getTotal());
        response.setList(list);
        return response;
    }

    public List<SpecSkuSalesChangeItemResponse> getSkuSalesChange(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);
        return parseJsonPayload(session, "skuSalesChange", new TypeReference<List<SpecSkuSalesChangeItemResponse>>() {
        }, List.of());
    }

    public SpecDetailPageResponse getSpecList(String sessionId,
                                              List<String> specTypeList,
                                              List<String> specList,
                                              String newSpecType,
                                              Integer page,
                                              Integer size,
                                              String order,
                                              String orderType) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);

        int actualPage = page == null || page < 1 ? 1 : page;
        int actualSize = size == null || size < 1 ? 10 : Math.min(size, 200);
        int offset = (actualPage - 1) * actualSize;
        String actualOrderBy = DETAIL_ORDER_MAPPING.getOrDefault(order, "sales");
        String actualOrderType = "asc".equalsIgnoreCase(orderType) ? "ASC" : "DESC";

        Long total = specMapper.countMetricByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), specTypeList, specList, newSpecType);
        List<DiagnosisSpecMetricRow> rows = safeList(specMapper.selectMetricPageByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(),
            specTypeList, specList, newSpecType, actualOrderBy, actualOrderType, offset, actualSize));

        List<SpecDetailItemResponse> records = new ArrayList<>(rows.size());
        for (DiagnosisSpecMetricRow row : rows) {
            records.add(toDetailItem(row));
        }

        SpecDetailPageResponse response = new SpecDetailPageResponse();
        response.setRecords(records);
        response.setTotal(total == null ? 0L : total);
        response.setCurrent(actualPage);
        response.setSize(actualSize);
        response.setPages((int) ((response.getTotal() + actualSize - 1) / actualSize));
        return response;
    }

    public SpecFilterOptionsResponse getFilterOptions(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);

        SpecFilterOptionsResponse response = new SpecFilterOptionsResponse();
        response.setSpecList(parseJsonPayload(session, "specType", new TypeReference<List<SpecFilterOptionItemResponse>>() {
        }, List.of()));
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

    private <T> T parseJsonPayload(DiagnosisSessionCacheModel session,
                                   String payloadCode,
                                   TypeReference<T> typeReference,
                                   T defaultValue) {
        DiagnosisSpecJsonRow row = specMapper.selectJsonByCode(TENANT_ID, session.getQueryHash(), session.getDataVersion(), payloadCode);
        if (row == null || !hasText(row.getPayloadJson())) {
            return defaultValue;
        }
        T value = JsonUtils.parseObject(row.getPayloadJson(), typeReference);
        return value == null ? defaultValue : value;
    }

    private SpecDetailItemResponse toDetailItem(DiagnosisSpecMetricRow row) {
        SpecDetailItemResponse item = new SpecDetailItemResponse();
        item.setSpecName(row.getSpecName());
        item.setSpecType(row.getSpecType());
        item.setSpecTypeName(row.getSpecTypeName());
        item.setNewSpecType(row.getNewSpecType());
        item.setNewSpecTypeName(row.getNewSpecTypeName());
        item.setSku(row.getSkuCount());
        item.setSkuChange(row.getSkuChange());
        item.setSkuInc(scale4(row.getSkuInc()));
        item.setSkuPer(scale4(row.getSkuPer()));
        item.setSaleQuantity(scale4(row.getSaleQuantity()));
        item.setSaleQuantityChange(scale4(row.getSaleQuantityChange()));
        item.setSaleQuantityInc(scale4(row.getSaleQuantityInc()));
        item.setSaleQuantityPer(scale4(row.getSaleQuantityPer()));
        item.setSaleQuantityPsd(scale4(row.getSaleQuantityPsd()));
        item.setSales(scale4(row.getSales()));
        item.setSalesChange(scale4(row.getSalesChange()));
        item.setSalesInc(scale4(row.getSalesInc()));
        item.setSalesPer(scale4(row.getSalesPer()));
        item.setSalesPsd(scale4(row.getSalesPsd()));
        item.setGross(scale4(row.getGross()));
        item.setGrossChange(scale4(row.getGrossChange()));
        item.setGrossInc(scale4(row.getGrossInc()));
        item.setGrossPer(scale4(row.getGrossPer()));
        item.setGrossPsd(scale4(row.getGrossPsd()));
        item.setGrossRate(scale4(row.getGrossRate()));
        item.setGrossRateInc(scale4(row.getGrossRateInc()));
        item.setStockQuantity(scale4(row.getStockQuantity()));
        item.setTurnoverRate(scale4(row.getTurnoverRate()));
        item.setTurnoverDays(scale4(row.getTurnoverDays()));
        item.setStockSalesRate(scale4(row.getStockSalesRate()));
        item.setContributionRate(scale4(row.getContributionRate()));
        item.setGmroi(scale4(row.getGmroi()));
        item.setSalesRate(scale4(row.getSalesRate()));
        item.setActivitySku(row.getActivitySku());
        return item;
    }

    private static Map<String, String> buildRankingMapping() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("1", "sales");
        mapping.put("2", "sale_quantity");
        mapping.put("3", "gross");
        mapping.put("4", "gross_rate");
        return mapping;
    }

    private static Map<String, String> buildDetailOrderMapping() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("sales", "sales");
        mapping.put("salesInc", "sales_inc");
        mapping.put("gross", "gross");
        mapping.put("grossInc", "gross_inc");
        mapping.put("grossRate", "gross_rate");
        mapping.put("grossRateInc", "gross_rate_inc");
        mapping.put("sku", "sku_count");
        mapping.put("skuInc", "sku_inc");
        mapping.put("saleQuantity", "sale_quantity");
        mapping.put("saleQuantityInc", "sale_quantity_inc");
        mapping.put("stockQuantity", "stock_quantity");
        mapping.put("turnoverRate", "turnover_rate");
        mapping.put("turnoverDays", "turnover_days");
        mapping.put("gmroi", "gmroi");
        mapping.put("salesPer", "sales_per");
        return mapping;
    }

    private List<DiagnosisSpecMetricRow> safeList(List<DiagnosisSpecMetricRow> rows) {
        return rows == null ? List.of() : rows;
    }

    private BigDecimal scale4(BigDecimal value) {
        return value == null ? null : value.setScale(4, RoundingMode.HALF_UP);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

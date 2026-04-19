package org.dromara.diagnosis.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.diagnosis.api.response.VendorRankingItemResponse;
import org.dromara.diagnosis.api.response.VendorRankingResponse;
import org.dromara.diagnosis.api.response.VendorSalesShareItemResponse;
import org.dromara.diagnosis.api.response.VendorSummaryResponse;
import org.dromara.diagnosis.application.config.DiagnosisCacheProperties;
import org.dromara.diagnosis.application.model.DiagnosisSessionCacheModel;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisVendorMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisOverviewSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeWindowRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisVendorJsonRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisVendorRankingItemRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisVendorRankingSummaryRow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VendorAnalysisService {

    private static final String TENANT_ID = "000000";
    private static final Map<String, String> RANKING_MAPPING = Map.of(
        "1", "unit_output",
        "2", "gross_rate",
        "3", "sku_count",
        "4", "new_sku_count",
        "5", "diff_order_amount_rate"
    );

    private final DiagnosisSnapshotMapper snapshotMapper;
    private final DiagnosisSessionCacheStore sessionCacheStore;
    private final DiagnosisCacheProperties cacheProperties;
    private final DiagnosisPrecomputeMapper precomputeMapper;
    private final DiagnosisVendorMapper vendorMapper;

    public List<VendorSalesShareItemResponse> getSalesShare(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);
        return parseJsonPayload(session, "salesShare", new TypeReference<List<VendorSalesShareItemResponse>>() {
        }, List.of());
    }

    public VendorSummaryResponse getSummary(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);
        VendorSummaryResponse response = new VendorSummaryResponse();
        response.setSummaryOne(parseJsonPayload(session, "summaryOne", new TypeReference<List<String>>() {
        }, List.of()));
        response.setSummaryTwo(parseJsonPayload(session, "summaryTwo", new TypeReference<List<String>>() {
        }, List.of()));
        response.setSummaryThree(parseJsonPayload(session, "summaryThree", new TypeReference<List<String>>() {
        }, List.of()));
        return response;
    }

    public VendorRankingResponse getRanking(String sessionId, String type, Integer page, Integer size, String orderType) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);

        int actualPage = page == null || page < 1 ? 1 : page;
        int actualSize = size == null || size < 1 ? 10 : Math.min(size, 200);
        int offset = (actualPage - 1) * actualSize;
        String metricColumn = RANKING_MAPPING.getOrDefault(type, "unit_output");
        String actualOrderType = "asc".equalsIgnoreCase(orderType) ? "ASC" : "DESC";

        DiagnosisVendorRankingSummaryRow summary = vendorMapper.selectRankingSummaryByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), metricColumn);
        List<DiagnosisVendorRankingItemRow> rows = vendorMapper.selectRankingPageByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), metricColumn, actualOrderType, offset, actualSize);

        List<VendorRankingItemResponse> list = new ArrayList<>(rows == null ? 0 : rows.size());
        for (DiagnosisVendorRankingItemRow row : rows == null ? List.<DiagnosisVendorRankingItemRow>of() : rows) {
            VendorRankingItemResponse item = new VendorRankingItemResponse();
            item.setProductVendorNo(row.getProductVendorNo());
            item.setProductVendorName(row.getProductVendorName());
            item.setData(scale4(row.getData()));
            list.add(item);
        }

        VendorRankingResponse response = new VendorRankingResponse();
        response.setAve(summary == null ? BigDecimal.ZERO : scale4(summary.getAve()));
        response.setMaxData(summary == null ? null : scale4(summary.getMaxData()));
        response.setMinData(summary == null ? null : scale4(summary.getMinData()));
        response.setTotal(summary == null || summary.getTotal() == null ? 0L : summary.getTotal());
        response.setList(list);
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
        DiagnosisVendorJsonRow row = vendorMapper.selectJsonByCode(TENANT_ID, session.getQueryHash(), session.getDataVersion(), payloadCode);
        if (row == null || !hasText(row.getPayloadJson())) {
            return defaultValue;
        }
        T value = JsonUtils.parseObject(row.getPayloadJson(), typeReference);
        return value == null ? defaultValue : value;
    }

    private BigDecimal scale4(BigDecimal value) {
        return value == null ? null : value.setScale(4, RoundingMode.HALF_UP);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

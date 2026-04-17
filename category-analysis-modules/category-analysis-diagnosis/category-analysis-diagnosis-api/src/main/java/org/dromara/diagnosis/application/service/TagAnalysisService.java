package org.dromara.diagnosis.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.diagnosis.api.response.TagDetailItemResponse;
import org.dromara.diagnosis.api.response.TagDetailPageResponse;
import org.dromara.diagnosis.api.response.TagSalesPerResponse;
import org.dromara.diagnosis.api.response.TagTypeGroupResponse;
import org.dromara.diagnosis.application.config.DiagnosisCacheProperties;
import org.dromara.diagnosis.application.model.DiagnosisSessionCacheModel;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisTagMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisOverviewSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeWindowRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisTagJsonRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisTagMetricRow;
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
public class TagAnalysisService {

    private static final String TENANT_ID = "000000";
    private static final Map<String, String> DETAIL_ORDER_MAPPING = buildDetailOrderMapping();

    private final DiagnosisSnapshotMapper snapshotMapper;
    private final DiagnosisSessionCacheStore sessionCacheStore;
    private final DiagnosisCacheProperties cacheProperties;
    private final DiagnosisPrecomputeMapper precomputeMapper;
    private final DiagnosisTagMapper tagMapper;

    public List<TagTypeGroupResponse> getTypes(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);
        return parseJsonPayload(session, "tagTypeTree", new TypeReference<List<TagTypeGroupResponse>>() {}, List.of());
    }

    public TagSalesPerResponse getSalesShare(String sessionId, String tagType) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);
        return parseJsonPayload(session, "tagSalesPer:" + tagType, new TypeReference<TagSalesPerResponse>() {}, new TagSalesPerResponse());
    }

    public TagDetailPageResponse getTagList(String sessionId,
                                            String tagType,
                                            List<String> tagList,
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
        boolean ignoreTagFilter = ignoreTagFilter(tagList);
        List<String> filteredTagList = ignoreTagFilter ? List.of() : tagList;

        Long total = tagMapper.countMetricByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), tagType, filteredTagList, ignoreTagFilter);
        List<DiagnosisTagMetricRow> rows = safeList(tagMapper.selectMetricPageByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), tagType, filteredTagList,
            ignoreTagFilter, actualOrderBy, actualOrderType, offset, actualSize));

        List<TagDetailItemResponse> records = new ArrayList<>(rows.size());
        for (DiagnosisTagMetricRow row : rows) {
            records.add(toDetailItem(row));
        }

        TagDetailPageResponse response = new TagDetailPageResponse();
        response.setRecords(records);
        response.setTotal(total == null ? 0L : total);
        response.setCurrent(actualPage);
        response.setSize(actualSize);
        response.setPages((int) ((response.getTotal() + actualSize - 1) / actualSize));
        return response;
    }

    private boolean ignoreTagFilter(List<String> tagList) {
        return tagList == null || tagList.isEmpty() || tagList.contains("-1");
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
        DiagnosisTagJsonRow row = tagMapper.selectJsonByCode(TENANT_ID, session.getQueryHash(), session.getDataVersion(), payloadCode);
        if (row == null || !hasText(row.getPayloadJson())) {
            return defaultValue;
        }
        T value = JsonUtils.parseObject(row.getPayloadJson(), typeReference);
        return value == null ? defaultValue : value;
    }

    private TagDetailItemResponse toDetailItem(DiagnosisTagMetricRow row) {
        TagDetailItemResponse item = new TagDetailItemResponse();
        item.setTagNo(row.getTagNo());
        item.setTagName(row.getTagName());
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
        mapping.put("salesPer", "sales_per");
        mapping.put("grossPer", "gross_per");
        mapping.put("contributionRate", "contribution_rate");
        return mapping;
    }

    private List<DiagnosisTagMetricRow> safeList(List<DiagnosisTagMetricRow> rows) {
        return rows == null ? List.of() : rows;
    }

    private BigDecimal scale4(BigDecimal value) {
        return value == null ? null : value.setScale(4, RoundingMode.HALF_UP);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

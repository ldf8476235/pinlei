package org.dromara.diagnosis.application.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.response.DiagnosisIntroduceDirectionResponse;
import org.dromara.diagnosis.application.config.DiagnosisCacheProperties;
import org.dromara.diagnosis.application.model.DiagnosisSessionCacheModel;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisBrandMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPriceBandMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSpecMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSubclassContributionMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisTagMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisVendorMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisBrandMetricRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisOverviewSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeWindowRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPriceBandRangeRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSpecMetricRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSubclassContributionRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisTagMetricRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisVendorRankingItemRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiagnosisIntroduceDirectionService {

    private static final Logger log = LoggerFactory.getLogger(DiagnosisIntroduceDirectionService.class);
    private static final String TENANT_ID = "000000";
    private static final int ROW_COUNT = 3;
    private static final List<String> TAG_LABEL_ORDER = List.of("产品形态", "原料", "功效", "香型", "包装", "人群", "产地");

    private final DiagnosisSessionCacheStore sessionCacheStore;
    private final DiagnosisCacheProperties cacheProperties;
    private final DiagnosisSnapshotMapper snapshotMapper;
    private final DiagnosisPrecomputeMapper precomputeMapper;
    private final DiagnosisVendorMapper vendorMapper;
    private final DiagnosisTagMapper tagMapper;
    private final DiagnosisSpecMapper specMapper;
    private final DiagnosisPriceBandMapper priceBandMapper;
    private final DiagnosisSubclassContributionMapper subclassContributionMapper;
    private final DiagnosisBrandMapper brandMapper;

    public DiagnosisIntroduceDirectionResponse getIntroduceDirection(String sessionId) {
        long start = System.currentTimeMillis();
        log.info("query introduce direction start, sessionId={}", sessionId);
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);

        List<String> vendors = topVendors(session);
        Map<TagColumn, List<String>> tagValues = topTags(session);
        List<String> specs = topSpecs(session);
        List<String> priceBands = topPriceBands(session);
        List<String> subClasses = topSubClasses(session);
        List<String> brands = topBrands(session);

        List<DiagnosisIntroduceDirectionResponse.Column> columns = buildColumns(tagValues.keySet());
        List<Map<String, String>> rows = new ArrayList<>(ROW_COUNT);
        for (int i = 0; i < ROW_COUNT; i++) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("index", String.valueOf(i + 1));
            row.put("priceBand", valueAt(priceBands, i));
            row.put("brand", valueAt(brands, i));
            row.put("spec", valueAt(specs, i));
            for (Map.Entry<TagColumn, List<String>> entry : tagValues.entrySet()) {
                row.put(entry.getKey().value(), valueAt(entry.getValue(), i));
            }
            row.put("vendor", valueAt(vendors, i));
            row.put("minClass", valueAt(subClasses, i));
            rows.add(row);
        }

        DiagnosisIntroduceDirectionResponse response = new DiagnosisIntroduceDirectionResponse();
        response.setSessionId(sessionId);
        response.setColumns(columns);
        response.setRows(rows);
        log.info("query introduce direction success, sessionId={}, rowCount={}, columnCount={}, costMs={}",
            sessionId, rows.size(), columns.size(), System.currentTimeMillis() - start);
        return response;
    }

    private List<DiagnosisIntroduceDirectionResponse.Column> buildColumns(Iterable<TagColumn> tagColumns) {
        List<DiagnosisIntroduceDirectionResponse.Column> columns = new ArrayList<>();
        columns.add(new DiagnosisIntroduceDirectionResponse.Column("顺序", "index"));
        columns.add(new DiagnosisIntroduceDirectionResponse.Column("价格区间", "priceBand"));
        columns.add(new DiagnosisIntroduceDirectionResponse.Column("品牌", "brand"));
        columns.add(new DiagnosisIntroduceDirectionResponse.Column("规格", "spec"));
        for (TagColumn tagColumn : tagColumns) {
            columns.add(new DiagnosisIntroduceDirectionResponse.Column(tagColumn.label(), tagColumn.value()));
        }
        columns.add(new DiagnosisIntroduceDirectionResponse.Column("供应商", "vendor"));
        columns.add(new DiagnosisIntroduceDirectionResponse.Column("小分类", "minClass"));
        return columns;
    }

    private List<String> topVendors(DiagnosisSessionCacheModel session) {
        List<DiagnosisVendorRankingItemRow> rows = vendorMapper.selectRankingPageByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), "unit_output", "DESC", 0, ROW_COUNT);
        List<String> result = new ArrayList<>();
        for (DiagnosisVendorRankingItemRow row : safeList(rows)) {
            result.add(joinCodeName(row.getProductVendorNo(), row.getProductVendorName()));
        }
        return result;
    }

    private Map<TagColumn, List<String>> topTags(DiagnosisSessionCacheModel session) {
        List<DiagnosisTagMetricRow> rows = safeList(tagMapper.selectMetricsByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion()));
        Map<String, TagColumn> columnMap = new LinkedHashMap<>();
        Map<String, List<DiagnosisTagMetricRow>> grouped = new LinkedHashMap<>();
        for (DiagnosisTagMetricRow row : rows) {
            if (Boolean.TRUE.equals(row.getIsUntagged()) || !hasText(row.getTagType()) || !hasText(row.getTagName())) {
                continue;
            }
            TagColumn column = new TagColumn(row.getTagTypeName(), "tag_" + row.getTagType());
            columnMap.putIfAbsent(row.getTagType(), column);
            grouped.computeIfAbsent(row.getTagType(), key -> new ArrayList<>()).add(row);
        }

        List<String> orderedKeys = new ArrayList<>(grouped.keySet());
        orderedKeys.sort(Comparator
            .comparingInt((String key) -> tagOrder(columnMap.get(key).label()))
            .thenComparing(key -> columnMap.get(key).label()));

        Map<TagColumn, List<String>> result = new LinkedHashMap<>();
        for (String key : orderedKeys) {
            List<String> values = grouped.get(key).stream()
                .sorted(Comparator.comparing(DiagnosisTagMetricRow::getSales, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(ROW_COUNT)
                .map(DiagnosisTagMetricRow::getTagName)
                .toList();
            result.put(columnMap.get(key), values);
        }
        return result;
    }

    private List<String> topSpecs(DiagnosisSessionCacheModel session) {
        return safeList(specMapper.selectMetricsByVersion(TENANT_ID, session.getQueryHash(), session.getDataVersion())).stream()
            .filter(row -> hasText(row.getSpecName()))
            .sorted(Comparator.comparing(DiagnosisSpecMetricRow::getSales, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(ROW_COUNT)
            .map(DiagnosisSpecMetricRow::getSpecName)
            .toList();
    }

    private List<String> topPriceBands(DiagnosisSessionCacheModel session) {
        return safeList(priceBandMapper.selectRangeByVersion(
                TENANT_ID, session.getQueryHash(), session.getDataVersion(), "sales", "DESC")).stream()
            .sorted(Comparator
                .comparingInt((DiagnosisPriceBandRangeRow row) -> suggestGap(row) <= 0 ? 1 : 0)
                .thenComparing((DiagnosisPriceBandRangeRow row) -> suggestGap(row), Comparator.reverseOrder())
                .thenComparing(DiagnosisPriceBandRangeRow::getSales, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(ROW_COUNT)
            .map(this::priceBandLabel)
            .toList();
    }

    private List<String> topSubClasses(DiagnosisSessionCacheModel session) {
        return safeList(subclassContributionMapper.selectContributionByVersion(
                TENANT_ID, session.getQueryHash(), session.getDataVersion())).stream()
            .filter(row -> hasText(row.getSubClassName()))
            .sorted(Comparator.comparing(DiagnosisSubclassContributionRow::getCurrentSales, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(ROW_COUNT)
            .map(DiagnosisSubclassContributionRow::getSubClassName)
            .toList();
    }

    private List<String> topBrands(DiagnosisSessionCacheModel session) {
        return safeList(brandMapper.selectMetricsByVersion(TENANT_ID, session.getQueryHash(), session.getDataVersion())).stream()
            .filter(row -> hasText(row.getProductBrand()))
            .sorted(Comparator.comparing(DiagnosisBrandMetricRow::getSales, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(ROW_COUNT)
            .map(DiagnosisBrandMetricRow::getProductBrand)
            .toList();
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
                for (DiagnosisPrecomputeWindowRow window : safeList(windows)) {
                    if (window != null && hasText(window.getDataVersion())) {
                        dataVersion = window.getDataVersion();
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
            log.warn("query introduce direction failed, no diagnosis snapshot, sessionId={}, queryHash={}",
                sessionId, session.getQueryHash());
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "no diagnosis snapshot found for current query");
        }
        if (hasText(latest.getDataVersion())) {
            session.setDataVersion(latest.getDataVersion());
            sessionCacheStore.save(sessionId, session, Duration.ofMinutes(Math.max(1, cacheProperties.getResultTtlMinutes())));
        }
        return latest;
    }

    private String priceBandLabel(DiagnosisPriceBandRangeRow row) {
        if (hasText(row.getPriceBandLabel())) {
            return row.getPriceBandLabel();
        }
        return formatMoney(row.getPriceBandMin()) + "-" + formatMoney(row.getPriceBandMax());
    }

    private String formatMoney(BigDecimal value) {
        return value == null ? "-" : value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private int suggestGap(DiagnosisPriceBandRangeRow row) {
        int suggest = row.getSuggestSku() == null ? 0 : row.getSuggestSku();
        int current = row.getSku() == null ? 0 : row.getSku();
        return suggest - current;
    }

    private int tagOrder(String label) {
        int index = TAG_LABEL_ORDER.indexOf(label);
        return index < 0 ? TAG_LABEL_ORDER.size() : index;
    }

    private String joinCodeName(String code, String name) {
        if (hasText(code) && hasText(name)) {
            return code + " " + name;
        }
        return hasText(code) ? code : (hasText(name) ? name : "");
    }

    private String valueAt(List<String> values, int index) {
        if (values == null || index < 0 || index >= values.size()) {
            return "";
        }
        return values.get(index);
    }

    private <T> List<T> safeList(List<T> rows) {
        return rows == null ? List.of() : rows;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record TagColumn(String label, String value) {
    }
}

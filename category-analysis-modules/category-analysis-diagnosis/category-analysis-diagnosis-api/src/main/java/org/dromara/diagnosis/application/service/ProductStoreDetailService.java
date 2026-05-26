package org.dromara.diagnosis.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.diagnosis.api.response.ProductStoreDetailItemResponse;
import org.dromara.diagnosis.api.response.ProductStoreDetailPageResponse;
import org.dromara.diagnosis.application.config.DiagnosisCacheProperties;
import org.dromara.diagnosis.application.model.DiagnosisSessionCacheModel;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisProductStoreDetailMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisOverviewSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeWindowRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisProductStoreDetailRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceShardParam;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductStoreDetailService {

    private static final String TENANT_ID = "000000";
    private static final Map<String, String> ORDER_MAPPING = buildOrderMapping();

    private final DiagnosisSnapshotMapper snapshotMapper;
    private final DiagnosisSessionCacheStore sessionCacheStore;
    private final DiagnosisCacheProperties cacheProperties;
    private final DiagnosisPrecomputeMapper precomputeMapper;
    private final DiagnosisProductStoreDetailMapper productStoreDetailMapper;

    public ProductStoreDetailPageResponse getStoreDetails(String requestId,
                                                          String sessionId,
                                                          String productNo,
                                                          List<String> statusList,
                                                          String storeKeyword,
                                                          Integer page,
                                                          Integer size,
                                                          String order,
                                                          String orderType) {
        long start = System.currentTimeMillis();
        if (!hasText(productNo)) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "productNo is required");
        }
        DiagnosisSessionCacheModel session = getSession(sessionId);
        DiagnosisOverviewSnapshotRow overview = resolveOverviewSnapshot(sessionId, session);

        int actualPage = page == null || page < 1 ? 1 : page;
        int actualSize = size == null || size < 1 ? 10 : Math.min(size, 200);
        int offset = (actualPage - 1) * actualSize;
        String actualOrderBy = ORDER_MAPPING.getOrDefault(order, "sales");
        String actualOrderType = "asc".equalsIgnoreCase(orderType) ? "ASC" : "DESC";
        List<String> normalizedStatusList = normalizeStatusList(statusList);
        String normalizedStoreKeyword = hasText(storeKeyword) ? storeKeyword.trim() : null;
        DiagnosisSourceShardParam param = toSourceParam(overview);
        LocalDate stockDate = overview.getPeriodEnd();
        long periodDays = Math.max(1L, ChronoUnit.DAYS.between(overview.getPeriodStart(), overview.getPeriodEnd()) + 1);
        BigDecimal totalGross = nvl(overview.getMetricTotalProfit());

        log.info("query product store details start, requestId={}, sessionId={}, queryHash={}, dataVersion={}, productNo={}, page={}, size={}, order={}, orderType={}",
            requestId, sessionId, session.getQueryHash(), session.getDataVersion(), productNo, actualPage, actualSize, actualOrderBy, actualOrderType);

        Long total = productStoreDetailMapper.countStoreDetails(
            param, productNo, stockDate, normalizedStatusList, normalizedStoreKeyword);
        List<DiagnosisProductStoreDetailRow> rows = productStoreDetailMapper.selectStoreDetailPage(
            param, productNo, stockDate, normalizedStatusList, normalizedStoreKeyword,
            actualOrderBy, actualOrderType, offset, actualSize);

        List<ProductStoreDetailItemResponse> records = new ArrayList<>();
        for (DiagnosisProductStoreDetailRow row : rows == null ? List.<DiagnosisProductStoreDetailRow>of() : rows) {
            records.add(toItem(row, periodDays, totalGross));
        }

        ProductStoreDetailPageResponse response = new ProductStoreDetailPageResponse();
        response.setRecords(records);
        response.setTotal(total == null ? 0L : total);
        response.setCurrent(actualPage);
        response.setSize(actualSize);
        response.setPages((int) ((response.getTotal() + actualSize - 1) / actualSize));

        log.info("query product store details finished, requestId={}, sessionId={}, productNo={}, total={}, returned={}, costMs={}",
            requestId, sessionId, productNo, response.getTotal(), records.size(), System.currentTimeMillis() - start);
        return response;
    }

    private ProductStoreDetailItemResponse toItem(DiagnosisProductStoreDetailRow row, long periodDays, BigDecimal totalGross) {
        BigDecimal sales = nvl(row.getSales());
        BigDecimal gross = nvl(row.getGross());
        BigDecimal saleQuantity = nvl(row.getSaleQuantity());
        BigDecimal saleCost = nvl(row.getSaleCost());
        BigDecimal stockQuantity = nvl(row.getStockQuantity());
        BigDecimal turnoverRate = ratio(saleCost, stockQuantity);

        ProductStoreDetailItemResponse item = new ProductStoreDetailItemResponse();
        item.setStoreNo(row.getStoreNo());
        item.setStoreName(row.getStoreName());
        item.setProductStatus(row.getProductStatus());
        item.setProductStatusNo(row.getProductStatusNo());
        item.setSaleQuantity(scale2(saleQuantity));
        item.setSales(scale2(sales));
        item.setGross(scale2(gross));
        item.setGrossRate(scale4(ratio(gross, sales)));
        item.setStockQuantity(scale2(stockQuantity));
        item.setTurnoverRate(scale4(turnoverRate));
        item.setTurnoverDays(scale4(turnoverRate.compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(periodDays).divide(turnoverRate, 6, RoundingMode.HALF_UP)));
        item.setContributionRate(scale4(ratio(gross, totalGross)));
        item.setGmroi(scale4(ratio(gross, stockQuantity)));
        item.setSalesRate(null);
        item.setActivity(row.getActivity());
        item.setFirstSaleDate(row.getFirstSaleDate());
        return item;
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

    private DiagnosisSourceShardParam toSourceParam(DiagnosisOverviewSnapshotRow overview) {
        DiagnosisSourceShardParam param = new DiagnosisSourceShardParam();
        param.setPeriodStart(overview.getPeriodStart());
        param.setPeriodEnd(overview.getPeriodEnd());
        param.setClassLevel(overview.getClassLevel());
        param.setClassNo(overview.getClassNo());
        param.setDeptId(overview.getDeptId() == null ? null : String.valueOf(overview.getDeptId()));
        param.setRetailTypeId(overview.getRetailTypeId());
        param.setBusinessCircleId(overview.getBusinessCircleId());
        param.setDeptGroupId(overview.getDeptGroupId());
        param.setStoreNo(overview.getStoreNo());
        return param;
    }

    private static Map<String, String> buildOrderMapping() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("storeNo", "storeNo");
        mapping.put("storeName", "storeName");
        mapping.put("productStatus", "productStatus");
        mapping.put("saleQuantity", "saleQuantity");
        mapping.put("sales", "sales");
        mapping.put("gross", "gross");
        mapping.put("grossRate", "CASE WHEN sales = 0 THEN 0 ELSE gross / sales END");
        mapping.put("stockQuantity", "stockQuantity");
        mapping.put("turnoverRate", "CASE WHEN stockQuantity = 0 THEN 0 ELSE saleCost / stockQuantity END");
        mapping.put("turnoverDays", "CASE WHEN saleCost = 0 THEN 0 ELSE stockQuantity / saleCost END");
        mapping.put("contributionRate", "gross");
        mapping.put("gmroi", "CASE WHEN stockQuantity = 0 THEN 0 ELSE gross / stockQuantity END");
        mapping.put("activity", "activity");
        mapping.put("firstSaleDate", "firstSaleDate");
        return mapping;
    }

    private List<String> normalizeStatusList(List<String> statusList) {
        List<String> result = statusList == null ? new ArrayList<>() : new ArrayList<>(statusList);
        result.remove("-1");
        result.removeIf(item -> item == null || item.isBlank());
        return result;
    }

    private BigDecimal ratio(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return numerator.divide(denominator, 6, RoundingMode.HALF_UP);
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
        return value != null && !value.isBlank();
    }
}

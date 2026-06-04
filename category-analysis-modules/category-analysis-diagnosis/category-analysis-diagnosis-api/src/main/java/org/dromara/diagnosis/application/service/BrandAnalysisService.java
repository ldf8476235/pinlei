package org.dromara.diagnosis.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.diagnosis.api.response.BrandDetailItemResponse;
import org.dromara.diagnosis.api.response.BrandDetailPageResponse;
import org.dromara.diagnosis.api.response.BrandFilterOptionItemResponse;
import org.dromara.diagnosis.api.response.BrandFilterOptionsResponse;
import org.dromara.diagnosis.api.response.BrandOverviewResponse;
import org.dromara.diagnosis.api.response.BrandRankingItemResponse;
import org.dromara.diagnosis.api.response.BrandRankingResponse;
import org.dromara.diagnosis.api.response.BrandSalesShareItemResponse;
import org.dromara.diagnosis.api.response.BrandSkuDetailPageResponse;
import org.dromara.diagnosis.api.response.BrandSkuSalesChangeItemResponse;
import org.dromara.diagnosis.api.response.LegacyClassSalesListItemResponse;
import org.dromara.diagnosis.application.config.DiagnosisCacheProperties;
import org.dromara.diagnosis.application.model.DiagnosisSessionCacheModel;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisBrandMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisCategorySalesListMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisCategorySalesSkuRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisBrandJsonRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisBrandMetricRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisBrandOverviewRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisBrandRankingItemRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisBrandRankingSummaryRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisOverviewSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeWindowRow;
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
public class BrandAnalysisService {

    private static final String TENANT_ID = "000000";
    private static final Map<String, String> RANKING_MAPPING = buildRankingMapping();
    private static final Map<String, String> DETAIL_ORDER_MAPPING = buildDetailOrderMapping();

    private final DiagnosisSnapshotMapper snapshotMapper;
    private final DiagnosisSessionCacheStore sessionCacheStore;
    private final DiagnosisCacheProperties cacheProperties;
    private final DiagnosisPrecomputeMapper precomputeMapper;
    private final DiagnosisBrandMapper brandMapper;
    private final DiagnosisCategorySalesListMapper categorySalesListMapper;

    public BrandOverviewResponse getOverview(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);

        DiagnosisBrandOverviewRow row = brandMapper.selectOverviewByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion());
        BrandOverviewResponse response = new BrandOverviewResponse();
        response.setTotalNum(row == null || row.getTotalNum() == null ? 0 : row.getTotalNum());
        response.setNewNum(row == null || row.getNewNum() == null ? 0 : row.getNewNum());
        response.setOwnNum(row == null || row.getOwnNum() == null ? 0 : row.getOwnNum());
        return response;
    }

    public List<BrandSalesShareItemResponse> getSalesShare(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);
        return parseJsonPayload(session, "salesShare", new TypeReference<List<BrandSalesShareItemResponse>>() {
        }, List.of());
    }

    public BrandRankingResponse getRanking(String sessionId, String type, Integer page, Integer size, String orderType) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);

        int actualPage = page == null || page < 1 ? 1 : page;
        int actualSize = size == null || size < 1 ? 10 : Math.min(size, 200);
        int offset = (actualPage - 1) * actualSize;
        String metricColumn = RANKING_MAPPING.getOrDefault(type, "sales");
        String actualOrderType = "asc".equalsIgnoreCase(orderType) ? "ASC" : "DESC";

        DiagnosisBrandRankingSummaryRow summary = brandMapper.selectRankingSummaryByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), metricColumn);
        List<DiagnosisBrandRankingItemRow> rows = brandMapper.selectRankingPageByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), metricColumn, actualOrderType, offset, actualSize);

        List<BrandRankingItemResponse> list = new ArrayList<>(rows == null ? 0 : rows.size());
        for (DiagnosisBrandRankingItemRow row : rows == null ? List.<DiagnosisBrandRankingItemRow>of() : rows) {
            BrandRankingItemResponse item = new BrandRankingItemResponse();
            item.setBrandNo(row.getBrandNo());
            item.setProductBrand(row.getProductBrand());
            item.setData(scale4(row.getData()));
            list.add(item);
        }

        BrandRankingResponse response = new BrandRankingResponse();
        response.setAve(summary == null ? BigDecimal.ZERO : scale4(summary.getAve()));
        response.setMaxData(summary == null ? null : scale4(summary.getMaxData()));
        response.setMinData(summary == null ? null : scale4(summary.getMinData()));
        response.setTotal(summary == null || summary.getTotal() == null ? 0L : summary.getTotal());
        response.setList(list);
        return response;
    }

    public List<BrandSkuSalesChangeItemResponse> getSkuSalesChange(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);
        return parseJsonPayload(session, "skuSalesChange", new TypeReference<List<BrandSkuSalesChangeItemResponse>>() {
        }, List.of());
    }

    public BrandDetailPageResponse getBrandList(String sessionId,
                                                List<String> brandTypeList,
                                                List<String> brandList,
                                                String newBrandType,
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

        Long total = brandMapper.countMetricByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), brandTypeList, brandList, newBrandType);
        List<DiagnosisBrandMetricRow> rows = safeList(brandMapper.selectMetricPageByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(),
            brandTypeList, brandList, newBrandType, actualOrderBy, actualOrderType, offset, actualSize));

        List<BrandDetailItemResponse> records = new ArrayList<>(rows.size());
        for (DiagnosisBrandMetricRow row : rows) {
            records.add(toDetailItem(row));
        }

        BrandDetailPageResponse response = new BrandDetailPageResponse();
        response.setRecords(records);
        response.setTotal(total == null ? 0L : total);
        response.setCurrent(actualPage);
        response.setSize(actualSize);
        response.setPages((int) ((response.getTotal() + actualSize - 1) / actualSize));
        return response;
    }

    public BrandFilterOptionsResponse getFilterOptions(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);

        BrandFilterOptionsResponse response = new BrandFilterOptionsResponse();
        response.setBrandList(parseJsonPayload(session, "brandType", new TypeReference<List<BrandFilterOptionItemResponse>>() {
        }, List.of()));
        response.setSummaryOne(parseJsonPayload(session, "summaryOne", new TypeReference<List<String>>() {
        }, List.of()));
        response.setSummaryTwo(parseJsonPayload(session, "summaryTwo", new TypeReference<List<String>>() {
        }, List.of()));
        response.setSummaryThree(parseJsonPayload(session, "summaryThree", new TypeReference<List<String>>() {
        }, List.of()));
        response.setSummaryFour(parseJsonPayload(session, "summaryFour", new TypeReference<List<String>>() {
        }, List.of()));
        return response;
    }

    public BrandSkuDetailPageResponse getBrandSkuList(String sessionId,
                                                      List<String> brandList,
                                                      List<String> specList,
                                                      String tagType,
                                                      List<String> tagList,
                                                      List<String> statusList,
                                                      String promotion,
                                                      Boolean activeOnly,
                                                      Integer page,
                                                      Integer size,
                                                      String order,
                                                      String orderType) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);

        int actualPage = page == null || page < 1 ? 1 : page;
        int actualSize = size == null || size < 1 ? 10 : Math.min(size, 200);
        int offset = (actualPage - 1) * actualSize;
        String actualOrderBy = CATEGORY_SALES_ORDER_MAPPING.getOrDefault(order, "sales");
        String actualOrderType = "asc".equalsIgnoreCase(orderType) ? "ASC" : "DESC";
        String promotionFlag = resolvePromotionFlag(promotion);
        List<String> normalizedStatusList = normalizeStatusList(statusList);
        List<String> normalizedBrandList = normalizeTextList(brandList);
        List<String> normalizedSpecList = normalizeTextList(specList);
        String normalizedTagType = hasText(tagType) ? tagType.trim() : null;
        List<String> normalizedTagList = normalizeTextList(tagList);
        if (!hasText(normalizedTagType) || normalizedTagList.contains("-1")) {
            normalizedTagType = null;
            normalizedTagList = List.of();
        }
        boolean onlyActiveSku = Boolean.TRUE.equals(activeOnly);

        Long total = categorySalesListMapper.countSku(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), promotionFlag, normalizedStatusList, normalizedBrandList,
            normalizedSpecList, normalizedTagType, normalizedTagList, onlyActiveSku);
        List<DiagnosisCategorySalesSkuRow> rows = categorySalesListMapper.selectSkuPage(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), promotionFlag, normalizedStatusList, normalizedBrandList,
            normalizedSpecList, normalizedTagType, normalizedTagList, onlyActiveSku, actualOrderBy, actualOrderType, offset, actualSize);

        List<LegacyClassSalesListItemResponse> records = new ArrayList<>();
        for (DiagnosisCategorySalesSkuRow row : rows == null ? List.<DiagnosisCategorySalesSkuRow>of() : rows) {
            records.add(toSkuDetailItem(row));
        }

        BrandSkuDetailPageResponse response = new BrandSkuDetailPageResponse();
        response.setRecords(records);
        response.setTotal(total == null ? 0L : total);
        response.setCurrent(actualPage);
        response.setSize(actualSize);
        response.setPages((int) ((response.getTotal() + actualSize - 1) / actualSize));
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
        DiagnosisBrandJsonRow row = brandMapper.selectJsonByCode(TENANT_ID, session.getQueryHash(), session.getDataVersion(), payloadCode);
        if (row == null || !hasText(row.getPayloadJson())) {
            return defaultValue;
        }
        T value = JsonUtils.parseObject(row.getPayloadJson(), typeReference);
        return value == null ? defaultValue : value;
    }

    private BrandDetailItemResponse toDetailItem(DiagnosisBrandMetricRow row) {
        BrandDetailItemResponse item = new BrandDetailItemResponse();
        item.setBrandNo(row.getBrandNo());
        item.setProductBrand(row.getProductBrand());
        item.setBrandType(row.getBrandType());
        item.setBrandTypeName(row.getBrandTypeName());
        item.setNewBrandType(row.getNewBrandType());
        item.setNewBrandTypeName(row.getNewBrandTypeName());
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

    private LegacyClassSalesListItemResponse toSkuDetailItem(DiagnosisCategorySalesSkuRow row) {
        LegacyClassSalesListItemResponse item = new LegacyClassSalesListItemResponse();
        item.setProductNo(row.getProductNo());
        item.setProductName(row.getProductName());
        item.setProductStatus(row.getProductStatus());
        item.setProductStatusNo(row.getProductStatusNo());
        item.setStoreNum(row.getStoreNum());
        item.setSaleQuantity(scale4(row.getSaleQuantity()));
        item.setSaleQuantityPsd(scale4(row.getSaleQuantityPsd()));
        item.setSales(scale4(row.getSales()));
        item.setSalesPer(scale4(row.getSalesPer()));
        item.setSalesPsd(scale4(row.getSalesPsd()));
        item.setGross(scale4(row.getGross()));
        item.setGrossPer(scale4(row.getGrossPer()));
        item.setGrossPsd(scale4(row.getGrossPsd()));
        item.setGrossRate(scale4(row.getGrossRate()));
        item.setStockQuantity(scale4(row.getStockQuantity()));
        item.setTurnoverRate(scale4(row.getTurnoverRate()));
        item.setTurnoverDays(scale4(row.getTurnoverDays()));
        item.setStockSalesRate(scale4(row.getStockSalesRate()));
        item.setContributionRate(scale4(row.getContributionRate()));
        item.setGmroi(scale4(row.getGmroi()));
        item.setSalesRate(scale4(row.getSalesRate()));
        item.setActivity(row.getActivity());
        item.setFirstSaleDate(row.getFirstSaleDate());
        item.setNewProduct(row.getNewProduct());
        item.setKeyProduct(row.getKeyProduct());
        item.setSeasonableFlag(row.getSeasonableFlag());
        item.setSeasonableFlagName(row.getSeasonableFlagName());
        item.setSeasonableStartDate(row.getSeasonableStartDate());
        item.setSeasonableEndDate(row.getSeasonableEndDate());
        item.setClassNo(row.getClassNo());
        item.setClassName(row.getClassName());
        item.setProductBarcode(row.getProductBarcode());
        item.setBrandName(row.getBrandName());
        item.setSpec(row.getSpec());
        item.setInPrice(scale4(row.getInPrice()));
        item.setSalesPrice(scale4(row.getSalesPrice()));
        item.setProductVendorNo(row.getProductVendorNo());
        item.setProductVendorName(row.getProductVendorName());
        item.setProductVendorNoName(row.getProductVendorNoName());
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

    private static final Map<String, String> CATEGORY_SALES_ORDER_MAPPING = buildCategorySalesOrderMapping();

    private static Map<String, String> buildCategorySalesOrderMapping() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("productNo", "product_no");
        map.put("productName", "product_name");
        map.put("productStatus", "product_status");
        map.put("storeNum", "store_num");
        map.put("saleQuantity", "sale_quantity");
        map.put("saleQuantityPsd", "sale_quantity_psd");
        map.put("sales", "sales");
        map.put("salesPer", "sales_per");
        map.put("salesPsd", "sales_psd");
        map.put("gross", "gross");
        map.put("grossPer", "gross_per");
        map.put("grossPsd", "gross_psd");
        map.put("grossRate", "gross_rate");
        map.put("stockQuantity", "stock_quantity");
        map.put("turnoverRate", "turnover_rate");
        map.put("turnoverDays", "turnover_days");
        map.put("stockSalesRate", "stock_sales_rate");
        map.put("contributionRate", "contribution_rate");
        map.put("gmroi", "gmroi");
        map.put("salesRate", "sales_rate");
        map.put("activity", "activity");
        map.put("firstSaleDate", "first_sale_date");
        map.put("brandName", "brand_name");
        map.put("spec", "spec");
        map.put("inPrice", "in_price");
        map.put("salesPrice", "sales_price");
        map.put("productVendorNo", "product_vendor_no");
        map.put("productVendorName", "product_vendor_name");
        return map;
    }

    private List<DiagnosisBrandMetricRow> safeList(List<DiagnosisBrandMetricRow> rows) {
        return rows == null ? List.of() : rows;
    }

    private BigDecimal scale4(BigDecimal value) {
        return value == null ? null : value.setScale(4, RoundingMode.HALF_UP);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String resolvePromotionFlag(String promotion) {
        if (!hasText(promotion) || "0".equals(promotion)) {
            return null;
        }
        return "1".equals(promotion) ? "1" : "2";
    }

    private List<String> normalizeStatusList(List<String> statusList) {
        List<String> result = statusList == null ? new ArrayList<>() : new ArrayList<>(statusList);
        result.remove("-1");
        result.removeIf(item -> item == null || item.isBlank());
        return result;
    }

    private List<String> normalizeTextList(List<String> values) {
        List<String> result = new ArrayList<>();
        if (values == null) {
            return result;
        }
        for (String value : values) {
            if (hasText(value)) {
                result.add(value.trim());
            }
        }
        return result;
    }
}

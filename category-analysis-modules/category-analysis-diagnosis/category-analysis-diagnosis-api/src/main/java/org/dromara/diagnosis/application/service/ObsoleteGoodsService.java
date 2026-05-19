package org.dromara.diagnosis.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.diagnosis.api.request.ObsoleteGoodsListRequest;
import org.dromara.diagnosis.api.response.ObsoleteGoodsItemResponse;
import org.dromara.diagnosis.api.response.ObsoleteGoodsListResponse;
import org.dromara.diagnosis.application.config.DiagnosisCacheProperties;
import org.dromara.diagnosis.application.model.DiagnosisSessionCacheModel;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisObsoleteGoodsMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisObsoleteGoodsRow;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ObsoleteGoodsService {

    private static final String TENANT_ID = "000000";
    private static final Map<String, String> ORDER_BY_MAPPING = buildOrderByMapping();

    private final DiagnosisSnapshotMapper snapshotMapper;
    private final DiagnosisSessionCacheStore sessionCacheStore;
    private final DiagnosisCacheProperties cacheProperties;
    private final DiagnosisPrecomputeMapper precomputeMapper;
    private final DiagnosisObsoleteGoodsMapper obsoleteGoodsMapper;

    public ObsoleteGoodsListResponse queryObsoleteList(ObsoleteGoodsListRequest request) {
        if (request == null || request.getSessionId() == null || request.getSessionId().isBlank()) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "sessionId is required");
        }
        DiagnosisSessionCacheModel session = getSession(request.getSessionId());
        DiagnosisOverviewSnapshotRow overview = resolveOverviewSnapshot(request.getSessionId(), session);

        int actualPage = request.getPage() == null || request.getPage() < 1 ? 1 : request.getPage();
        int actualSize = request.getSize() == null || request.getSize() < 1 ? 10 : Math.min(request.getSize(), 200);
        int obsoleteSkuLimit = request.getObsoleteSku() == null || request.getObsoleteSku() < 1 ? Integer.MAX_VALUE : request.getObsoleteSku();
        int offset = (actualPage - 1) * actualSize;
        int limitedPageSize = offset >= obsoleteSkuLimit ? 0 : Math.min(actualSize, obsoleteSkuLimit - offset);

        List<String> obsoleteTypes = normalizeList(request.getObsoleteType());
        boolean useAbc = obsoleteTypes.isEmpty() || obsoleteTypes.contains("abc");
        boolean useGross = obsoleteTypes.isEmpty() || obsoleteTypes.contains("gross");
        boolean useGmroi = obsoleteTypes.isEmpty() || obsoleteTypes.contains("gmroi");

        String abcType = normalizeAbcType(request.getAbcType());
        String currentAbc = normalizeBlank(request.getCurrentAbc());
        String compareAbc = normalizeBlank(request.getCompareAbc());
        String currentGrossRole = normalizeRole(request.getCurrentGrossRole());
        String compareGrossRole = normalizeRole(request.getCompareGrossRole());
        String currentGmroiRole = normalizeRole(request.getCurrentGmroiRole());
        String compareGmroiRole = normalizeRole(request.getCompareGmroiRole());
        List<String> statusList = normalizeStatusList(request.getProductStatus());
        List<String> priceBandList = normalizeList(request.getPriceBandList());
        List<String> brandList = normalizeList(request.getBrandList());
        List<String> specList = normalizeList(request.getSpecList());
        String tagType = normalizeBlank(request.getTagType());
        List<String> tagList = normalizeList(request.getTagList());
        if (tagType == null || tagList.isEmpty()) {
            tagType = null;
            tagList = List.of();
        }
        String orderBy = ORDER_BY_MAPPING.getOrDefault(request.getOrder(), "g.sales");
        String orderType = "asc".equalsIgnoreCase(request.getOrderType()) ? "ASC" : "DESC";

        log.info("query obsolete goods start sessionId={}, queryHash={}, dataVersion={}, page={}, size={}, obsoleteSku={}, useAbc={}, useGross={}, useGmroi={}, priceBandFilter={}, brandFilter={}, specFilter={}, tagFilter={}",
            request.getSessionId(), session.getQueryHash(), overview.getDataVersion(), actualPage, actualSize, obsoleteSkuLimit, useAbc, useGross, useGmroi,
            priceBandList.size(), brandList.size(), specList.size(), tagList.size());

        Long rawTotal = obsoleteGoodsMapper.countObsoleteGoods(
            TENANT_ID, session.getQueryHash(), overview.getDataVersion(), abcType, currentAbc, compareAbc,
            currentGrossRole, compareGrossRole, currentGmroiRole, compareGmroiRole,
            useAbc, useGross, useGmroi, statusList, normalizeBlank(request.getFirstSaleDate()), priceBandList, brandList, specList, tagType, tagList);
        long total = Math.min(rawTotal == null ? 0L : rawTotal, obsoleteSkuLimit);

        List<DiagnosisObsoleteGoodsRow> rows = limitedPageSize <= 0
            ? List.of()
            : obsoleteGoodsMapper.selectObsoleteGoodsPage(
                TENANT_ID, session.getQueryHash(), overview.getDataVersion(), abcType, currentAbc, compareAbc,
                currentGrossRole, compareGrossRole, currentGmroiRole, compareGmroiRole,
                useAbc, useGross, useGmroi, statusList, normalizeBlank(request.getFirstSaleDate()), priceBandList, brandList, specList, tagType, tagList,
                orderBy, orderType, offset, limitedPageSize);

        ObsoleteGoodsListResponse response = new ObsoleteGoodsListResponse();
        response.setRecords(toResponses(rows));
        response.setTotal(total);
        response.setCurrent(actualPage);
        response.setSize(actualSize);
        response.setPages((int) ((total + actualSize - 1) / actualSize));

        log.info("query obsolete goods end sessionId={}, queryHash={}, dataVersion={}, rawTotal={}, returned={}",
            request.getSessionId(), session.getQueryHash(), overview.getDataVersion(), rawTotal, response.getRecords().size());
        return response;
    }

    private List<ObsoleteGoodsItemResponse> toResponses(List<DiagnosisObsoleteGoodsRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        List<ObsoleteGoodsItemResponse> records = new ArrayList<>(rows.size());
        for (DiagnosisObsoleteGoodsRow row : rows) {
            ObsoleteGoodsItemResponse item = new ObsoleteGoodsItemResponse();
            item.setProductNo(row.getProductNo());
            item.setProductName(row.getProductName());
            item.setProductStatus(row.getProductStatus());
            item.setProductStatusNo(row.getProductStatusNo());
            item.setHandleStatus("未处理");
            item.setDealStatusName("未处理");
            item.setStoreNum(row.getStoreNum());
            item.setCurrentSalesAbc(row.getCurrentSalesAbc());
            item.setCompareSalesAbc(row.getCompareSalesAbc());
            item.setCurrentGrossAbc(row.getCurrentGrossAbc());
            item.setCompareGrossAbc(row.getCompareGrossAbc());
            item.setCurrentContributionAbc(row.getCurrentContributionAbc());
            item.setCompareContributionAbc(row.getCompareContributionAbc());
            item.setCurrentGrossRole(row.getCurrentGrossRole());
            item.setCurrentGrossRoleName(grossRoleName(row.getCurrentGrossRole()));
            item.setCompareGrossRole(row.getCompareGrossRole());
            item.setCompareGrossRoleName(grossRoleName(row.getCompareGrossRole()));
            item.setCurrentGmroiRole(row.getCurrentGmroiRole());
            item.setCurrentGmroiRoleName(gmroiRoleName(row.getCurrentGmroiRole()));
            item.setCompareGmroiRole(row.getCompareGmroiRole());
            item.setCompareGmroiRoleName(gmroiRoleName(row.getCompareGmroiRole()));
            item.setSaleQuantity(scale2(row.getSaleQuantity()));
            item.setSaleQuantityPsd(scale4(row.getSaleQuantityPsd()));
            item.setSales(scale2(row.getSales()));
            item.setSalesPer(scale6(row.getSalesPer()));
            item.setSalesPsd(scale4(row.getSalesPsd()));
            item.setGross(scale2(row.getGross()));
            item.setGrossPer(scale6(row.getGrossPer()));
            item.setGrossPsd(scale4(row.getGrossPsd()));
            item.setGrossRate(scale6(row.getGrossRate()));
            item.setStockQuantity(scale2(row.getStockQuantity()));
            item.setTurnoverRate(scale4(row.getTurnoverRate()));
            item.setTurnoverDays(scale4(row.getTurnoverDays()));
            item.setStockSalesRate(scale2(row.getStockSalesRate()));
            item.setContributionRate(scale4(row.getContributionRate()));
            item.setGmroi(scale2(row.getGmroi()));
            item.setSalesRate(scale6(row.getSalesRate()));
            item.setActivity(row.getActivity());
            item.setActivitySku(row.getActivity());
            item.setFirstSaleDate(row.getFirstSaleDate());
            item.setNewProduct(row.getNewProduct());
            item.setKeyProduct(row.getKeyProduct());
            item.setSeasonableFlag(row.getSeasonableFlag());
            item.setSeasonableFlagName(row.getSeasonableFlagName());
            item.setSeasonableStartDate(row.getSeasonableStartDate());
            item.setSeasonableEndDate(row.getSeasonableEndDate());
            item.setProductTags(null);
            item.setAllTagName(null);
            item.setClassNo(row.getClassNo());
            item.setClassName(row.getClassName());
            item.setClassLevel(row.getClassLevel());
            item.setProductBarcode(row.getProductBarcode());
            item.setBrandName(row.getBrandName());
            item.setSpec(row.getSpec());
            item.setInPrice(scale2(row.getInPrice()));
            item.setSalesPrice(scale2(row.getSalesPrice()));
            item.setProductVendorNo(row.getProductVendorNo());
            item.setProductVendorName(row.getProductVendorName());
            item.setProductVendorNoName(row.getProductVendorNoName());
            records.add(item);
        }
        return records;
    }

    private DiagnosisSessionCacheModel getSession(String sessionId) {
        DiagnosisSessionCacheModel model = sessionCacheStore.get(sessionId);
        if (model == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "session not found or expired");
        }
        if (model.getQueryHash() == null || model.getQueryHash().isBlank()) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "session payload corrupted");
        }
        return model;
    }

    private DiagnosisOverviewSnapshotRow resolveOverviewSnapshot(String sessionId, DiagnosisSessionCacheModel session) {
        if (session.getDataVersion() != null && !session.getDataVersion().isBlank()) {
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
                for (DiagnosisPrecomputeWindowRow window : windows) {
                    DiagnosisOverviewSnapshotRow overview = snapshotMapper.selectOverviewByQueryAndVersion(
                        TENANT_ID, session.getQueryHash(), window.getDataVersion());
                    if (overview != null) {
                        session.setDataVersion(window.getDataVersion());
                        sessionCacheStore.save(sessionId, session, Duration.ofMinutes(Math.max(1, cacheProperties.getResultTtlMinutes())));
                        return overview;
                    }
                }
            }
        }
        DiagnosisOverviewSnapshotRow latest = snapshotMapper.selectLatestOverviewByQuery(TENANT_ID, session.getQueryHash());
        if (latest == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.DATA_PREPARING, "session result not ready");
        }
        session.setDataVersion(latest.getDataVersion());
        sessionCacheStore.save(sessionId, session, Duration.ofMinutes(Math.max(1, cacheProperties.getResultTtlMinutes())));
        return latest;
    }

    private List<String> normalizeStatusList(List<String> values) {
        List<String> list = normalizeList(values);
        list.remove("-1");
        list.remove("all");
        return list;
    }

    private List<String> normalizeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (String value : values) {
            String normalized = normalizeBlank(value);
            if (normalized != null) {
                result.add(normalized);
            }
        }
        return result;
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeAbcType(String abcType) {
        String value = normalizeBlank(abcType);
        if ("gross".equals(value) || "contribution".equals(value)) {
            return value;
        }
        return "sales";
    }

    private String normalizeRole(String role) {
        String value = normalizeBlank(role);
        if (value == null || "0".equals(value)) {
            return null;
        }
        return switch (value) {
            case "leading", "success", "1" -> "1";
            case "attracting", "sleep", "2" -> "2";
            case "problem", "3" -> "3";
            case "profit", "drainage", "4" -> "4";
            default -> null;
        };
    }

    private String grossRoleName(String role) {
        if ("1".equals(role)) return "领跑商品";
        if ("2".equals(role)) return "吸客商品";
        if ("4".equals(role)) return "利润商品";
        return "问题商品";
    }

    private String gmroiRoleName(String role) {
        if ("1".equals(role)) return "成功商品";
        if ("2".equals(role)) return "沉睡商品";
        if ("4".equals(role)) return "吸客商品";
        return "问题商品";
    }

    private BigDecimal scale2(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scale4(BigDecimal value) {
        return value == null ? null : value.setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal scale6(BigDecimal value) {
        return value == null ? null : value.setScale(6, RoundingMode.HALF_UP);
    }

    private static Map<String, String> buildOrderByMapping() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("productNo", "g.product_no");
        map.put("productName", "g.product_name");
        map.put("handleStatus", "g.product_no");
        map.put("productStatus", "g.product_status_no");
        map.put("storeNum", "g.store_num");
        map.put("currentSalesAbc", "abc.current_abc");
        map.put("compareSalesAbc", "abc.compare_abc");
        map.put("currentGrossAbc", "gross_abc.current_abc");
        map.put("compareGrossAbc", "gross_abc.compare_abc");
        map.put("currentContributionAbc", "contribution_abc.current_abc");
        map.put("compareContributionAbc", "contribution_abc.compare_abc");
        map.put("currentContributionRole", "g.current_gross_role");
        map.put("compareContributionRole", "g.compare_gross_role");
        map.put("currentGmroiRole", "g.current_gmroi_role");
        map.put("compareGmroiRole", "g.compare_gmroi_role");
        map.put("saleQuantity", "g.sale_quantity");
        map.put("saleQuantityPsd", "g.sale_quantity_psd");
        map.put("sales", "g.sales");
        map.put("salesPer", "g.sales_per");
        map.put("salesPsd", "g.sales_psd");
        map.put("gross", "g.gross");
        map.put("grossPer", "g.gross_per");
        map.put("grossPsd", "g.gross_psd");
        map.put("grossRate", "g.gross_rate");
        map.put("stockQuantity", "g.stock_quantity");
        map.put("turnoverRate", "g.turnover_rate");
        map.put("turnoverDays", "g.turnover_days");
        map.put("stockSalesRate", "g.stock_sales_rate");
        map.put("contributionRate", "g.contribution_rate");
        map.put("gmroi", "g.gmroi");
        map.put("salesRate", "g.sales_rate");
        map.put("activitySku", "g.activity");
        map.put("firstSaleDate", "g.first_sale_date");
        map.put("newProduct", "g.new_product");
        map.put("keyProduct", "g.key_product");
        map.put("seasonableFlagName", "g.seasonable_flag_name");
        return map;
    }
}

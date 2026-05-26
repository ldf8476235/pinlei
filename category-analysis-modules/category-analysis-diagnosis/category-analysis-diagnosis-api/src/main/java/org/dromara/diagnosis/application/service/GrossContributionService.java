package org.dromara.diagnosis.application.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.response.LegacyGrossFourQuadrantItemResponse;
import org.dromara.diagnosis.api.response.LegacyGrossFourQuadrantResponse;
import org.dromara.diagnosis.api.response.LegacyGrossSalesListItemResponse;
import org.dromara.diagnosis.api.response.LegacyGrossSalesListResponse;
import org.dromara.diagnosis.api.response.LegacyGrossSalesPerResponse;
import org.dromara.diagnosis.api.response.LegacyGrossSkuChangeResponse;
import org.dromara.diagnosis.api.response.LegacyGrossSkuPerResponse;
import org.dromara.diagnosis.application.config.DiagnosisCacheProperties;
import org.dromara.diagnosis.application.model.DiagnosisSessionCacheModel;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisGrossContributionMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisGrossSalesPerAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisGrossSkuPerAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisGrossSkuRow;
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
public class GrossContributionService {

    private static final String TENANT_ID = "000000";
    private static final Map<String, String> ORDER_BY_MAPPING = buildOrderByMapping();

    private final DiagnosisSnapshotMapper snapshotMapper;
    private final DiagnosisSessionCacheStore sessionCacheStore;
    private final DiagnosisCacheProperties cacheProperties;
    private final DiagnosisPrecomputeMapper precomputeMapper;
    private final DiagnosisGrossContributionMapper grossContributionMapper;

    public LegacyGrossFourQuadrantResponse getGrossFourQuadrant(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);
        List<DiagnosisGrossSkuRow> rows = grossContributionMapper.selectQuadrantList(TENANT_ID, session.getQueryHash(), session.getDataVersion());
        if (rows == null) {
            rows = List.of();
        }

        BigDecimal avgSalesPer = BigDecimal.ZERO;
        BigDecimal avgGrossRate = BigDecimal.ZERO;
        BigDecimal minGrossRate = null;
        BigDecimal maxGrossRate = null;
        int count = 0;
        List<LegacyGrossFourQuadrantItemResponse> list = new ArrayList<>(rows.size());
        for (DiagnosisGrossSkuRow row : rows) {
            LegacyGrossFourQuadrantItemResponse item = new LegacyGrossFourQuadrantItemResponse();
            item.setContributionType(parseRole(row.getCurrentGrossRole()));
            item.setProductNo(row.getProductNo());
            item.setProductName(row.getProductName());
            item.setSales(scale2(row.getSales()));
            item.setSalesPer(scale2(row.getSalesPer()));
            item.setGross(scale2(row.getGross()));
            item.setGrossRate(scale2(row.getGrossRate()));
            list.add(item);

            BigDecimal currentSalesPer = nvl(row.getSalesPer());
            BigDecimal currentGrossRate = nvl(row.getGrossRate());
            avgSalesPer = avgSalesPer.add(currentSalesPer);
            avgGrossRate = avgGrossRate.add(currentGrossRate);
            minGrossRate = minGrossRate == null ? currentGrossRate : minGrossRate.min(currentGrossRate);
            maxGrossRate = maxGrossRate == null ? currentGrossRate : maxGrossRate.max(currentGrossRate);
            count++;
        }

        LegacyGrossFourQuadrantResponse response = new LegacyGrossFourQuadrantResponse();
        response.setSalesPer(count == 0 ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
            : avgSalesPer.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
        response.setGross(count == 0 ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
            : avgGrossRate.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
        response.setList(list);

        int min = minGrossRate == null ? -100 : minGrossRate.setScale(0, RoundingMode.FLOOR).intValue();
        int max = maxGrossRate == null ? 100 : maxGrossRate.setScale(0, RoundingMode.CEILING).intValue();
        response.setScaleX(List.of(min, 0, 17, 19, 21, 26, 35, max));
        return response;
    }

    public LegacyGrossSalesPerResponse getGrossSalesPer(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);
        DiagnosisGrossSalesPerAggRow row = grossContributionMapper.aggregateSalesPer(TENANT_ID, session.getQueryHash(), session.getDataVersion());
        LegacyGrossSalesPerResponse response = new LegacyGrossSalesPerResponse();
        response.setCurrentSalesPer_1(scale2(row == null ? null : row.getCurrentSalesPer1()));
        response.setCurrentSalesPer_2(scale2(row == null ? null : row.getCurrentSalesPer2()));
        response.setCurrentSalesPer_3(scale2(row == null ? null : row.getCurrentSalesPer3()));
        response.setCurrentSalesPer_4(scale2(row == null ? null : row.getCurrentSalesPer4()));
        response.setCompareSalesPer_1(scale2(row == null ? null : row.getCompareSalesPer1()));
        response.setCompareSalesPer_2(scale2(row == null ? null : row.getCompareSalesPer2()));
        response.setCompareSalesPer_3(scale2(row == null ? null : row.getCompareSalesPer3()));
        response.setCompareSalesPer_4(scale2(row == null ? null : row.getCompareSalesPer4()));
        return response;
    }

    public LegacyGrossSkuPerResponse getGrossSkuPer(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);
        DiagnosisGrossSkuPerAggRow row = grossContributionMapper.aggregateSkuPer(TENANT_ID, session.getQueryHash(), session.getDataVersion());
        LegacyGrossSkuPerResponse response = new LegacyGrossSkuPerResponse();
        response.setCurrentSkuPer_1(scale2(row == null ? null : row.getCurrentSkuPer1()));
        response.setCurrentSkuPer_2(scale2(row == null ? null : row.getCurrentSkuPer2()));
        response.setCurrentSkuPer_3(scale2(row == null ? null : row.getCurrentSkuPer3()));
        response.setCurrentSkuPer_4(scale2(row == null ? null : row.getCurrentSkuPer4()));
        response.setCurrentSku_3(row == null || row.getCurrentSku3() == null ? 0 : row.getCurrentSku3());
        response.setCompareSkuPer_1(scale2(row == null ? null : row.getCompareSkuPer1()));
        response.setCompareSkuPer_2(scale2(row == null ? null : row.getCompareSkuPer2()));
        response.setCompareSkuPer_3(scale2(row == null ? null : row.getCompareSkuPer3()));
        response.setCompareSkuPer_4(scale2(row == null ? null : row.getCompareSkuPer4()));
        return response;
    }

    public LegacyGrossSkuChangeResponse getGrossSkuChange(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);
        LegacyGrossSkuChangeResponse response = new LegacyGrossSkuChangeResponse();
        response.setSku_2(nvlInt(grossContributionMapper.countSkuChangeFromRunner(TENANT_ID, session.getQueryHash(), session.getDataVersion(), "2")));
        response.setSku_3(nvlInt(grossContributionMapper.countSkuChangeFromRunner(TENANT_ID, session.getQueryHash(), session.getDataVersion(), "3")));
        response.setSku_4(nvlInt(grossContributionMapper.countSkuChangeFromRunner(TENANT_ID, session.getQueryHash(), session.getDataVersion(), "4")));
        response.setContributionType(null);
        response.setNum(null);
        return response;
    }

    public LegacyGrossSalesListResponse getGrossSalesList(String sessionId,
                                                          List<String> status,
                                                          String promotion,
                                                          String currentGross,
                                                          String compareGross,
                                                          Integer page,
                                                          Integer size,
                                                          String order,
                                                          String orderType) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);
        int actualPage = page == null || page < 1 ? 1 : page;
        int actualSize = size == null || size < 1 ? 10 : Math.min(size, 200);
        int offset = (actualPage - 1) * actualSize;
        String actualOrderBy = ORDER_BY_MAPPING.getOrDefault(order, "sales");
        String actualOrderType = "asc".equalsIgnoreCase(orderType) ? "ASC" : "DESC";
        List<String> statusList = status == null ? new ArrayList<>() : new ArrayList<>(status);
        statusList.remove("-1");
        String currentGrossRole = normalizeGrossRoleFilter(currentGross);
        String compareGrossRole = normalizeGrossRoleFilter(compareGross);

        Long total = grossContributionMapper.countSku(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), currentGrossRole, compareGrossRole, promotion, statusList);
        List<DiagnosisGrossSkuRow> rows = grossContributionMapper.selectSkuPage(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), currentGrossRole, compareGrossRole, promotion, statusList,
            actualOrderBy, actualOrderType, offset, actualSize);
        if (rows == null) {
            rows = List.of();
        }
        List<LegacyGrossSalesListItemResponse> records = new ArrayList<>(rows.size());
        for (DiagnosisGrossSkuRow row : rows) {
            LegacyGrossSalesListItemResponse item = new LegacyGrossSalesListItemResponse();
            item.setProductNo(row.getProductNo());
            item.setProductName(row.getProductName());
            item.setProductStatus(row.getProductStatus());
            item.setProductStatusNo(row.getProductStatusNo());
            item.setStoreNum(row.getStoreNum());
            item.setCurrentGrossRole(row.getCurrentGrossRole());
            item.setCurrentGrossRoleName(roleName(row.getCurrentGrossRole()));
            item.setCompareGrossRole(row.getCompareGrossRole());
            item.setCompareGrossRoleName(roleName(row.getCompareGrossRole()));
            item.setCurrentGmroiRole(row.getCurrentGmroiRole());
            item.setCurrentGmroiRoleName(roleName(row.getCurrentGmroiRole()));
            item.setCompareGmroiRole(row.getCompareGmroiRole());
            item.setCompareGmroiRoleName(roleName(row.getCompareGmroiRole()));
            item.setSaleQuantity(scale2(row.getSaleQuantity()));
            item.setSaleQuantityPsd(scale4(row.getSaleQuantityPsd()));
            item.setSales(scale2(row.getSales()));
            item.setSalesPer(scale2(row.getSalesPer()));
            item.setSalesPsd(scale4(row.getSalesPsd()));
            item.setGross(scale2(row.getGross()));
            item.setGrossPer(scale2(row.getGrossPer()));
            item.setGrossPsd(scale4(row.getGrossPsd()));
            item.setGrossRate(scale2(row.getGrossRate()));
            item.setStockQuantity(scale2(row.getStockQuantity()));
            item.setTurnoverRate(scale4(row.getTurnoverRate()));
            item.setTurnoverDays(scale4(row.getTurnoverDays()));
            item.setStockSalesRate(scale2(row.getStockSalesRate()));
            item.setContributionRate(scale2(row.getContributionRate()));
            item.setGmroi(scale4(row.getGmroi()));
            item.setSalesRate(scale2(row.getSalesRate()));
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
        LegacyGrossSalesListResponse response = new LegacyGrossSalesListResponse();
        response.setRecords(records);
        response.setTotal(total == null ? 0L : total);
        response.setCurrent(actualPage);
        response.setSize(actualSize);
        response.setPages((int) ((response.getTotal() + actualSize - 1) / actualSize));
        return response;
    }

    private String normalizeGrossRoleFilter(String role) {
        if (role == null || role.isBlank() || "0".equals(role)) {
            return null;
        }
        return switch (role.trim()) {
            case "leading", "1" -> "1";
            case "attracting", "2" -> "2";
            case "problem", "3" -> "3";
            case "profit", "4" -> "4";
            default -> null;
        };
    }

    private String roleName(String role) {
        if ("1".equals(role)) {
            return "领跑商品";
        }
        if ("2".equals(role)) {
            return "吸客商品";
        }
        if ("4".equals(role)) {
            return "利润商品";
        }
        return "问题商品";
    }

    private int parseRole(String role) {
        if ("1".equals(role)) {
            return 1;
        }
        if ("2".equals(role)) {
            return 2;
        }
        if ("4".equals(role)) {
            return 4;
        }
        return 3;
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
        DiagnosisOverviewSnapshotRow byJobBinding = resolveBoundJobSnapshot(sessionId, session);
        if (byJobBinding != null) {
            return byJobBinding;
        }

        DiagnosisOverviewSnapshotRow overview = snapshotMapper.selectLatestOverviewByQuery(TENANT_ID, session.getQueryHash());
        if (overview == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT,
                "no diagnosis snapshot found for current query; trigger precompute first");
        }
        if (overview.getDataVersion() != null && !overview.getDataVersion().isBlank()) {
            session.setDataVersion(overview.getDataVersion());
            sessionCacheStore.save(sessionId, session, Duration.ofMinutes(Math.max(1, cacheProperties.getResultTtlMinutes())));
        }
        return overview;
    }

    private DiagnosisOverviewSnapshotRow resolveBoundJobSnapshot(String sessionId, DiagnosisSessionCacheModel session) {
        if (session.getJobId() == null) {
            return null;
        }
        DiagnosisPrecomputeJobRow job = precomputeMapper.selectJobById(TENANT_ID, session.getJobId());
        if (job == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "bound precompute job not found");
        }
        if (!"SUCCESS".equalsIgnoreCase(job.getStatusCode())) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT,
                "session result not ready, precompute job status: " + job.getStatusCode());
        }
        List<DiagnosisPrecomputeWindowRow> windows = precomputeMapper.selectWindowsByJobId(TENANT_ID, session.getJobId());
        String dataVersion = null;
        if (windows != null) {
            for (DiagnosisPrecomputeWindowRow window : windows) {
                if (window != null && window.getDataVersion() != null && !window.getDataVersion().isBlank()) {
                    dataVersion = window.getDataVersion();
                }
            }
        }
        if (dataVersion == null || dataVersion.isBlank()) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "bound precompute job has no data version");
        }
        DiagnosisOverviewSnapshotRow overview = snapshotMapper.selectOverviewByQueryAndVersion(
            TENANT_ID, session.getQueryHash(), dataVersion);
        if (overview == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT,
                "bound precompute result not published, please retry later");
        }
        session.setDataVersion(dataVersion);
        sessionCacheStore.save(sessionId, session, Duration.ofMinutes(Math.max(1, cacheProperties.getResultTtlMinutes())));
        return overview;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private int nvlInt(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal scale2(BigDecimal value) {
        return nvl(value).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scale4(BigDecimal value) {
        return nvl(value).setScale(4, RoundingMode.HALF_UP);
    }

    private static Map<String, String> buildOrderByMapping() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("productNo", "product_no");
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
        return map;
    }
}

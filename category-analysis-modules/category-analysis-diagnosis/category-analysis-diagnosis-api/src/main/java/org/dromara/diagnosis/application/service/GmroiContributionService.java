package org.dromara.diagnosis.application.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.response.LegacyGmroiFourQuadrantItemResponse;
import org.dromara.diagnosis.api.response.LegacyGmroiFourQuadrantResponse;
import org.dromara.diagnosis.api.response.LegacyGmroiSalesListItemResponse;
import org.dromara.diagnosis.api.response.LegacyGmroiSalesListResponse;
import org.dromara.diagnosis.api.response.LegacyGmroiSkuChangeResponse;
import org.dromara.diagnosis.api.response.LegacyGmroiSkuNumResponse;
import org.dromara.diagnosis.api.response.LegacyGmroiSkuPerResponse;
import org.dromara.diagnosis.application.config.DiagnosisCacheProperties;
import org.dromara.diagnosis.application.model.DiagnosisSessionCacheModel;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisGmroiContributionMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisGmroiSkuNumAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisGmroiSkuPerAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisGmroiSkuRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisOverviewSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeWindowRow;
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
public class GmroiContributionService {

    private static final Logger log = LoggerFactory.getLogger(GmroiContributionService.class);
    private static final String TENANT_ID = "000000";
    private static final Map<String, String> ORDER_BY_MAPPING = buildOrderByMapping();

    private final DiagnosisSnapshotMapper snapshotMapper;
    private final DiagnosisSessionCacheStore sessionCacheStore;
    private final DiagnosisCacheProperties cacheProperties;
    private final DiagnosisPrecomputeMapper precomputeMapper;
    private final DiagnosisGmroiContributionMapper gmroiContributionMapper;

    public LegacyGmroiFourQuadrantResponse getGmroiFourQuadrant(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);
        List<DiagnosisGmroiSkuRow> rows = gmroiContributionMapper.selectQuadrantList(TENANT_ID, session.getQueryHash(), session.getDataVersion());
        if (rows == null) {
            rows = List.of();
        }

        BigDecimal grossRateSum = BigDecimal.ZERO;
        BigDecimal turnoverRateSum = BigDecimal.ZERO;
        BigDecimal minX = null;
        BigDecimal maxX = null;
        BigDecimal minY = null;
        BigDecimal maxY = null;
        int count = 0;

        List<LegacyGmroiFourQuadrantItemResponse> list = new ArrayList<>(rows.size());
        for (DiagnosisGmroiSkuRow row : rows) {
            LegacyGmroiFourQuadrantItemResponse item = new LegacyGmroiFourQuadrantItemResponse();
            item.setGmroiType(parseRole(row.getCurrentGmroiRole()));
            item.setProductNo(row.getProductNo());
            item.setProductName(row.getProductName());
            item.setGrossRate(scale2(row.getGrossRate()));
            item.setTurnoverRate(scale4(row.getTurnoverRate()));
            list.add(item);

            BigDecimal x = nvl(row.getTurnoverRate());
            BigDecimal y = nvl(row.getGrossRate());
            turnoverRateSum = turnoverRateSum.add(x);
            grossRateSum = grossRateSum.add(y);
            minX = minX == null ? x : minX.min(x);
            maxX = maxX == null ? x : maxX.max(x);
            minY = minY == null ? y : minY.min(y);
            maxY = maxY == null ? y : maxY.max(y);
            count++;
        }

        LegacyGmroiFourQuadrantResponse response = new LegacyGmroiFourQuadrantResponse();
        response.setGrossRate(count == 0 ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
            : grossRateSum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
        response.setTurnoverRate(count == 0 ? BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP)
            : turnoverRateSum.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP));
        response.setList(list);
        int dynamicMinX = minX == null ? -1 : minX.setScale(0, RoundingMode.FLOOR).intValue();
        int dynamicMaxX = maxX == null ? 27 : maxX.setScale(0, RoundingMode.CEILING).intValue();
        int dynamicMinY = minY == null ? -1 : minY.setScale(0, RoundingMode.FLOOR).intValue();
        int dynamicMaxY = maxY == null ? 17 : maxY.setScale(0, RoundingMode.CEILING).intValue();
        response.setScaleX(buildScaleX(dynamicMinX, dynamicMaxX));
        response.setScaleY(buildScaleY(dynamicMinY, dynamicMaxY));
        return response;
    }

    public LegacyGmroiSkuNumResponse getGmroiSkuNum(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);
        DiagnosisGmroiSkuNumAggRow row = gmroiContributionMapper.aggregateSkuNum(TENANT_ID, session.getQueryHash(), session.getDataVersion());
        LegacyGmroiSkuNumResponse response = new LegacyGmroiSkuNumResponse();
        response.setSku_1(row == null || row.getSku1() == null ? 0 : row.getSku1());
        response.setSku_2(row == null || row.getSku2() == null ? 0 : row.getSku2());
        response.setSku_3(row == null || row.getSku3() == null ? 0 : row.getSku3());
        response.setSku_4(row == null || row.getSku4() == null ? 0 : row.getSku4());
        response.setSkuPer_1(scale4(row == null ? null : row.getSkuPer1()));
        response.setSkuPer_2(scale4(row == null ? null : row.getSkuPer2()));
        response.setSkuPer_3(scale4(row == null ? null : row.getSkuPer3()));
        response.setSkuPer_4(scale4(row == null ? null : row.getSkuPer4()));
        return response;
    }

    public LegacyGmroiSkuPerResponse getGmroiSkuPer(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);
        DiagnosisGmroiSkuPerAggRow row = gmroiContributionMapper.aggregateSkuPer(TENANT_ID, session.getQueryHash(), session.getDataVersion());
        LegacyGmroiSkuPerResponse response = new LegacyGmroiSkuPerResponse();
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

    public LegacyGmroiSkuChangeResponse getGmroiSkuChange(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);
        LegacyGmroiSkuChangeResponse response = new LegacyGmroiSkuChangeResponse();
        response.setSku_2(nvlInt(gmroiContributionMapper.countSkuChangeFromSuccess(TENANT_ID, session.getQueryHash(), session.getDataVersion(), "2")));
        response.setSku_3(nvlInt(gmroiContributionMapper.countSkuChangeFromSuccess(TENANT_ID, session.getQueryHash(), session.getDataVersion(), "3")));
        response.setSku_4(nvlInt(gmroiContributionMapper.countSkuChangeFromSuccess(TENANT_ID, session.getQueryHash(), session.getDataVersion(), "4")));
        response.setGmroiType(null);
        response.setNum(null);
        return response;
    }

    public LegacyGmroiSalesListResponse getGmroiSalesList(String sessionId,
                                                          List<String> status,
                                                          String promotion,
                                                          String currentGmroi,
                                                          String compareGmroi,
                                                          List<String> gmroiList,
                                                          Integer page,
                                                          Integer size,
                                                          String order,
                                                          String orderType) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);
        int actualPage = page == null || page < 1 ? 1 : page;
        int actualSize = size == null || size < 1 ? 10 : Math.min(size, 200);
        int offset = (actualPage - 1) * actualSize;
        String actualOrderType = "asc".equalsIgnoreCase(orderType) ? "ASC" : "DESC";
        List<String> statusList = status == null ? new ArrayList<>() : new ArrayList<>(status);
        statusList.remove("-1");
        List<String> gmroiSegments = gmroiList == null ? List.of("0") : new ArrayList<>(gmroiList);
        String currentGmroiRole = normalizeGmroiRoleFilter(currentGmroi);
        String compareGmroiRole = normalizeGmroiRoleFilter(compareGmroi);
        String actualOrder = ORDER_BY_MAPPING.containsKey(order) ? order : "sales";

        List<DiagnosisGmroiSkuRow> allRows = gmroiContributionMapper.selectQuadrantList(
            TENANT_ID, session.getQueryHash(), session.getDataVersion());
        List<DiagnosisGmroiSkuRow> filteredRows = filterRows(
            allRows, currentGmroiRole, compareGmroiRole, promotion, statusList, gmroiSegments);
        sortRows(filteredRows, actualOrder, actualOrderType);
        int total = filteredRows.size();
        int fromIndex = Math.min(offset, total);
        int toIndex = Math.min(fromIndex + actualSize, total);
        List<DiagnosisGmroiSkuRow> rows = filteredRows.subList(fromIndex, toIndex);

        log.info("query gmroi sku list, sessionId={}, queryHash={}, dataVersion={}, currentGmroi={}, compareGmroi={}, statusSize={}, gmroiSegments={}, page={}, size={}, total={}, returned={}",
            sessionId, session.getQueryHash(), session.getDataVersion(), currentGmroiRole, compareGmroiRole,
            statusList.size(), gmroiSegments, actualPage, actualSize, total, rows.size());

        List<LegacyGmroiSalesListItemResponse> records = new ArrayList<>(rows.size());
        for (DiagnosisGmroiSkuRow row : rows) {
            LegacyGmroiSalesListItemResponse item = new LegacyGmroiSalesListItemResponse();
            item.setProductNo(row.getProductNo());
            item.setProductName(row.getProductName());
            item.setProductStatus(row.getProductStatus());
            item.setProductStatusNo(row.getProductStatusNo());
            item.setStoreNum(row.getStoreNum());
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
            item.setGrossRate(scale6(ratioPercentToDecimal(row.getGrossRate())));
            item.setStockQuantity(scale2(row.getStockQuantity()));
            item.setTurnoverRate(scale2(row.getTurnoverRate()));
            item.setTurnoverDays(scale2(row.getTurnoverDays()));
            item.setStockSalesRate(scale2(row.getStockSalesRate()));
            item.setContributionRate(scale4(row.getContributionRate()));
            item.setGmroi(scale2(row.getGmroi()));
            item.setSalesRate(row.getSalesRate());
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

        LegacyGmroiSalesListResponse response = new LegacyGmroiSalesListResponse();
        response.setRecords(records);
        response.setTotal((long) total);
        response.setCurrent(actualPage);
        response.setSize(actualSize);
        response.setPages((int) ((response.getTotal() + actualSize - 1) / actualSize));
        return response;
    }

    private String normalizeGmroiRoleFilter(String role) {
        if (role == null || role.isBlank() || "0".equals(role)) {
            return null;
        }
        return switch (role.trim()) {
            case "success", "1" -> "1";
            case "sleep", "sleeping", "2" -> "2";
            case "problem", "3" -> "3";
            case "attract", "attracting", "4" -> "4";
            default -> null;
        };
    }

    private List<DiagnosisGmroiSkuRow> filterRows(List<DiagnosisGmroiSkuRow> rows,
                                                  String currentGmroi,
                                                  String compareGmroi,
                                                  String promotion,
                                                  List<String> statusList,
                                                  List<String> gmroiSegments) {
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }
        List<DiagnosisGmroiSkuRow> result = new ArrayList<>(rows.size());
        boolean filterStatus = statusList != null && !statusList.isEmpty();
        boolean filterGmroiSegment = gmroiSegments != null && !gmroiSegments.isEmpty() && !gmroiSegments.contains("0");
        for (DiagnosisGmroiSkuRow row : rows) {
            if (currentGmroi != null && !currentGmroi.equals(row.getCurrentGmroiRole())) {
                continue;
            }
            if (compareGmroi != null && !compareGmroi.equals(row.getCompareGmroiRole())) {
                continue;
            }
            if (!matchPromotion(row, promotion)) {
                continue;
            }
            if (filterStatus && !statusList.contains(row.getProductStatusNo())) {
                continue;
            }
            if (filterGmroiSegment && !matchGmroiSegment(row.getGmroi(), gmroiSegments)) {
                continue;
            }
            result.add(row);
        }
        return result;
    }

    private boolean matchPromotion(DiagnosisGmroiSkuRow row, String promotion) {
        if (promotion == null || promotion.isBlank() || "0".equals(promotion)) {
            return true;
        }
        String flag = row.getPromotionFlag();
        if ("1".equals(promotion) || "Y".equalsIgnoreCase(promotion) || "是".equals(promotion)) {
            return "1".equals(flag) || "Y".equalsIgnoreCase(flag) || "是".equals(flag);
        }
        if ("2".equals(promotion) || "N".equalsIgnoreCase(promotion) || "否".equals(promotion)) {
            return flag == null || flag.isBlank() || "0".equals(flag) || "N".equalsIgnoreCase(flag) || "否".equals(flag);
        }
        return true;
    }

    private boolean matchGmroiSegment(BigDecimal gmroi, List<String> gmroiSegments) {
        BigDecimal value = nvl(gmroi);
        for (String segment : gmroiSegments) {
            if ("1".equals(segment) && value.compareTo(BigDecimal.ONE) <= 0) {
                return true;
            }
            if ("2".equals(segment) && value.compareTo(BigDecimal.ONE) > 0 && value.compareTo(new BigDecimal("2")) <= 0) {
                return true;
            }
            if ("3".equals(segment) && value.compareTo(new BigDecimal("2")) > 0 && value.compareTo(new BigDecimal("3")) <= 0) {
                return true;
            }
            if ("4".equals(segment) && value.compareTo(new BigDecimal("3")) > 0) {
                return true;
            }
        }
        return false;
    }

    private void sortRows(List<DiagnosisGmroiSkuRow> rows, String order, String orderType) {
        Comparator<DiagnosisGmroiSkuRow> comparator = (left, right) -> compareSortValue(sortValue(left, order), sortValue(right, order));
        if (!"ASC".equalsIgnoreCase(orderType)) {
            comparator = comparator.reversed();
        }
        rows.sort(comparator.thenComparing(row -> stringValue(row.getProductNo())));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private int compareSortValue(Comparable left, Comparable right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return left.compareTo(right);
    }

    private Comparable<?> sortValue(DiagnosisGmroiSkuRow row, String order) {
        return switch (order) {
            case "productNo" -> stringValue(row.getProductNo());
            case "productName" -> stringValue(row.getProductName());
            case "productStatus" -> stringValue(row.getProductStatusNo());
            case "storeNum" -> row.getStoreNum();
            case "saleQuantity" -> nvl(row.getSaleQuantity());
            case "saleQuantityPsd" -> nvl(row.getSaleQuantityPsd());
            case "salesPer" -> nvl(row.getSalesPer());
            case "salesPsd" -> nvl(row.getSalesPsd());
            case "gross" -> nvl(row.getGross());
            case "grossPer" -> nvl(row.getGrossPer());
            case "grossPsd" -> nvl(row.getGrossPsd());
            case "grossRate" -> nvl(row.getGrossRate());
            case "stockQuantity" -> nvl(row.getStockQuantity());
            case "turnoverRate" -> nvl(row.getTurnoverRate());
            case "turnoverDays" -> nvl(row.getTurnoverDays());
            case "stockSalesRate" -> nvl(row.getStockSalesRate());
            case "contributionRate" -> nvl(row.getContributionRate());
            case "gmroi" -> nvl(row.getGmroi());
            case "salesRate" -> nvl(row.getSalesRate());
            case "activity" -> stringValue(row.getActivity());
            case "firstSaleDate" -> row.getFirstSaleDate();
            default -> nvl(row.getSales());
        };
    }

    private String stringValue(String value) {
        return value == null ? "" : value;
    }

    private String gmroiRoleName(String role) {
        if ("1".equals(role)) {
            return "成功商品";
        }
        if ("2".equals(role)) {
            return "沉睡商品";
        }
        if ("4".equals(role)) {
            return "吸客商品";
        }
        return "问题商品";
    }

    private String grossRoleName(String role) {
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

    private BigDecimal ratioPercentToDecimal(BigDecimal percentValue) {
        if (percentValue == null) {
            return BigDecimal.ZERO;
        }
        return percentValue.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
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

    private BigDecimal scale6(BigDecimal value) {
        return nvl(value).setScale(6, RoundingMode.HALF_UP);
    }

    private List<Integer> buildScaleX(int rawMin, int rawMax) {
        int min = Math.min(rawMin, 0);
        int max = Math.max(rawMax, 27);
        if (max <= min) {
            max = min + 1;
        }
        return List.of(min, 0, 1, 3, 6, 13, 27, max);
    }

    private List<Integer> buildScaleY(int rawMin, int rawMax) {
        int min = Math.min(rawMin, 0);
        int max = Math.max(rawMax, 17);
        if (max <= min) {
            max = min + 1;
        }
        return List.of(min, 0, 17, max);
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

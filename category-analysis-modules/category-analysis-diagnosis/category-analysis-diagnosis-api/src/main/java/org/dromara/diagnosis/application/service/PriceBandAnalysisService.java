package org.dromara.diagnosis.application.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.response.PriceBandConfigItemResponse;
import org.dromara.diagnosis.api.response.PriceBandDetailItemResponse;
import org.dromara.diagnosis.api.response.PriceBandDetailPageResponse;
import org.dromara.diagnosis.api.response.PriceBandDiagramResponse;
import org.dromara.diagnosis.api.response.PriceBandLinePerformanceResponse;
import org.dromara.diagnosis.api.response.PriceBandPointResponse;
import org.dromara.diagnosis.api.response.PriceBandRangePerformanceResponse;
import org.dromara.diagnosis.api.response.PriceBandRangeSummaryItemResponse;
import org.dromara.diagnosis.api.response.PriceBandRangeSummaryResponse;
import org.dromara.diagnosis.application.config.DiagnosisCacheProperties;
import org.dromara.diagnosis.application.model.DiagnosisSessionCacheModel;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.dromara.diagnosis.domain.enums.PriceBandPromotionType;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPriceBandConfigMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPriceBandMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisOverviewSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeWindowRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPriceBandConfigRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPriceBandLineRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPriceBandPointRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPriceBandRangeRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPriceBandSkuRow;
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
public class PriceBandAnalysisService {

    private static final String TENANT_ID = "000000";
    private static final Map<String, String> RANGE_ORDER_BY_MAPPING = buildRangeOrderByMapping();
    private static final Map<String, String> DETAIL_ORDER_BY_MAPPING = buildDetailOrderByMapping();

    private final DiagnosisSnapshotMapper snapshotMapper;
    private final DiagnosisSessionCacheStore sessionCacheStore;
    private final DiagnosisCacheProperties cacheProperties;
    private final DiagnosisPrecomputeMapper precomputeMapper;
    private final DiagnosisPriceBandMapper priceBandMapper;
    private final DiagnosisPriceBandConfigMapper priceBandConfigMapper;

    public PriceBandDiagramResponse getDiagram(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);

        List<DiagnosisPriceBandRangeRow> rangeRows = safeList(priceBandMapper.selectRangeByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), "price_band_min", "ASC"));
        List<DiagnosisPriceBandLineRow> lineRows = safeList(priceBandMapper.selectLineByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion()));
        List<DiagnosisPriceBandPointRow> pointRows = safeList(priceBandMapper.selectPointByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion()));
        List<DiagnosisPriceBandConfigRow> configRows = safeList(priceBandConfigMapper.selectActiveConfig(TENANT_ID, session.getQueryHash()));

        PriceBandDiagramResponse response = new PriceBandDiagramResponse();
        response.setRangePerformanceList(mapRangePerformance(rangeRows, pointRows));
        response.setLinePerformanceList(mapLinePerformance(lineRows));
        response.setPriceRangeList(mapConfig(configRows, rangeRows));
        response.setPricePointList(mapPoint(pointRows));
        response.setPriceMin(findMin(configRows));
        response.setPriceMax(findMax(configRows));
        response.setPriceLineNum(lineRows.size());
        return response;
    }

    public PriceBandRangeSummaryResponse getRangeSummary(String sessionId, String order, String orderType) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);

        String actualOrderBy = RANGE_ORDER_BY_MAPPING.getOrDefault(order, "price_band_min");
        String actualOrderType = "asc".equalsIgnoreCase(orderType) ? "ASC" : "DESC";
        List<DiagnosisPriceBandRangeRow> rangeRows = safeList(priceBandMapper.selectRangeByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), actualOrderBy, actualOrderType));

        PriceBandRangeSummaryResponse response = new PriceBandRangeSummaryResponse();
        List<PriceBandRangeSummaryItemResponse> list = new ArrayList<>();
        for (DiagnosisPriceBandRangeRow row : rangeRows) {
            PriceBandRangeSummaryItemResponse item = new PriceBandRangeSummaryItemResponse();
            item.setPriceBandMin(scale2(row.getPriceBandMin()));
            item.setPriceBandMax(scale2(row.getPriceBandMax()));
            item.setPriceBand(row.getPriceBandLabel());
            item.setSku(row.getSku());
            item.setSkuPer(scale2(row.getSkuPer()));
            item.setSaleQuantity(scale2(row.getSaleQuantity()));
            item.setSaleQuantityPer(scale2(row.getSaleQuantityPer()));
            item.setSaleQuantityUnit(scale2(row.getSaleQuantityUnit()));
            item.setSales(scale2(row.getSales()));
            item.setSalesPer(scale2(row.getSalesPer()));
            item.setActivitySku(row.getActivitySku());
            item.setSuggestSku(row.getSuggestSku());
            item.setSuggestSkuPer(scale2(row.getSuggestSkuPer()));
            item.setSalePrice(scale2(row.getSalePrice()));
            list.add(item);
        }
        response.setList(list);
        response.setSummaryTwo(mapBandSummary(priceBandMapper.selectRangeByBandLevel(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), "MAIN")));
        response.setSummaryThree(mapBandSummary(priceBandMapper.selectRangeByBandLevel(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), "TAIL")));
        return response;
    }

    public PriceBandDetailPageResponse getDetails(String sessionId,
                                                  List<String> status,
                                                  String promotion,
                                                  List<String> priceBandList,
                                                  Integer page,
                                                  Integer size,
                                                  String order,
                                                  String orderType) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);

        int actualPage = page == null || page < 1 ? 1 : page;
        int actualSize = size == null || size < 1 ? 10 : Math.min(size, 200);
        int offset = (actualPage - 1) * actualSize;
        String actualOrderBy = DETAIL_ORDER_BY_MAPPING.getOrDefault(order, "sales");
        String actualOrderType = "asc".equalsIgnoreCase(orderType) ? "ASC" : "DESC";

        List<String> statusList = normalizeStatusList(status);
        List<String> bandLabels = normalizeBandList(priceBandList);
        String promotionCode = normalizePromotion(promotion);

        Long total = priceBandMapper.countSkuByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), promotionCode, statusList, bandLabels);
        List<DiagnosisPriceBandSkuRow> rows = safeList(priceBandMapper.selectSkuPageByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), promotionCode, statusList, bandLabels,
            actualOrderBy, actualOrderType, offset, actualSize));

        List<PriceBandDetailItemResponse> records = new ArrayList<>();
        for (DiagnosisPriceBandSkuRow row : rows) {
            PriceBandDetailItemResponse item = new PriceBandDetailItemResponse();
            item.setProductNo(row.getProductNo());
            item.setProductName(row.getProductName());
            item.setProductStatus(row.getProductStatus());
            item.setProductStatusNo(row.getProductStatusNo());
            item.setStoreNum(row.getStoreNum());
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
            item.setAvgDealPrice(scale2(row.getAvgDealPrice()));
            item.setProductVendorNo(row.getProductVendorNo());
            item.setProductVendorName(row.getProductVendorName());
            item.setProductVendorNoName(row.getProductVendorNoName());
            records.add(item);
        }

        PriceBandDetailPageResponse response = new PriceBandDetailPageResponse();
        response.setRecords(records);
        response.setTotal(total == null ? 0L : total);
        response.setCurrent(actualPage);
        response.setSize(actualSize);
        response.setPages((int) ((response.getTotal() + actualSize - 1) / actualSize));
        return response;
    }

    public List<PriceBandConfigItemResponse> getConfig(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);
        List<DiagnosisPriceBandConfigRow> configRows = safeList(priceBandConfigMapper.selectActiveConfig(TENANT_ID, session.getQueryHash()));
        List<DiagnosisPriceBandRangeRow> rangeRows = safeList(priceBandMapper.selectRangeByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), "price_band_min", "ASC"));
        return mapConfig(configRows, rangeRows);
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

    private List<PriceBandRangePerformanceResponse> mapRangePerformance(List<DiagnosisPriceBandRangeRow> rangeRows,
                                                                        List<DiagnosisPriceBandPointRow> pointRows) {
        Map<String, DiagnosisPriceBandPointRow> pointMap = new LinkedHashMap<>();
        for (DiagnosisPriceBandPointRow pointRow : pointRows) {
            pointMap.put(buildBandKey(pointRow.getPriceBandMin(), pointRow.getPriceBandMax()), pointRow);
        }
        List<PriceBandRangePerformanceResponse> result = new ArrayList<>();
        for (DiagnosisPriceBandRangeRow row : rangeRows) {
            DiagnosisPriceBandPointRow pointRow = pointMap.get(buildBandKey(row.getPriceBandMin(), row.getPriceBandMax()));
            PriceBandRangePerformanceResponse item = new PriceBandRangePerformanceResponse();
            item.setPriceBandMin(scale2(row.getPriceBandMin()));
            item.setPriceBandMax(scale2(row.getPriceBandMax()));
            item.setSku(row.getSku());
            item.setSales(scale2(row.getSales()));
            item.setSaleQuantity(scale2(row.getSaleQuantity()));
            item.setPriceBandAve(scale2(row.getSalePrice()));
            item.setSalePrice(pointRow == null ? null : scale2(pointRow.getSalePrice()));
            item.setMinSalePrice(scale2(row.getPriceBandMin()));
            item.setTotalSales(pointRow == null ? scale2(row.getSales()) : scale2(pointRow.getTotalSales()));
            item.setWaveType(pointRow == null ? null : pointRow.getWaveType());
            result.add(item);
        }
        return result;
    }

    private List<PriceBandLinePerformanceResponse> mapLinePerformance(List<DiagnosisPriceBandLineRow> rows) {
        List<PriceBandLinePerformanceResponse> result = new ArrayList<>();
        for (DiagnosisPriceBandLineRow row : rows) {
            PriceBandLinePerformanceResponse item = new PriceBandLinePerformanceResponse();
            item.setPriceLine(scale2(row.getPriceLine()));
            item.setSku(row.getSku());
            item.setSales(scale2(row.getSales()));
            result.add(item);
        }
        return result;
    }

    private List<PriceBandPointResponse> mapPoint(List<DiagnosisPriceBandPointRow> rows) {
        List<PriceBandPointResponse> result = new ArrayList<>();
        for (DiagnosisPriceBandPointRow row : rows) {
            PriceBandPointResponse item = new PriceBandPointResponse();
            item.setPriceBandMin(scale2(row.getPriceBandMin()));
            item.setPriceBandMax(scale2(row.getPriceBandMax()));
            item.setSku(row.getSku());
            item.setSales(scale2(row.getSales()));
            item.setSaleQuantity(scale2(row.getSaleQuantity()));
            item.setPriceBandAve(scale2(row.getSalePrice()));
            item.setSalePrice(scale2(row.getSalePrice()));
            item.setMinSalePrice(scale2(row.getPriceBandMin()));
            item.setTotalSales(scale2(row.getTotalSales()));
            item.setWaveType(row.getWaveType());
            result.add(item);
        }
        return result;
    }

    private List<PriceBandConfigItemResponse> mapConfig(List<DiagnosisPriceBandConfigRow> configRows,
                                                        List<DiagnosisPriceBandRangeRow> rangeRows) {
        Map<String, BigDecimal> salesMap = new LinkedHashMap<>();
        for (DiagnosisPriceBandRangeRow rangeRow : rangeRows) {
            salesMap.put(buildBandKey(rangeRow.getPriceBandMin(), rangeRow.getPriceBandMax()), scale2(rangeRow.getSales()));
        }
        List<PriceBandConfigItemResponse> result = new ArrayList<>();
        for (DiagnosisPriceBandConfigRow row : configRows) {
            PriceBandConfigItemResponse item = new PriceBandConfigItemResponse();
            item.setPriceBandMin(scale2(row.getPriceBandMin()));
            item.setPriceBandMax(scale2(row.getPriceBandMax()));
            item.setSales(salesMap.get(buildBandKey(row.getPriceBandMin(), row.getPriceBandMax())));
            result.add(item);
        }
        return result;
    }

    private List<PriceBandConfigItemResponse> mapBandSummary(List<DiagnosisPriceBandRangeRow> rows) {
        List<PriceBandConfigItemResponse> result = new ArrayList<>();
        for (DiagnosisPriceBandRangeRow row : safeList(rows)) {
            PriceBandConfigItemResponse item = new PriceBandConfigItemResponse();
            item.setPriceBandMin(scale2(row.getPriceBandMin()));
            item.setPriceBandMax(scale2(row.getPriceBandMax()));
            item.setSales(scale2(row.getSales()));
            result.add(item);
        }
        return result;
    }

    private List<String> normalizeStatusList(List<String> status) {
        List<String> result = new ArrayList<>();
        if (status == null) {
            return result;
        }
        for (String item : status) {
            if (item == null || item.isBlank() || "-1".equals(item)) {
                continue;
            }
            result.add(item);
        }
        return result;
    }

    private List<String> normalizeBandList(List<String> priceBandList) {
        List<String> result = new ArrayList<>();
        if (priceBandList == null) {
            return result;
        }
        for (String item : priceBandList) {
            if (item == null || item.isBlank()) {
                continue;
            }
            result.add(item.trim());
        }
        return result;
    }

    private String normalizePromotion(String promotion) {
        if (!hasText(promotion) || !PriceBandPromotionType.isValid(promotion)) {
            return PriceBandPromotionType.ALL.getCode();
        }
        return promotion;
    }

    private BigDecimal findMin(List<DiagnosisPriceBandConfigRow> rows) {
        BigDecimal min = null;
        for (DiagnosisPriceBandConfigRow row : rows) {
            if (row == null || row.getPriceBandMin() == null) {
                continue;
            }
            if (min == null || row.getPriceBandMin().compareTo(min) < 0) {
                min = row.getPriceBandMin();
            }
        }
        return scale2(min);
    }

    private BigDecimal findMax(List<DiagnosisPriceBandConfigRow> rows) {
        BigDecimal max = null;
        for (DiagnosisPriceBandConfigRow row : rows) {
            if (row == null || row.getPriceBandMax() == null) {
                continue;
            }
            if (max == null || row.getPriceBandMax().compareTo(max) > 0) {
                max = row.getPriceBandMax();
            }
        }
        return scale2(max);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String buildBandKey(BigDecimal min, BigDecimal max) {
        return scale2(min).toPlainString() + "|" + (max == null ? "NULL" : scale2(max).toPlainString());
    }

    private BigDecimal scale2(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scale4(BigDecimal value) {
        return value == null ? null : value.setScale(4, RoundingMode.HALF_UP);
    }

    private <T> List<T> safeList(List<T> rows) {
        return rows == null ? List.of() : rows;
    }

    private static Map<String, String> buildRangeOrderByMapping() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("priceBandMin", "price_band_min");
        map.put("sku", "sku");
        map.put("skuPer", "sku_per");
        map.put("saleQuantity", "sale_quantity");
        map.put("saleQuantityPer", "sale_quantity_per");
        map.put("sales", "sales");
        map.put("salesPer", "sales_per");
        map.put("activitySku", "activity_sku");
        map.put("suggestSku", "suggest_sku");
        map.put("suggestSkuPer", "suggest_sku_per");
        map.put("salePrice", "sale_price");
        return map;
    }

    private static Map<String, String> buildDetailOrderByMapping() {
        Map<String, String> map = new LinkedHashMap<>();
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
        map.put("avgDealPrice", "avg_deal_price");
        return map;
    }
}

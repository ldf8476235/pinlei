package org.dromara.diagnosis.application.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.request.LegacyClassSalesListRequest;
import org.dromara.diagnosis.api.response.LegacyClassSalesListItemResponse;
import org.dromara.diagnosis.api.response.LegacyClassSalesListResponse;
import org.dromara.diagnosis.application.config.DiagnosisCacheProperties;
import org.dromara.diagnosis.application.model.DiagnosisSessionCacheModel;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisCategorySalesListMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisCategorySalesSkuRow;
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
public class LegacyClassSalesListService {

    private static final String TENANT_ID = "000000";
    private static final Map<String, String> ORDER_BY_MAPPING = buildOrderByMapping();

    private final DiagnosisSnapshotMapper snapshotMapper;
    private final DiagnosisSessionCacheStore sessionCacheStore;
    private final DiagnosisCacheProperties cacheProperties;
    private final DiagnosisPrecomputeMapper precomputeMapper;
    private final DiagnosisCategorySalesListMapper categorySalesListMapper;

    public LegacyClassSalesListResponse getSalesList(LegacyClassSalesListRequest request) {
        if (request == null || !hasText(request.getSessionId())) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "sessionId is required");
        }

        DiagnosisSessionCacheModel session = getSession(request.getSessionId());
        resolveOverviewSnapshot(request.getSessionId(), session);

        int actualPage = request.getPage() == null || request.getPage() < 1 ? 1 : request.getPage();
        int actualSize = request.getSize() == null || request.getSize() < 1 ? 10 : Math.min(request.getSize(), 200);
        int offset = (actualPage - 1) * actualSize;
        String actualOrderBy = ORDER_BY_MAPPING.getOrDefault(request.getOrder(), "sales");
        String actualOrderType = "asc".equalsIgnoreCase(request.getOrderType()) ? "ASC" : "DESC";
        String promotionFlag = resolvePromotionFlag(request.getPromotion());
        List<String> statusList = normalizeStatusList(request.getStatus());

        Long total = categorySalesListMapper.countSku(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), promotionFlag, statusList, null);
        List<DiagnosisCategorySalesSkuRow> rows = categorySalesListMapper.selectSkuPage(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), promotionFlag, statusList, null,
            actualOrderBy, actualOrderType, offset, actualSize);

        List<LegacyClassSalesListItemResponse> content = new ArrayList<>();
        if (rows != null) {
            for (DiagnosisCategorySalesSkuRow row : rows) {
                content.add(toItem(row));
            }
        }

        LegacyClassSalesListResponse response = new LegacyClassSalesListResponse();
        response.setContent(content);
        response.setTotalElements(total == null ? 0L : total);
        response.setList(null);
        response.setTotal(null);
        return response;
    }

    private LegacyClassSalesListItemResponse toItem(DiagnosisCategorySalesSkuRow row) {
        LegacyClassSalesListItemResponse item = new LegacyClassSalesListItemResponse();
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
        item.setSalesRate(row.getSalesRate() == null ? null : scale2(row.getSalesRate()));
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
        item.setInPrice(scale2(row.getInPrice()));
        item.setSalesPrice(scale2(row.getSalesPrice()));
        item.setProductVendorNo(row.getProductVendorNo());
        item.setProductVendorName(row.getProductVendorName());
        item.setProductVendorNoName(row.getProductVendorNoName());
        return item;
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
            if (job == null || !"SUCCESS".equalsIgnoreCase(job.getStatusCode())) {
                throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT,
                    "session result not ready, precompute job status: " + (job == null ? "UNKNOWN" : job.getStatusCode()));
            }
            List<DiagnosisPrecomputeWindowRow> windows = precomputeMapper.selectWindowsByJobId(TENANT_ID, session.getJobId());
            String dataVersion = null;
            if (windows != null) {
                for (DiagnosisPrecomputeWindowRow window : windows) {
                    if (window != null && hasText(window.getDataVersion())) {
                        dataVersion = window.getDataVersion();
                    }
                }
            }
            if (!hasText(dataVersion)) {
                throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "bound precompute job has no data version");
            }
            DiagnosisOverviewSnapshotRow overview = snapshotMapper.selectOverviewByQueryAndVersion(
                TENANT_ID, session.getQueryHash(), dataVersion);
            if (overview == null) {
                throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT,
                    "bound precompute result not published, please retry later");
            }
            session.setDataVersion(dataVersion);
            sessionCacheStore.save(sessionId, session, sessionTtl());
            return overview;
        }

        DiagnosisOverviewSnapshotRow overview = snapshotMapper.selectLatestOverviewByQuery(TENANT_ID, session.getQueryHash());
        if (overview == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT,
                "no diagnosis snapshot found for current query; trigger precompute first");
        }
        if (hasText(overview.getDataVersion())) {
            session.setDataVersion(overview.getDataVersion());
            sessionCacheStore.save(sessionId, session, sessionTtl());
        }
        return overview;
    }

    private String resolvePromotionFlag(String promotion) {
        if (!hasText(promotion) || "0".equals(promotion)) {
            return null;
        }
        return "1".equals(promotion) ? "1" : "2";
    }

    private List<String> normalizeStatusList(List<String> status) {
        List<String> statusList = status == null ? new ArrayList<>() : new ArrayList<>(status);
        statusList.remove("-1");
        return statusList;
    }

    private Duration sessionTtl() {
        return Duration.ofMinutes(Math.max(1, cacheProperties.getResultTtlMinutes()));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
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

    private static Map<String, String> buildOrderByMapping() {
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
}

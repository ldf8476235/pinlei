package org.dromara.diagnosis.application.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.response.LegacySubclassSalesListItemResponse;
import org.dromara.diagnosis.api.response.LegacySubclassSalesListResponse;
import org.dromara.diagnosis.api.response.LegacySubclassSalesPerResponse;
import org.dromara.diagnosis.api.response.LegacySubclassSalesTrendPointResponse;
import org.dromara.diagnosis.api.response.LegacySubclassSalesTrendResponse;
import org.dromara.diagnosis.application.config.DiagnosisCacheProperties;
import org.dromara.diagnosis.application.model.DiagnosisSessionCacheModel;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSubclassContributionMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisOverviewSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeWindowRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSubclassContributionRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSubclassTrendRow;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SubClassContributionService {

    private static final String TENANT_ID = "000000";
    private static final DateTimeFormatter LEGACY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final Map<String, String> ORDER_BY_MAPPING = buildOrderByMapping();

    private final DiagnosisSnapshotMapper snapshotMapper;
    private final DiagnosisSessionCacheStore sessionCacheStore;
    private final DiagnosisCacheProperties cacheProperties;
    private final DiagnosisSubclassContributionMapper subclassContributionMapper;
    private final DiagnosisPrecomputeMapper precomputeMapper;

    public List<LegacySubclassSalesPerResponse> getSalesPer(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);
        List<DiagnosisSubclassContributionRow> rows = subclassContributionMapper.selectContributionByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion());
        if (rows == null) {
            rows = List.of();
        }

        List<LegacySubclassSalesPerResponse> result = new ArrayList<>(rows.size());
        for (DiagnosisSubclassContributionRow row : rows) {
            LegacySubclassSalesPerResponse item = new LegacySubclassSalesPerResponse();
            item.setClassNo(row.getSubClassNo());
            item.setClassName(row.getSubClassName());
            item.setClassLevel(row.getSubClassLevel());
            item.setParentClassNo(row.getParentClassNo());
            item.setSales(row.getCurrentSales());
            item.setSalesPer(row.getCurrentSalesPer());
            result.add(item);
        }
        return result;
    }

    public LegacySubclassSalesTrendResponse getTrendChart(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);
        List<DiagnosisSubclassTrendRow> rows = subclassContributionMapper.selectTrendByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion());
        if (rows == null) {
            rows = List.of();
        }

        List<String> legend = new ArrayList<>();
        List<String> xdata = new ArrayList<>();
        List<LegacySubclassSalesTrendPointResponse> lineDate = new ArrayList<>(rows.size());
        Map<String, Boolean> legendSeen = new LinkedHashMap<>();
        Map<String, Boolean> dateSeen = new LinkedHashMap<>();

        for (DiagnosisSubclassTrendRow row : rows) {
            if (row.getSubClassNo() != null && !legendSeen.containsKey(row.getSubClassNo())) {
                legendSeen.put(row.getSubClassNo(), Boolean.TRUE);
                legend.add((row.getSubClassNo() == null ? "" : row.getSubClassNo())
                    + (row.getSubClassName() == null ? "" : row.getSubClassName()));
            }
            if (row.getPointDate() != null) {
                String formattedDate = LEGACY_DATE_FORMAT.format(row.getPointDate());
                if (!dateSeen.containsKey(formattedDate)) {
                    dateSeen.put(formattedDate, Boolean.TRUE);
                    xdata.add(formattedDate);
                }
            }

            LegacySubclassSalesTrendPointResponse point = new LegacySubclassSalesTrendPointResponse();
            point.setDataDate(row.getPointDate() == null ? null : LEGACY_DATE_FORMAT.format(row.getPointDate()));
            point.setClassNo(row.getSubClassNo());
            point.setClassName(row.getSubClassName());
            point.setSales(row.getSales());
            lineDate.add(point);
        }

        LegacySubclassSalesTrendResponse response = new LegacySubclassSalesTrendResponse();
        response.setLegend(legend);
        response.setLineDate(lineDate);
        response.setXdata(xdata);
        return response;
    }

    public LegacySubclassSalesListResponse getSalesList(String sessionId, Integer page, Integer size, String order, String orderType) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);

        int actualPage = page == null || page < 1 ? 1 : page;
        int actualSize = size == null || size < 1 ? 10 : Math.min(size, 200);
        int offset = (actualPage - 1) * actualSize;
        String actualOrderBy = ORDER_BY_MAPPING.getOrDefault(order, "current_sales");
        String actualOrderType = "asc".equalsIgnoreCase(orderType) ? "ASC" : "DESC";

        List<DiagnosisSubclassContributionRow> rows = subclassContributionMapper.selectContributionPageByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), actualOrderBy, actualOrderType, offset, actualSize);
        Long total = subclassContributionMapper.countContributionByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion());
        if (rows == null) {
            rows = List.of();
        }

        List<LegacySubclassSalesListItemResponse> content = new ArrayList<>(rows.size());
        for (DiagnosisSubclassContributionRow row : rows) {
            LegacySubclassSalesListItemResponse item = new LegacySubclassSalesListItemResponse();
            item.setClassNo(row.getSubClassNo());
            item.setClassName(row.getSubClassName());
            item.setClassLevel(row.getSubClassLevel());
            item.setParentClassNo(row.getParentClassNo());
            item.setCurrentSales(row.getCurrentSales());
            item.setCurrentSalesPer(row.getCurrentSalesPer());
            item.setCurrentGross(row.getCurrentGross());
            item.setCurrentGrossPer(row.getCurrentGrossPer());
            item.setCurrentGrossRate(row.getCurrentGrossRate());
            item.setCurrentSaleQuantity(row.getCurrentSaleQuantity());
            item.setCurrentCustomerCount(row.getCurrentCustomerCount());
            item.setCurrentCustomerPrice(row.getCurrentCustomerPrice());
            item.setCompareSales(row.getCompareSales());
            item.setCompareSalesPer(row.getCompareSalesPer());
            item.setCompareSalesAddRate(row.getCompareSalesAddRate());
            item.setCompareGross(row.getCompareGross());
            item.setCompareGrossPer(row.getCompareGrossPer());
            item.setCompareGrossAddRate(row.getCompareGrossAddRate());
            item.setCompareGrossRate(row.getCompareGrossRate());
            item.setCompareSaleQuantity(row.getCompareSaleQuantity());
            item.setCompareSaleQuantityAddRate(row.getCompareSaleQuantityAddRate());
            item.setCompareCustomerCount(row.getCompareCustomerCount());
            item.setCompareCustomerPrice(row.getCompareCustomerPrice());
            item.setCompareCustomerPriceAddRate(row.getCompareCustomerPriceAddRate());
            item.setCurrentTurnoverRate(row.getCurrentTurnoverRate());
            item.setCurrentTurnoverDays(row.getCurrentTurnoverDays());
            item.setGmroi(row.getCurrentGmroi());
            item.setSaleCost(row.getCurrentSaleCost());
            content.add(item);
        }

        LegacySubclassSalesListResponse response = new LegacySubclassSalesListResponse();
        response.setContent(content);
        response.setTotalElements(total);
        response.setList(null);
        response.setTotal(null);
        return response;
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

    private static Map<String, String> buildOrderByMapping() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("currentSales", "current_sales");
        mapping.put("currentSalesPer", "current_sales_per");
        mapping.put("currentGross", "current_gross");
        mapping.put("currentGrossPer", "current_gross_per");
        mapping.put("currentGrossRate", "current_gross_rate");
        mapping.put("currentSaleQuantity", "current_sale_quantity");
        mapping.put("currentCustomerPrice", "current_customer_price");
        mapping.put("compareSales", "compare_sales");
        mapping.put("compareSalesPer", "compare_sales_per");
        mapping.put("compareSalesAddRate", "compare_sales_add_rate");
        mapping.put("compareGross", "compare_gross");
        mapping.put("compareGrossPer", "compare_gross_per");
        mapping.put("compareGrossAddRate", "compare_gross_add_rate");
        mapping.put("compareGrossRate", "compare_gross_rate");
        mapping.put("compareSaleQuantity", "compare_sale_quantity");
        mapping.put("compareSaleQuantityAddRate", "compare_sale_quantity_add_rate");
        mapping.put("compareCustomerPrice", "compare_customer_price");
        mapping.put("compareCustomerPriceAddRate", "compare_customer_price_add_rate");
        mapping.put("currentTurnoverRate", "current_turnover_rate");
        mapping.put("currentTurnoverDays", "current_turnover_days");
        mapping.put("gmroi", "current_gmroi");
        mapping.put("saleCost", "current_sale_cost");
        return mapping;
    }
}

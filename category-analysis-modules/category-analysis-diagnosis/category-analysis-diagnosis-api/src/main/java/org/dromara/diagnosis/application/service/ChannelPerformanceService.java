package org.dromara.diagnosis.application.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.response.ChannelSalesDetailsItemResponse;
import org.dromara.diagnosis.api.response.ChannelSalesDetailsResponse;
import org.dromara.diagnosis.api.response.ChannelSalesPieItemResponse;
import org.dromara.diagnosis.api.response.ChannelSalesTrendPointResponse;
import org.dromara.diagnosis.api.response.ChannelSalesTrendResponse;
import org.dromara.diagnosis.application.config.DiagnosisCacheProperties;
import org.dromara.diagnosis.application.model.DiagnosisSessionCacheModel;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisChannelPerformanceMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisChannelContributionRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisChannelTrendRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisOverviewSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeWindowRow;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChannelPerformanceService {

    private static final String TENANT_ID = "000000";
    private static final DateTimeFormatter LEGACY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final Map<String, String> ORDER_BY_MAPPING = buildOrderByMapping();

    private final DiagnosisSnapshotMapper snapshotMapper;
    private final DiagnosisSessionCacheStore sessionCacheStore;
    private final DiagnosisCacheProperties cacheProperties;
    private final DiagnosisChannelPerformanceMapper channelPerformanceMapper;
    private final DiagnosisPrecomputeMapper precomputeMapper;

    public List<ChannelSalesPieItemResponse> getSalesPie(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);
        List<DiagnosisChannelContributionRow> rows = channelPerformanceMapper.selectContributionByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion());
        if (rows == null) {
            rows = List.of();
        }

        List<ChannelSalesPieItemResponse> result = new ArrayList<>(rows.size());
        for (DiagnosisChannelContributionRow row : rows) {
            if (row.getCurrentSales() == null || row.getCurrentSales().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                continue;
            }
            ChannelSalesPieItemResponse item = new ChannelSalesPieItemResponse();
            item.setName(row.getChannelName());
            item.setValue(row.getCurrentSales());
            item.setPer(row.getCurrentSalesPer());
            result.add(item);
        }
        return result;
    }

    public ChannelSalesTrendResponse getSalesTrend(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);
        List<DiagnosisChannelTrendRow> rows = channelPerformanceMapper.selectTrendByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion());
        if (rows == null) {
            rows = List.of();
        }

        Map<String, Boolean> xdataSeen = new LinkedHashMap<>();
        List<String> xdata = new ArrayList<>();
        List<ChannelSalesTrendPointResponse> lineDate = new ArrayList<>(rows.size());

        for (DiagnosisChannelTrendRow row : rows) {
            String date = row.getPointDate() == null ? null : LEGACY_DATE_FORMAT.format(row.getPointDate());
            if (date != null && !xdataSeen.containsKey(date)) {
                xdataSeen.put(date, Boolean.TRUE);
                xdata.add(date);
            }

            ChannelSalesTrendPointResponse point = new ChannelSalesTrendPointResponse();
            point.setDataDate(date);
            point.setSaleChannel(row.getSaleChannel());
            point.setOnlineType(row.getOnlineType());
            point.setOnlineName(row.getOnlineName());
            point.setSales(row.getCurrentSales());
            lineDate.add(point);
        }

        ChannelSalesTrendResponse response = new ChannelSalesTrendResponse();
        response.setLineDate(lineDate);
        response.setXdata(xdata);
        return response;
    }

    public ChannelSalesDetailsResponse getSalesDetails(String sessionId, Integer page, Integer size, String order, String orderType) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);

        int actualPage = page == null || page < 1 ? 1 : page;
        int actualSize = size == null || size < 1 ? 10 : Math.min(size, 200);
        int offset = (actualPage - 1) * actualSize;
        String actualOrderBy = ORDER_BY_MAPPING.getOrDefault(order, "current_sales");
        String actualOrderType = "asc".equalsIgnoreCase(orderType) ? "ASC" : "DESC";

        List<DiagnosisChannelContributionRow> rows = channelPerformanceMapper.selectContributionPageByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), actualOrderBy, actualOrderType, offset, actualSize);
        Long total = channelPerformanceMapper.countContributionByVersion(TENANT_ID, session.getQueryHash(), session.getDataVersion());
        if (rows == null) {
            rows = List.of();
        }

        List<ChannelSalesDetailsItemResponse> records = new ArrayList<>(rows.size());
        for (DiagnosisChannelContributionRow row : rows) {
            ChannelSalesDetailsItemResponse item = new ChannelSalesDetailsItemResponse();
            item.setChannelName(row.getChannelName());
            item.setSaleChannel(row.getSaleChannel());
            item.setOnlineType(row.getOnlineType());
            item.setOnlineName(row.getOnlineName());
            item.setCurrentSales(row.getCurrentSales());
            item.setCurrentSalesPer(row.getCurrentSalesPer());
            item.setCurrentGross(row.getCurrentGross());
            item.setCurrentGrossPer(row.getCurrentGrossPer());
            item.setCurrentGrossRate(row.getCurrentGrossRate());
            item.setCurrentCustomerCount(row.getCurrentCustomerCount());
            item.setCurrentCustomerPrice(row.getCurrentCustomerPrice());
            item.setCompareSales(row.getCompareSales());
            item.setCompareSalesPer(row.getCompareSalesPer());
            item.setCompareSalesInc(row.getCompareSalesInc());
            item.setCompareGross(row.getCompareGross());
            item.setCompareGrossPer(row.getCompareGrossPer());
            item.setCompareGrossInc(row.getCompareGrossInc());
            item.setCompareGrossRate(row.getCompareGrossRate());
            item.setCompareCustomerCount(row.getCompareCustomerCount());
            item.setCompareCustomerCountInc(row.getCompareCustomerCountInc());
            item.setCompareCustomerPrice(row.getCompareCustomerPrice());
            item.setCompareCustomerPriceInc(row.getCompareCustomerPriceInc());
            records.add(item);
        }

        ChannelSalesDetailsResponse response = new ChannelSalesDetailsResponse();
        response.setRecords(records);
        response.setTotal(total == null ? 0L : total);
        response.setSize(actualSize);
        response.setCurrent(actualPage);
        long totalPages = (response.getTotal() + actualSize - 1) / actualSize;
        response.setPages((int) totalPages);
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
        mapping.put("compareSales", "compare_sales");
        mapping.put("currentCustomerCount", "current_customer_count");
        mapping.put("compareCustomerCount", "compare_customer_count");
        mapping.put("salesGrowth", "compare_sales_inc");
        mapping.put("customerGrowth", "compare_customer_count_inc");
        return mapping;
    }
}

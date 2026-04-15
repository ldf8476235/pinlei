package org.dromara.diagnosis.application.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.response.CustomerAgeBucketResponse;
import org.dromara.diagnosis.api.response.CustomerSalesDetailsItemResponse;
import org.dromara.diagnosis.api.response.CustomerSalesDetailsResponse;
import org.dromara.diagnosis.api.response.CustomerSalesRadarItemResponse;
import org.dromara.diagnosis.application.config.DiagnosisCacheProperties;
import org.dromara.diagnosis.application.model.DiagnosisSessionCacheModel;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisBatchSourceMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisCustomerAnalysisMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisCustomerAgeSummaryRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisCustomerContributionRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisDictRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisOverviewSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeWindowRow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerAnalysisService {

    private static final String TENANT_ID = "000000";
    private static final String AGE_DICT_TYPE = "diag_customer_age_bucket";
    private static final Map<String, String> ORDER_BY_MAPPING = buildOrderByMapping();

    private final DiagnosisSnapshotMapper snapshotMapper;
    private final DiagnosisSessionCacheStore sessionCacheStore;
    private final DiagnosisCacheProperties cacheProperties;
    private final DiagnosisCustomerAnalysisMapper customerAnalysisMapper;
    private final DiagnosisBatchSourceMapper batchSourceMapper;
    private final DiagnosisPrecomputeMapper precomputeMapper;

    public List<CustomerAgeBucketResponse> getAgeBuckets(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);

        List<DiagnosisDictRow> rows = batchSourceMapper.selectOnlineChannelDictRows(List.of(AGE_DICT_TYPE));
        List<CustomerAgeBucketResponse> result = new ArrayList<>();
        if (rows != null) {
            for (DiagnosisDictRow row : rows) {
                CustomerAgeBucketResponse item = toAgeBucket(row);
                if (item != null) {
                    result.add(item);
                }
            }
        }
        result.sort(Comparator.comparingInt(v -> v.getOrderNumber() == null ? 999 : v.getOrderNumber()));
        return result;
    }

    public List<CustomerSalesRadarItemResponse> getSalesRadar(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);
        List<DiagnosisCustomerContributionRow> rows = customerAnalysisMapper.selectContributionByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion());
        if (rows == null) {
            rows = List.of();
        }

        List<CustomerSalesRadarItemResponse> result = new ArrayList<>();
        for (Integer gender : List.of(1, 2, 3)) {
            List<DiagnosisCustomerContributionRow> genderRows = rows.stream()
                .filter(v -> v != null && gender.equals(v.getGender()))
                .sorted(Comparator.comparing(v -> v.getAgeOrder() == null ? 999 : v.getAgeOrder()))
                .toList();
            if (genderRows.isEmpty()) {
                continue;
            }
            result.add(toRadar(sumRows(genderRows, "ALL", "全部", null, null, 0), gender));
            for (DiagnosisCustomerContributionRow row : genderRows) {
                result.add(toRadar(row, gender));
            }
        }
        return result;
    }

    public CustomerSalesDetailsResponse getSalesDetails(String sessionId, Integer page, Integer size, String order, String orderType) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        resolveOverviewSnapshot(sessionId, session);

        int actualPage = page == null || page < 1 ? 1 : page;
        int actualSize = size == null || size < 1 ? 10 : Math.min(size, 200);
        int offset = (actualPage - 1) * actualSize;
        String actualOrderBy = ORDER_BY_MAPPING.getOrDefault(order, "age_order");
        String actualOrderType = "asc".equalsIgnoreCase(orderType) ? "ASC" : "DESC";

        List<DiagnosisCustomerAgeSummaryRow> pageRows = customerAnalysisMapper.selectSummaryPageByVersion(
            TENANT_ID, session.getQueryHash(), session.getDataVersion(), actualOrderBy, actualOrderType, offset, actualSize);
        Long total = customerAnalysisMapper.countSummaryByVersion(TENANT_ID, session.getQueryHash(), session.getDataVersion());

        List<CustomerSalesDetailsItemResponse> pageRecords = new ArrayList<>();
        if (actualPage == 1) {
            DiagnosisCustomerAgeSummaryRow totalRow = customerAnalysisMapper.selectSummaryTotalByVersion(
                TENANT_ID, session.getQueryHash(), session.getDataVersion());
            if (totalRow != null) {
                pageRecords.add(toDetails(totalRow));
            }
        }
        if (pageRows != null) {
            for (DiagnosisCustomerAgeSummaryRow row : pageRows) {
                pageRecords.add(toDetails(row));
            }
        }

        CustomerSalesDetailsResponse response = new CustomerSalesDetailsResponse();
        response.setRecords(pageRecords);
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

    private DiagnosisCustomerContributionRow sumRows(List<DiagnosisCustomerContributionRow> rows,
                                                     String ageCode,
                                                     String ageName,
                                                     Integer ageStart,
                                                     Integer ageEnd,
                                                     Integer ageOrder) {
        DiagnosisCustomerContributionRow sum = new DiagnosisCustomerContributionRow();
        sum.setAgeBucketCode(ageCode);
        sum.setAgeBucketName(ageName);
        sum.setAgeStart(ageStart);
        sum.setAgeEnd(ageEnd);
        sum.setAgeOrder(ageOrder);

        BigDecimal currentSales = BigDecimal.ZERO;
        BigDecimal currentCustomerCount = BigDecimal.ZERO;
        BigDecimal currentSaleQuantity = BigDecimal.ZERO;
        BigDecimal compareSales = BigDecimal.ZERO;
        BigDecimal compareCustomerCount = BigDecimal.ZERO;
        BigDecimal compareSaleQuantity = BigDecimal.ZERO;
        for (DiagnosisCustomerContributionRow row : rows) {
            if (row == null) {
                continue;
            }
            currentSales = currentSales.add(nvl(row.getCurrentSales()));
            currentCustomerCount = currentCustomerCount.add(nvl(row.getCurrentCustomerCount()));
            currentSaleQuantity = currentSaleQuantity.add(nvl(row.getCurrentSaleQuantity()));
            compareSales = compareSales.add(nvl(row.getCompareSales()));
            compareCustomerCount = compareCustomerCount.add(nvl(row.getCompareCustomerCount()));
            compareSaleQuantity = compareSaleQuantity.add(nvl(row.getCompareSaleQuantity()));
        }
        sum.setCurrentSales(currentSales);
        sum.setCurrentCustomerCount(currentCustomerCount);
        sum.setCurrentSaleQuantity(currentSaleQuantity);
        sum.setCurrentCustomerPrice(rate(currentSales, currentCustomerCount));
        sum.setCurrentUnitPrice(rate(currentSales, currentSaleQuantity));
        sum.setCurrentCountAve(rate(currentSaleQuantity, currentCustomerCount));
        sum.setCompareSales(compareSales);
        sum.setCompareCustomerCount(compareCustomerCount);
        sum.setCompareSaleQuantity(compareSaleQuantity);
        sum.setCompareCustomerPrice(rate(compareSales, compareCustomerCount));
        sum.setCompareUnitPrice(rate(compareSales, compareSaleQuantity));
        sum.setCompareCountAve(rate(compareSaleQuantity, compareCustomerCount));
        sum.setSalesGrowth(growth(sum.getCompareSales(), sum.getCurrentSales()));
        sum.setCustomerGrowth(growth(sum.getCompareCustomerCount(), sum.getCurrentCustomerCount()));
        sum.setCustomerPriceGrowth(growth(sum.getCompareCustomerPrice(), sum.getCurrentCustomerPrice()));
        sum.setUnitPriceGrowth(growth(sum.getCompareUnitPrice(), sum.getCurrentUnitPrice()));
        sum.setCountAveGrowth(growth(sum.getCompareCountAve(), sum.getCurrentCountAve()));
        sum.setSaleQuantityGrowth(growth(sum.getCompareSaleQuantity(), sum.getCurrentSaleQuantity()));
        return sum;
    }

    private CustomerSalesDetailsItemResponse toDetails(DiagnosisCustomerAgeSummaryRow total) {
        CustomerSalesDetailsItemResponse item = new CustomerSalesDetailsItemResponse();
        item.setAgeCode(total.getAgeCode());
        item.setAgeName(total.getAgeName());
        item.setAgeStart(total.getAgeStart());
        item.setAgeEnd(total.getAgeEnd());
        item.setAgeOrder(total.getAgeOrder());
        item.setCurrentSales(scale(total.getCurrentSales()));
        item.setCurrentCustomerCount(scale(total.getCurrentCustomerCount()));
        item.setCurrentCustomerPrice(scale(total.getCurrentCustomerPrice()));
        item.setCurrentUnitPrice(scale(total.getCurrentUnitPrice()));
        item.setCurrentCountAve(scale(total.getCurrentCountAve()));
        item.setCurrentSaleQuantity(scale(total.getCurrentSaleQuantity()));
        item.setCompareSales(scale(total.getCompareSales()));
        item.setCompareCustomerCount(scale(total.getCompareCustomerCount()));
        item.setCompareCustomerPrice(scale(total.getCompareCustomerPrice()));
        item.setCompareUnitPrice(scale(total.getCompareUnitPrice()));
        item.setCompareCountAve(scale(total.getCompareCountAve()));
        item.setCompareSaleQuantity(scale(total.getCompareSaleQuantity()));
        item.setSalesGrowth(scale(total.getSalesGrowth()));
        item.setCustomerGrowth(scale(total.getCustomerGrowth()));
        item.setCustomerPriceGrowth(scale(total.getCustomerPriceGrowth()));
        item.setUnitPriceGrowth(scale(total.getUnitPriceGrowth()));
        item.setCountAveGrowth(scale(total.getCountAveGrowth()));
        item.setSaleQuantityGrowth(scale(total.getSaleQuantityGrowth()));
        item.setCurrentManSales(scale(total.getCurrentManSales()));
        item.setCurrentManCustomerCount(scale(total.getCurrentManCustomerCount()));
        item.setCurrentManCustomerPrice(scale(total.getCurrentManCustomerPrice()));
        item.setCurrentManUnitPrice(scale(total.getCurrentManUnitPrice()));
        item.setCurrentManCountAve(scale(total.getCurrentManCountAve()));
        item.setCurrentWomanSales(scale(total.getCurrentWomanSales()));
        item.setCurrentWomanCustomerCount(scale(total.getCurrentWomanCustomerCount()));
        item.setCurrentWomanCustomerPrice(scale(total.getCurrentWomanCustomerPrice()));
        item.setCurrentWomanUnitPrice(scale(total.getCurrentWomanUnitPrice()));
        item.setCurrentWomanCountAve(scale(total.getCurrentWomanCountAve()));
        item.setCurrentUnknownSales(scale(total.getCurrentUnknownSales()));
        item.setCurrentUnknownCustomerCount(scale(total.getCurrentUnknownCustomerCount()));
        item.setCurrentUnknownCustomerPrice(scale(total.getCurrentUnknownCustomerPrice()));
        item.setCurrentUnknownUnitPrice(scale(total.getCurrentUnknownUnitPrice()));
        item.setCurrentUnknownCountAve(scale(total.getCurrentUnknownCountAve()));
        return item;
    }

    private CustomerSalesRadarItemResponse toRadar(DiagnosisCustomerContributionRow row, Integer gender) {
        CustomerSalesRadarItemResponse item = new CustomerSalesRadarItemResponse();
        item.setGender(gender);
        item.setAgeCode(row.getAgeBucketCode());
        item.setAgeName(row.getAgeBucketName());
        item.setAgeStart(row.getAgeStart());
        item.setAgeEnd(row.getAgeEnd());
        item.setCurrentSales(scale(row.getCurrentSales()));
        item.setCurrentCustomerCount(scale(row.getCurrentCustomerCount()));
        item.setCurrentCustomerPrice(scale(row.getCurrentCustomerPrice()));
        item.setCurrentUnitPrice(scale(row.getCurrentUnitPrice()));
        item.setCurrentCountAve(scale(row.getCurrentCountAve()));
        item.setCurrentSaleQuantity(scale(row.getCurrentSaleQuantity()));
        item.setCompareSales(scale(row.getCompareSales()));
        item.setCompareCustomerCount(scale(row.getCompareCustomerCount()));
        item.setCompareCustomerPrice(scale(row.getCompareCustomerPrice()));
        item.setCompareUnitPrice(scale(row.getCompareUnitPrice()));
        item.setCompareCountAve(scale(row.getCompareCountAve()));
        item.setCompareSaleQuantity(scale(row.getCompareSaleQuantity()));
        item.setSalesGrowth(scale(row.getSalesGrowth()));
        item.setCustomerGrowth(scale(row.getCustomerGrowth()));
        return item;
    }

    private CustomerAgeBucketResponse toAgeBucket(DiagnosisDictRow row) {
        if (row == null || row.getDictValue() == null || row.getDictValue().isBlank()) {
            return null;
        }
        String[] values = row.getDictValue().trim().split("-");
        if (values.length != 2) {
            return null;
        }
        Integer start = null;
        Integer end = null;
        try {
            if (!values[0].trim().isEmpty()) {
                start = Integer.parseInt(values[0].trim());
            }
            if (!values[1].trim().isEmpty()) {
                end = Integer.parseInt(values[1].trim());
            }
        } catch (NumberFormatException ex) {
            return null;
        }
        CustomerAgeBucketResponse response = new CustomerAgeBucketResponse();
        response.setAgeCode(row.getDictValue());
        response.setAgeName(row.getDictLabel());
        response.setAgeStart(start);
        response.setAgeEnd(end);
        response.setOrderNumber(row.getDictSort());
        return response;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal rate(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return nvl(numerator).divide(denominator, 6, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal growth(BigDecimal compare, BigDecimal current) {
        if (compare == null || compare.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return nvl(current).subtract(compare)
            .multiply(new BigDecimal("100"))
            .divide(compare, 6, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal scale(BigDecimal value) {
        return nvl(value).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private static Map<String, String> buildOrderByMapping() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("currentSales", "current_sales");
        mapping.put("compareSales", "compare_sales");
        mapping.put("currentCustomerCount", "current_customer_count");
        mapping.put("compareCustomerCount", "compare_customer_count");
        mapping.put("salesGrowth", "sales_growth");
        mapping.put("customerGrowth", "customer_growth");
        mapping.put("ageOrder", "age_order");
        return mapping;
    }
}

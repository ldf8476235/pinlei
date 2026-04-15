package org.dromara.diagnosis.application.batch.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.application.batch.model.DiagnosisFinalizeContext;
import org.dromara.diagnosis.application.batch.model.DiagnosisVipFinalizeResult;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisBatchSourceMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisCustomerAnalysisMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisCustomerContributionRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisDictRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceVipGenderAgeAggRow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiagnosisVipAnalysisFinalizeService {

    private static final String TENANT_ID = "000000";
    private static final String AGE_DICT_TYPE = "diag_customer_age_bucket";
    private static final AgeBucket UNKNOWN_BUCKET = new AgeBucket("UNKNOWN", "未知年龄段", null, null, 999);

    private final DiagnosisBatchSourceMapper batchSourceMapper;
    private final DiagnosisCustomerAnalysisMapper customerAnalysisMapper;
    private final DiagnosisFinalizeSupport finalizeSupport;

    public DiagnosisVipFinalizeResult finalizeVipAnalysis(DiagnosisFinalizeContext context) {
        customerAnalysisMapper.deleteContributionByVersion(TENANT_ID, context.getQueryHash(), context.getDataVersion());

        List<AgeBucket> configuredBuckets = loadAgeBuckets();
        List<AgeBucket> buckets = new ArrayList<>(configuredBuckets);
        buckets.add(UNKNOWN_BUCKET);

        List<DiagnosisSourceVipGenderAgeAggRow> currentRows = safeRows(
            batchSourceMapper.aggregateVipByGenderAndAge(context.getParam(), context.getPeriodEnd()));
        List<DiagnosisSourceVipGenderAgeAggRow> compareRows = context.getCompareParam() == null
            ? List.of()
            : safeRows(batchSourceMapper.aggregateVipByGenderAndAge(context.getCompareParam(), context.getCompareEnd()));

        Map<GroupKey, AggValue> currentMap = toAggMap(currentRows, buckets);
        Map<GroupKey, AggValue> compareMap = toAggMap(compareRows, buckets);

        List<DiagnosisCustomerContributionRow> rows = new ArrayList<>();
        LocalDateTime snapshotTime = LocalDateTime.now();
        List<Integer> genders = List.of(1, 2, 3);
        for (AgeBucket bucket : buckets) {
            for (Integer gender : genders) {
                AggValue current = currentMap.getOrDefault(new GroupKey(gender, bucket.code()), AggValue.zero());
                AggValue compare = compareMap.getOrDefault(new GroupKey(gender, bucket.code()), AggValue.zero());

                BigDecimal currentCustomerPrice = rate(current.sales(), current.customerCount());
                BigDecimal currentUnitPrice = rate(current.sales(), current.saleQuantity());
                BigDecimal currentCountAve = rate(current.saleQuantity(), current.customerCount());

                BigDecimal compareCustomerPrice = rate(compare.sales(), compare.customerCount());
                BigDecimal compareUnitPrice = rate(compare.sales(), compare.saleQuantity());
                BigDecimal compareCountAve = rate(compare.saleQuantity(), compare.customerCount());

                DiagnosisCustomerContributionRow row = new DiagnosisCustomerContributionRow();
                row.setTenantId(TENANT_ID);
                row.setQueryHash(context.getQueryHash());
                row.setDataVersion(context.getDataVersion());
                row.setGender(gender);
                row.setAgeBucketCode(bucket.code());
                row.setAgeBucketName(bucket.name());
                row.setAgeStart(bucket.start());
                row.setAgeEnd(bucket.end());
                row.setAgeOrder(bucket.orderNo());
                row.setRetailTypeId(context.getParam().getRetailTypeId());
                row.setDeptId(finalizeSupport.toLong(context.getParam().getDeptId()));
                row.setBusinessCircleId(context.getParam().getBusinessCircleId());
                row.setDeptGroupId(context.getParam().getDeptGroupId());
                row.setStoreNo(context.getParam().getStoreNo());
                row.setPeriodStart(context.getPeriodStart());
                row.setPeriodEnd(context.getPeriodEnd());
                row.setCompareStart(context.getCompareStart());
                row.setCompareEnd(context.getCompareEnd());
                row.setCurrentSales(money(current.sales()));
                row.setCurrentCustomerCount(count(current.customerCount()));
                row.setCurrentCustomerPrice(currentCustomerPrice);
                row.setCurrentUnitPrice(currentUnitPrice);
                row.setCurrentCountAve(currentCountAve);
                row.setCurrentSaleQuantity(count(current.saleQuantity()));
                row.setCompareSales(money(compare.sales()));
                row.setCompareCustomerCount(count(compare.customerCount()));
                row.setCompareCustomerPrice(compareCustomerPrice);
                row.setCompareUnitPrice(compareUnitPrice);
                row.setCompareCountAve(compareCountAve);
                row.setCompareSaleQuantity(count(compare.saleQuantity()));
                row.setSalesGrowth(growthRate(compare.sales(), current.sales()));
                row.setCustomerGrowth(growthRate(compare.customerCount(), current.customerCount()));
                row.setCustomerPriceGrowth(growthRate(compareCustomerPrice, currentCustomerPrice));
                row.setUnitPriceGrowth(growthRate(compareUnitPrice, currentUnitPrice));
                row.setCountAveGrowth(growthRate(compareCountAve, currentCountAve));
                row.setSaleQuantityGrowth(growthRate(compare.saleQuantity(), current.saleQuantity()));
                row.setSnapshotTime(snapshotTime);
                rows.add(row);
            }
        }

        if (!rows.isEmpty()) {
            customerAnalysisMapper.batchInsertContribution(rows);
        }

        return DiagnosisVipFinalizeResult.builder()
            .detailRows(rows.size())
            .build();
    }

    private List<AgeBucket> loadAgeBuckets() {
        List<DiagnosisDictRow> dictRows = batchSourceMapper.selectOnlineChannelDictRows(List.of(AGE_DICT_TYPE));
        List<AgeBucket> buckets = new ArrayList<>();
        if (dictRows != null) {
            for (DiagnosisDictRow row : dictRows) {
                AgeBucket bucket = parseBucket(row);
                if (bucket != null) {
                    buckets.add(bucket);
                }
            }
        }
        if (buckets.isEmpty()) {
            buckets.add(new AgeBucket("LE_20", "<=20", null, 20, 1));
            buckets.add(new AgeBucket("21_30", "21-30", 21, 30, 2));
            buckets.add(new AgeBucket("31_40", "31-40", 31, 40, 3));
            buckets.add(new AgeBucket("41_50", "41-50", 41, 50, 4));
            buckets.add(new AgeBucket("51_60", "51-60", 51, 60, 5));
            buckets.add(new AgeBucket("GE_61", ">=61", 61, null, 6));
        }
        buckets.sort(Comparator.comparingInt(AgeBucket::orderNo));
        return buckets;
    }

    private AgeBucket parseBucket(DiagnosisDictRow row) {
        if (row == null || row.getDictValue() == null || row.getDictValue().isBlank()) {
            return null;
        }
        String value = row.getDictValue().trim();
        String[] parts = value.split("-");
        Integer start = null;
        Integer end = null;
        if (parts.length != 2) {
            return null;
        }
        String s1 = parts[0].trim();
        String s2 = parts[1].trim();
        try {
            if (!s1.isEmpty()) {
                start = Integer.parseInt(s1);
            }
            if (!s2.isEmpty()) {
                end = Integer.parseInt(s2);
            }
        } catch (NumberFormatException ex) {
            return null;
        }
        String code = value;
        String label = row.getDictLabel() == null || row.getDictLabel().isBlank() ? value : row.getDictLabel().trim();
        Integer order = row.getDictSort() == null ? 999 : row.getDictSort();
        return new AgeBucket(code, label, start, end, order);
    }

    private List<DiagnosisSourceVipGenderAgeAggRow> safeRows(List<DiagnosisSourceVipGenderAgeAggRow> rows) {
        return rows == null ? List.of() : rows;
    }

    private Map<GroupKey, AggValue> toAggMap(List<DiagnosisSourceVipGenderAgeAggRow> rows, List<AgeBucket> buckets) {
        Map<GroupKey, AggValue> map = new LinkedHashMap<>();
        for (DiagnosisSourceVipGenderAgeAggRow row : rows) {
            if (row == null) {
                continue;
            }
            Integer gender = normalizeGender(row.getGender());
            AgeBucket bucket = resolveBucket(buckets, row.getAgeValue());
            GroupKey key = new GroupKey(gender, bucket.code());
            AggValue current = map.getOrDefault(key, AggValue.zero());
            map.put(key, new AggValue(
                current.sales().add(finalizeSupport.nvl(row.getTotalSales())),
                current.saleQuantity().add(finalizeSupport.nvl(row.getTotalSaleQuantity())),
                current.customerCount().add(finalizeSupport.nvl(row.getTotalCustomerCount()))
            ));
        }
        return map;
    }

    private Integer normalizeGender(Integer gender) {
        if (gender == null) {
            return 3;
        }
        return (gender == 1 || gender == 2) ? gender : 3;
    }

    private AgeBucket resolveBucket(List<AgeBucket> buckets, Integer ageValue) {
        if (ageValue == null || ageValue < 0) {
            return UNKNOWN_BUCKET;
        }
        for (AgeBucket bucket : buckets) {
            if (UNKNOWN_BUCKET.code().equals(bucket.code())) {
                continue;
            }
            boolean left = bucket.start() == null || ageValue >= bucket.start();
            boolean right = bucket.end() == null || ageValue <= bucket.end();
            if (left && right) {
                return bucket;
            }
        }
        return UNKNOWN_BUCKET;
    }

    private BigDecimal money(BigDecimal value) {
        return finalizeSupport.round2(finalizeSupport.nvl(value));
    }

    private BigDecimal count(BigDecimal value) {
        return finalizeSupport.round2(finalizeSupport.nvl(value));
    }

    private BigDecimal rate(BigDecimal numerator, BigDecimal denominator) {
        return finalizeSupport.round2(finalizeSupport.divideOrZero(
            finalizeSupport.nvl(numerator), finalizeSupport.nvl(denominator)));
    }

    private BigDecimal growthRate(BigDecimal compareValue, BigDecimal currentValue) {
        return finalizeSupport.round2(finalizeSupport.nvl(finalizeSupport.calcGrowthRate(
            finalizeSupport.nvl(compareValue), finalizeSupport.nvl(currentValue))));
    }

    private record AgeBucket(String code, String name, Integer start, Integer end, Integer orderNo) {
    }

    private record GroupKey(Integer gender, String ageCode) {
    }

    private record AggValue(BigDecimal sales, BigDecimal saleQuantity, BigDecimal customerCount) {
        static AggValue zero() {
            return new AggValue(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
    }
}

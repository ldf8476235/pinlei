package org.dromara.diagnosis.application.batch.service;

import lombok.RequiredArgsConstructor;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.diagnosis.application.batch.model.DiagnosisFinalizeContext;
import org.dromara.diagnosis.application.batch.model.DiagnosisTagFinalizeResult;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisBatchSourceMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisTagMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceTagAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceTagMetaRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceTagStockRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisTagJsonRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisTagMetricRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiagnosisTagFinalizeService {

    private static final String TENANT_ID = "000000";
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final int SUMMARY_LIMIT = 5;
    private static final int INSERT_BATCH_SIZE = 30;

    private final DiagnosisBatchSourceMapper batchSourceMapper;
    private final DiagnosisTagMapper tagMapper;

    @Transactional(rollbackFor = Exception.class)
    public DiagnosisTagFinalizeResult finalizeTag(DiagnosisFinalizeContext context) {
        List<DiagnosisSourceTagAggRow> currentAggRows = safeList(batchSourceMapper.aggregateTagMetrics(context.getParam()));
        List<DiagnosisSourceTagAggRow> compareAggRows = context.getCompareParam() == null
            ? List.of()
            : safeList(batchSourceMapper.aggregateTagMetrics(context.getCompareParam()));
        List<DiagnosisSourceTagStockRow> currentStockRows = safeList(batchSourceMapper.aggregateTagStock(context.getParam()));
        List<DiagnosisSourceTagStockRow> compareStockRows = context.getCompareParam() == null
            ? List.of()
            : safeList(batchSourceMapper.aggregateTagStock(context.getCompareParam()));
        List<DiagnosisSourceTagMetaRow> metaRows = safeList(batchSourceMapper.selectTagMeta(context.getParam()));

        tagMapper.deleteByVersion(TENANT_ID, context.getQueryHash(), context.getDataVersion());

        Map<String, DiagnosisSourceTagAggRow> currentMap = toAggMap(currentAggRows);
        Map<String, DiagnosisSourceTagAggRow> compareMap = toAggMap(compareAggRows);
        Map<String, DiagnosisSourceTagStockRow> currentStockMap = toStockMap(currentStockRows);
        Map<String, DiagnosisSourceTagStockRow> compareStockMap = toStockMap(compareStockRows);
        Map<String, DiagnosisSourceTagMetaRow> metaMap = toMetaMap(metaRows);

        LinkedHashMap<String, TagMetricCalc> calcMap = new LinkedHashMap<>();
        mergeKeys(calcMap, currentMap, compareMap, currentStockMap, compareStockMap, metaMap, context);

        List<TagMetricCalc> calcs = new ArrayList<>(calcMap.values());
        calcs.sort(Comparator.comparing(TagMetricCalc::getSales, Comparator.nullsFirst(BigDecimal::compareTo)).reversed()
            .thenComparing(TagMetricCalc::getMetricKey, Comparator.nullsFirst(String::compareTo)));

        Totals totals = computeTotals(calcs);
        LocalDateTime snapshotTime = LocalDateTime.now();
        List<DiagnosisTagMetricRow> metricRows = buildMetricRows(calcs, totals, context, snapshotTime);
        List<DiagnosisTagJsonRow> jsonRows = buildJsonRows(calcs, context, snapshotTime);

        if (!metricRows.isEmpty()) {
            batchInsertMetrics(metricRows);
        }
        if (!jsonRows.isEmpty()) {
            batchInsertJson(jsonRows);
        }

        return DiagnosisTagFinalizeResult.builder()
            .metricRows(metricRows.size())
            .jsonRows(jsonRows.size())
            .build();
    }

    private void mergeKeys(Map<String, TagMetricCalc> calcMap,
                           Map<String, DiagnosisSourceTagAggRow> currentMap,
                           Map<String, DiagnosisSourceTagAggRow> compareMap,
                           Map<String, DiagnosisSourceTagStockRow> currentStockMap,
                           Map<String, DiagnosisSourceTagStockRow> compareStockMap,
                           Map<String, DiagnosisSourceTagMetaRow> metaMap,
                           DiagnosisFinalizeContext context) {
        LinkedHashMap<String, String> keys = new LinkedHashMap<>();
        addKeys(keys, currentMap.keySet());
        addKeys(keys, compareMap.keySet());
        addKeys(keys, currentStockMap.keySet());
        addKeys(keys, compareStockMap.keySet());
        addKeys(keys, metaMap.keySet());

        for (String metricKey : keys.keySet()) {
            DiagnosisSourceTagAggRow current = currentMap.get(metricKey);
            DiagnosisSourceTagAggRow compare = compareMap.get(metricKey);
            DiagnosisSourceTagStockRow currentStock = currentStockMap.get(metricKey);
            DiagnosisSourceTagStockRow compareStock = compareStockMap.get(metricKey);
            DiagnosisSourceTagMetaRow meta = metaMap.get(metricKey);

            TagMetricCalc calc = new TagMetricCalc();
            calc.metricKey = metricKey;
            calc.tagType = firstText(current == null ? null : current.getTagType(), compare == null ? null : compare.getTagType(), meta == null ? null : meta.getTagType());
            calc.tagTypeName = firstText(current == null ? null : current.getTagTypeName(), compare == null ? null : compare.getTagTypeName(), meta == null ? null : meta.getTagTypeName());
            calc.tagNo = firstText(current == null ? null : current.getTagNo(), compare == null ? null : compare.getTagNo(), meta == null ? null : meta.getTagNo());
            calc.tagName = firstText(current == null ? null : current.getTagName(), compare == null ? null : compare.getTagName(), meta == null ? null : meta.getTagName());
            calc.isUntagged = isTrue(current == null ? null : current.getIsUntagged())
                || isTrue(compare == null ? null : compare.getIsUntagged())
                || isTrue(meta == null ? null : meta.getIsUntagged());
            calc.currentSku = current == null || current.getSkuCount() == null ? 0 : current.getSkuCount();
            calc.compareSku = compare == null || compare.getSkuCount() == null ? 0 : compare.getSkuCount();
            calc.skuChange = calc.currentSku - calc.compareSku;
            calc.skuInc = growth(BigDecimal.valueOf(calc.currentSku), BigDecimal.valueOf(calc.compareSku));
            calc.saleQuantity = nvl(current == null ? null : current.getSaleQuantity());
            calc.compareSaleQuantity = nvl(compare == null ? null : compare.getSaleQuantity());
            calc.saleQuantityChange = calc.saleQuantity.subtract(calc.compareSaleQuantity);
            calc.saleQuantityInc = growth(calc.saleQuantity, calc.compareSaleQuantity);
            calc.sales = nvl(current == null ? null : current.getSales());
            calc.compareSales = nvl(compare == null ? null : compare.getSales());
            calc.salesChange = calc.sales.subtract(calc.compareSales);
            calc.salesInc = growth(calc.sales, calc.compareSales);
            calc.gross = nvl(current == null ? null : current.getGross());
            calc.compareGross = nvl(compare == null ? null : compare.getGross());
            calc.grossChange = calc.gross.subtract(calc.compareGross);
            calc.grossInc = growth(calc.gross, calc.compareGross);
            calc.salesCost = nvl(current == null ? null : current.getSaleCost());
            calc.compareSalesCost = nvl(compare == null ? null : compare.getSaleCost());
            calc.stockQuantity = nvl(currentStock == null ? null : currentStock.getAvgStockQuantity());
            calc.compareStockQuantity = nvl(compareStock == null ? null : compareStock.getAvgStockQuantity());
            calc.activeStoreCount = current == null || current.getActiveStoreCount() == null ? 0 : current.getActiveStoreCount();
            calc.activitySku = current == null || current.getActivitySku() == null ? 0 : current.getActivitySku();
            calc.periodDays = Math.max(1, (int) context.getPeriodDays());
            calc.saleQuantityPsd = perStoreDaily(calc.saleQuantity, calc.activeStoreCount, calc.periodDays);
            calc.salesPsd = perStoreDaily(calc.sales, calc.activeStoreCount, calc.periodDays);
            calc.grossPsd = perStoreDaily(calc.gross, calc.activeStoreCount, calc.periodDays);
            calc.grossRate = ratio(calc.gross, calc.sales);
            calc.compareGrossRate = ratio(calc.compareGross, calc.compareSales);
            calc.grossRateInc = diff(calc.grossRate, calc.compareGrossRate);
            calc.turnoverRate = ratio(calc.salesCost, calc.stockQuantity);
            calc.turnoverDays = calc.turnoverRate == null || calc.turnoverRate.compareTo(BigDecimal.ZERO) == 0
                ? null
                : BigDecimal.valueOf(calc.periodDays).divide(calc.turnoverRate, 6, RoundingMode.HALF_UP);
            calc.stockSalesRate = ratio(calc.stockQuantity, calc.saleQuantity);
            calc.gmroi = null;
            calc.salesRate = null;
            calcMap.put(metricKey, calc);
        }
    }

    private Totals computeTotals(List<TagMetricCalc> calcs) {
        Totals totals = new Totals();
        for (TagMetricCalc calc : calcs) {
            totals.totalSku += Math.max(0, calc.currentSku);
            totals.totalSaleQuantity = totals.totalSaleQuantity.add(nvl(calc.saleQuantity));
            totals.totalSales = totals.totalSales.add(nvl(calc.sales));
            totals.totalGross = totals.totalGross.add(nvl(calc.gross));
        }
        return totals;
    }

    private List<DiagnosisTagMetricRow> buildMetricRows(List<TagMetricCalc> calcs,
                                                        Totals totals,
                                                        DiagnosisFinalizeContext context,
                                                        LocalDateTime snapshotTime) {
        List<DiagnosisTagMetricRow> rows = new ArrayList<>(calcs.size());
        for (TagMetricCalc calc : calcs) {
            DiagnosisTagMetricRow row = new DiagnosisTagMetricRow();
            row.setTenantId(TENANT_ID);
            row.setQueryHash(context.getQueryHash());
            row.setDataVersion(context.getDataVersion());
            row.setTagType(calc.tagType);
            row.setTagTypeName(calc.tagTypeName);
            row.setTagNo(calc.tagNo);
            row.setTagName(calc.tagName);
            row.setMetricKey(calc.metricKey);
            row.setIsUntagged(calc.isUntagged);
            row.setSkuCount(calc.currentSku);
            row.setCompareSkuCount(calc.compareSku);
            row.setSkuChange(calc.skuChange);
            row.setSkuInc(scale6OrZero(calc.skuInc));
            calc.skuPer = ratio(BigDecimal.valueOf(Math.max(0, calc.currentSku)), BigDecimal.valueOf(Math.max(1, totals.totalSku)));
            calc.salesPer = ratio(calc.sales, totals.totalSales);
            row.setSkuPer(scale6OrZero(calc.skuPer));
            row.setSaleQuantity(scale4(calc.saleQuantity));
            row.setCompareSaleQuantity(scale4(calc.compareSaleQuantity));
            row.setSaleQuantityChange(scale4(calc.saleQuantityChange));
            row.setSaleQuantityInc(scale6OrZero(calc.saleQuantityInc));
            row.setSaleQuantityPer(scale6OrZero(ratio(calc.saleQuantity, totals.totalSaleQuantity)));
            row.setSaleQuantityPsd(scale6OrZero(calc.saleQuantityPsd));
            row.setSales(scale4(calc.sales));
            row.setCompareSales(scale4(calc.compareSales));
            row.setSalesChange(scale4(calc.salesChange));
            row.setSalesInc(scale6OrZero(calc.salesInc));
            row.setSalesPer(scale6OrZero(calc.salesPer));
            row.setSalesPsd(scale6OrZero(calc.salesPsd));
            row.setGross(scale4(calc.gross));
            row.setCompareGross(scale4(calc.compareGross));
            row.setGrossChange(scale4(calc.grossChange));
            row.setGrossInc(scale6OrZero(calc.grossInc));
            row.setGrossPer(scale6OrZero(ratio(calc.gross, totals.totalGross)));
            row.setGrossPsd(scale6OrZero(calc.grossPsd));
            row.setGrossRate(scale6OrZero(calc.grossRate));
            row.setCompareGrossRate(scale6OrZero(calc.compareGrossRate));
            row.setGrossRateInc(scale6OrZero(calc.grossRateInc));
            row.setStockQuantity(scale6OrZero(calc.stockQuantity));
            row.setCompareStockQuantity(scale6OrZero(calc.compareStockQuantity));
            row.setSalesCost(scale4(calc.salesCost));
            row.setCompareSalesCost(scale4(calc.compareSalesCost));
            row.setTurnoverRate(scale6OrZero(calc.turnoverRate));
            row.setTurnoverDays(scale6OrZero(calc.turnoverDays));
            row.setStockSalesRate(scale6OrZero(calc.stockSalesRate));
            row.setContributionRate(scale6OrZero(ratio(calc.gross, totals.totalGross)));
            row.setGmroi(BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP));
            row.setSalesRate(BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP));
            row.setActivitySku(calc.activitySku);
            row.setActiveStoreCount(calc.activeStoreCount);
            row.setPeriodDays(calc.periodDays);
            row.setSnapshotTime(snapshotTime);
            rows.add(row);
        }
        return rows;
    }

    private List<DiagnosisTagJsonRow> buildJsonRows(List<TagMetricCalc> calcs,
                                                    DiagnosisFinalizeContext context,
                                                    LocalDateTime snapshotTime) {
        List<DiagnosisTagJsonRow> rows = new ArrayList<>();
        rows.add(jsonRow(context, snapshotTime, "tagTypeTree", "标签类型树", JsonUtils.toJsonString(buildTagTypeTreePayload(calcs))));
        for (Map.Entry<String, List<TagMetricCalc>> entry : groupByTagType(calcs).entrySet()) {
            String tagType = entry.getKey();
            rows.add(jsonRow(
                context,
                snapshotTime,
                "tagSalesPer:" + tagType,
                "标签销售占比:" + tagType,
                JsonUtils.toJsonString(buildTagSalesPerPayload(entry.getValue()))
            ));
        }
        return rows;
    }

    private DiagnosisTagJsonRow jsonRow(DiagnosisFinalizeContext context,
                                        LocalDateTime snapshotTime,
                                        String code,
                                        String name,
                                        String payloadJson) {
        DiagnosisTagJsonRow row = new DiagnosisTagJsonRow();
        row.setTenantId(TENANT_ID);
        row.setQueryHash(context.getQueryHash());
        row.setDataVersion(context.getDataVersion());
        row.setPayloadCode(code);
        row.setPayloadName(name);
        row.setPayloadJson(payloadJson);
        row.setSnapshotTime(snapshotTime);
        return row;
    }

    private void batchInsertMetrics(List<DiagnosisTagMetricRow> rows) {
        for (int start = 0; start < rows.size(); start += INSERT_BATCH_SIZE) {
            int end = Math.min(start + INSERT_BATCH_SIZE, rows.size());
            tagMapper.batchInsertMetrics(rows.subList(start, end));
        }
    }

    private void batchInsertJson(List<DiagnosisTagJsonRow> rows) {
        for (int start = 0; start < rows.size(); start += INSERT_BATCH_SIZE) {
            int end = Math.min(start + INSERT_BATCH_SIZE, rows.size());
            tagMapper.batchInsertJson(rows.subList(start, end));
        }
    }

    private List<Map<String, Object>> buildTagTypeTreePayload(List<TagMetricCalc> calcs) {
        Map<String, Map<String, Object>> typeMap = new LinkedHashMap<>();
        for (TagMetricCalc calc : calcs) {
            if (calc.isUntagged) {
                continue;
            }
            Map<String, Object> group = typeMap.computeIfAbsent(calc.tagType, key -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("tagType", calc.tagType);
                item.put("tagTypeName", calc.tagTypeName);
                item.put("tagList", new ArrayList<Map<String, Object>>());
                return item;
            });
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tagList = (List<Map<String, Object>>) group.get("tagList");
            Map<String, Object> tag = new LinkedHashMap<>();
            tag.put("tagNo", calc.tagNo);
            tag.put("tagName", calc.tagName);
            tag.put("sales", null);
            tagList.add(tag);
        }
        List<Map<String, Object>> result = new ArrayList<>(typeMap.values());
        for (Map<String, Object> group : result) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tagList = (List<Map<String, Object>>) group.get("tagList");
            tagList.sort(Comparator.comparing(item -> String.valueOf(item.get("tagName")), Comparator.nullsFirst(String::compareTo)));
        }
        result.sort(Comparator.comparing(item -> String.valueOf(item.get("tagType")), Comparator.nullsFirst(String::compareTo)));
        return result;
    }

    private Map<String, Object> buildTagSalesPerPayload(List<TagMetricCalc> calcs) {
        List<TagMetricCalc> sorted = new ArrayList<>(calcs);
        sorted.sort(Comparator.comparing(TagMetricCalc::getSales, Comparator.nullsFirst(BigDecimal::compareTo)).reversed()
            .thenComparing(TagMetricCalc::getTagName, Comparator.nullsFirst(String::compareTo)));

        List<Map<String, Object>> salesAndSkuList = new ArrayList<>();
        for (TagMetricCalc calc : sorted) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("tagName", calc.tagName);
            item.put("sales", scale2(calc.sales));
            item.put("salesPer", scale2(percent(calc.salesPer())));
            item.put("sku", calc.currentSku);
            item.put("skuPer", scale2(percent(calc.skuPer())));
            salesAndSkuList.add(item);
        }

        List<String> goodTagList = takeTagNames(sortBySalesPer(calcs, true));
        List<String> badTagList = takeTagNames(sortBySalesPer(calcs, false));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("salesAndSkuList", salesAndSkuList);
        result.put("goodTagList", goodTagList);
        result.put("badTagList", badTagList);
        return result;
    }

    private List<TagMetricCalc> sortBySalesPer(List<TagMetricCalc> calcs, boolean desc) {
        List<TagMetricCalc> sorted = new ArrayList<>(calcs);
        Comparator<TagMetricCalc> salesPerComparator = Comparator.comparing(
            TagMetricCalc::salesPerSortable,
            Comparator.nullsFirst(BigDecimal::compareTo)
        );
        if (desc) {
            salesPerComparator = salesPerComparator.reversed();
        }
        sorted.sort(salesPerComparator.thenComparing(TagMetricCalc::getTagName, Comparator.nullsFirst(String::compareTo)));
        return sorted;
    }

    private List<String> takeTagNames(List<TagMetricCalc> sorted) {
        List<String> result = new ArrayList<>();
        for (TagMetricCalc calc : sorted) {
            if (!hasText(calc.tagName)) {
                continue;
            }
            result.add(calc.tagName);
            if (result.size() >= SUMMARY_LIMIT) {
                break;
            }
        }
        return result;
    }

    private Map<String, List<TagMetricCalc>> groupByTagType(List<TagMetricCalc> calcs) {
        Map<String, List<TagMetricCalc>> grouped = new LinkedHashMap<>();
        for (TagMetricCalc calc : calcs) {
            if (calc.isUntagged || !hasText(calc.tagType)) {
                continue;
            }
            grouped.computeIfAbsent(calc.tagType, key -> new ArrayList<>()).add(calc);
        }
        return grouped;
    }

    private Map<String, DiagnosisSourceTagAggRow> toAggMap(List<DiagnosisSourceTagAggRow> rows) {
        Map<String, DiagnosisSourceTagAggRow> map = new LinkedHashMap<>();
        for (DiagnosisSourceTagAggRow row : rows) {
            if (row == null || !hasText(row.getMetricKey())) {
                continue;
            }
            map.put(row.getMetricKey(), row);
        }
        return map;
    }

    private Map<String, DiagnosisSourceTagStockRow> toStockMap(List<DiagnosisSourceTagStockRow> rows) {
        Map<String, DiagnosisSourceTagStockRow> map = new LinkedHashMap<>();
        for (DiagnosisSourceTagStockRow row : rows) {
            if (row == null || !hasText(row.getMetricKey())) {
                continue;
            }
            map.put(row.getMetricKey(), row);
        }
        return map;
    }

    private Map<String, DiagnosisSourceTagMetaRow> toMetaMap(List<DiagnosisSourceTagMetaRow> rows) {
        Map<String, DiagnosisSourceTagMetaRow> map = new LinkedHashMap<>();
        for (DiagnosisSourceTagMetaRow row : rows) {
            if (row == null || !hasText(row.getMetricKey())) {
                continue;
            }
            map.put(row.getMetricKey(), row);
        }
        return map;
    }

    private void addKeys(Map<String, String> target, Iterable<String> source) {
        for (String key : source) {
            if (hasText(key)) {
                target.putIfAbsent(key, key);
            }
        }
    }

    private boolean isTrue(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private BigDecimal perStoreDaily(BigDecimal value, Integer activeStoreCount, int periodDays) {
        if (activeStoreCount == null || activeStoreCount <= 0 || periodDays <= 0) {
            return BigDecimal.ZERO;
        }
        return nvl(value).divide(BigDecimal.valueOf((long) activeStoreCount * periodDays), 6, RoundingMode.HALF_UP);
    }

    private BigDecimal growth(BigDecimal current, BigDecimal compare) {
        if (compare == null || compare.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return nvl(current).subtract(compare).divide(compare, 6, RoundingMode.HALF_UP);
    }

    private BigDecimal ratio(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return nvl(numerator).divide(denominator, 6, RoundingMode.HALF_UP);
    }

    private BigDecimal diff(BigDecimal current, BigDecimal compare) {
        if (current == null || compare == null) {
            return null;
        }
        return current.subtract(compare);
    }

    private BigDecimal percent(BigDecimal ratio) {
        return ratio == null ? null : ratio.multiply(HUNDRED);
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
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

    private BigDecimal scale6OrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP) : value.setScale(6, RoundingMode.HALF_UP);
    }

    private <T> List<T> safeList(List<T> rows) {
        return rows == null ? List.of() : rows;
    }

    private static class Totals {
        private int totalSku;
        private BigDecimal totalSaleQuantity = BigDecimal.ZERO;
        private BigDecimal totalSales = BigDecimal.ZERO;
        private BigDecimal totalGross = BigDecimal.ZERO;
    }

    private static class TagMetricCalc {
        private String tagType;
        private String tagTypeName;
        private String tagNo;
        private String tagName;
        private String metricKey;
        private boolean isUntagged;
        private int currentSku;
        private int compareSku;
        private int skuChange;
        private BigDecimal skuInc;
        private BigDecimal skuPer;
        private BigDecimal saleQuantity = BigDecimal.ZERO;
        private BigDecimal compareSaleQuantity = BigDecimal.ZERO;
        private BigDecimal saleQuantityChange = BigDecimal.ZERO;
        private BigDecimal saleQuantityInc;
        private BigDecimal sales = BigDecimal.ZERO;
        private BigDecimal compareSales = BigDecimal.ZERO;
        private BigDecimal salesChange = BigDecimal.ZERO;
        private BigDecimal salesInc;
        private BigDecimal salesPer;
        private BigDecimal gross = BigDecimal.ZERO;
        private BigDecimal compareGross = BigDecimal.ZERO;
        private BigDecimal grossChange = BigDecimal.ZERO;
        private BigDecimal grossInc;
        private BigDecimal salesCost = BigDecimal.ZERO;
        private BigDecimal compareSalesCost = BigDecimal.ZERO;
        private BigDecimal grossRate;
        private BigDecimal compareGrossRate;
        private BigDecimal grossRateInc;
        private BigDecimal stockQuantity = BigDecimal.ZERO;
        private BigDecimal compareStockQuantity = BigDecimal.ZERO;
        private BigDecimal saleQuantityPsd = BigDecimal.ZERO;
        private BigDecimal salesPsd = BigDecimal.ZERO;
        private BigDecimal grossPsd = BigDecimal.ZERO;
        private BigDecimal turnoverRate;
        private BigDecimal turnoverDays;
        private BigDecimal stockSalesRate;
        private BigDecimal gmroi;
        private BigDecimal salesRate;
        private int activitySku;
        private int activeStoreCount;
        private int periodDays;

        private BigDecimal getSales() {
            return sales;
        }

        private String getMetricKey() {
            return metricKey;
        }

        private String getTagName() {
            return tagName;
        }

        private BigDecimal skuPer() {
            return skuPer;
        }

        private BigDecimal salesPer() {
            return salesPer;
        }

        private BigDecimal salesPerSortable() {
            return salesPer();
        }
    }
}

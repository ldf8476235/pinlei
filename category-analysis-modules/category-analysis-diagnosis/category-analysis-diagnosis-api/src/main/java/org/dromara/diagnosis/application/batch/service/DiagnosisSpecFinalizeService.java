package org.dromara.diagnosis.application.batch.service;

import lombok.RequiredArgsConstructor;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.diagnosis.application.batch.model.DiagnosisFinalizeContext;
import org.dromara.diagnosis.application.batch.model.DiagnosisSpecFinalizeResult;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisBatchSourceMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSpecMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceSpecAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceSpecMetaRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceSpecStockRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSpecJsonRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSpecMetricRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSpecOverviewRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiagnosisSpecFinalizeService {

    private static final String TENANT_ID = "000000";
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final int METRIC_INSERT_BATCH_SIZE = 30;
    private static final String TYPE_OTHER = "1";
    private static final String TYPE_NONE = "/";
    private static final String TYPE_OTHER_NAME = "其他规格";
    private static final String TYPE_NONE_NAME = "无规格";
    private static final String NEW_SPEC_YES = "1";
    private static final String NEW_SPEC_NO = "0";

    private final DiagnosisBatchSourceMapper batchSourceMapper;
    private final DiagnosisSpecMapper specMapper;

    @Transactional(rollbackFor = Exception.class)
    public DiagnosisSpecFinalizeResult finalizeSpec(DiagnosisFinalizeContext context) {
        List<DiagnosisSourceSpecAggRow> currentAggRows = safeList(batchSourceMapper.aggregateSpecMetrics(context.getParam()));
        List<DiagnosisSourceSpecAggRow> compareAggRows = context.getCompareParam() == null
            ? List.of()
            : safeList(batchSourceMapper.aggregateSpecMetrics(context.getCompareParam()));
        List<DiagnosisSourceSpecStockRow> currentStockRows = safeList(batchSourceMapper.aggregateSpecStock(context.getParam()));
        List<DiagnosisSourceSpecStockRow> compareStockRows = context.getCompareParam() == null
            ? List.of()
            : safeList(batchSourceMapper.aggregateSpecStock(context.getCompareParam()));
        List<DiagnosisSourceSpecMetaRow> metaRows = safeList(batchSourceMapper.selectSpecMeta(context.getParam()));

        specMapper.deleteByVersion(TENANT_ID, context.getQueryHash(), context.getDataVersion());

        Map<String, DiagnosisSourceSpecAggRow> currentMap = toAggMap(currentAggRows);
        Map<String, DiagnosisSourceSpecAggRow> compareMap = toAggMap(compareAggRows);
        Map<String, DiagnosisSourceSpecStockRow> currentStockMap = toStockMap(currentStockRows);
        Map<String, DiagnosisSourceSpecStockRow> compareStockMap = toStockMap(compareStockRows);
        Map<String, DiagnosisSourceSpecMetaRow> metaMap = toMetaMap(metaRows);

        LinkedHashMap<String, SpecMetricCalc> calcMap = new LinkedHashMap<>();
        mergeSpecKeys(calcMap, currentMap, compareMap, currentStockMap, compareStockMap, metaMap, context);
        if (calcMap.isEmpty()) {
            insertEmptyOverviewAndJson(context);
            return DiagnosisSpecFinalizeResult.builder()
                .overviewRows(1L)
                .metricRows(0L)
                .jsonRows(3L)
                .build();
        }

        List<SpecMetricCalc> calcs = mergeDuplicateSpecCalcs(calcMap.values(), context);
        calcs.sort(Comparator.comparing(SpecMetricCalc::getSales, Comparator.nullsFirst(BigDecimal::compareTo)).reversed()
            .thenComparing(SpecMetricCalc::getSpecNo, Comparator.nullsFirst(String::compareTo)));

        Totals totals = computeTotals(calcs);
        LocalDateTime snapshotTime = LocalDateTime.now();
        List<DiagnosisSpecMetricRow> metricRows = buildMetricRows(calcs, totals, context, snapshotTime);
        DiagnosisSpecOverviewRow overviewRow = buildOverviewRow(calcs, context, snapshotTime);
        List<DiagnosisSpecJsonRow> jsonRows = buildJsonRows(calcs, context, snapshotTime);

        specMapper.insertOverview(overviewRow);
        if (!metricRows.isEmpty()) {
            batchInsertMetrics(metricRows);
        }
        if (!jsonRows.isEmpty()) {
            specMapper.batchInsertJson(jsonRows);
        }

        return DiagnosisSpecFinalizeResult.builder()
            .overviewRows(1L)
            .metricRows(metricRows.size())
            .jsonRows(jsonRows.size())
            .build();
    }

    private List<SpecMetricCalc> mergeDuplicateSpecCalcs(Iterable<SpecMetricCalc> source, DiagnosisFinalizeContext context) {
        LinkedHashMap<String, SpecMetricCalc> merged = new LinkedHashMap<>();
        for (SpecMetricCalc calc : source) {
            if (calc == null) {
                continue;
            }
            String key = normalizeSpecUniqueKey(calc.specName);
            SpecMetricCalc existing = merged.get(key);
            if (existing == null) {
                merged.put(key, calc);
                continue;
            }
            mergeSpecCalc(existing, calc, context);
        }
        return new ArrayList<>(merged.values());
    }

    private void mergeSpecCalc(SpecMetricCalc target, SpecMetricCalc source, DiagnosisFinalizeContext context) {
        target.currentSku += Math.max(0, source.currentSku);
        target.compareSku += Math.max(0, source.compareSku);
        target.saleQuantity = nvl(target.saleQuantity).add(nvl(source.saleQuantity));
        target.compareSaleQuantity = nvl(target.compareSaleQuantity).add(nvl(source.compareSaleQuantity));
        target.sales = nvl(target.sales).add(nvl(source.sales));
        target.compareSales = nvl(target.compareSales).add(nvl(source.compareSales));
        target.gross = nvl(target.gross).add(nvl(source.gross));
        target.compareGross = nvl(target.compareGross).add(nvl(source.compareGross));
        target.salesCost = nvl(target.salesCost).add(nvl(source.salesCost));
        target.compareSalesCost = nvl(target.compareSalesCost).add(nvl(source.compareSalesCost));
        target.stockQuantity = nvl(target.stockQuantity).add(nvl(source.stockQuantity));
        target.compareStockQuantity = nvl(target.compareStockQuantity).add(nvl(source.compareStockQuantity));
        target.activitySku += Math.max(0, source.activitySku);
        target.activeStoreCount += Math.max(0, source.activeStoreCount);
        if (NEW_SPEC_YES.equals(source.newSpecType)) {
            target.newSpecType = NEW_SPEC_YES;
            target.newSpecTypeName = "是";
        }
        recalculateDerivedFields(target, context);
    }

    private void recalculateDerivedFields(SpecMetricCalc calc, DiagnosisFinalizeContext context) {
        calc.skuChange = calc.currentSku - calc.compareSku;
        calc.skuInc = growth(BigDecimal.valueOf(calc.currentSku), BigDecimal.valueOf(calc.compareSku));
        calc.saleQuantityChange = nvl(calc.saleQuantity).subtract(nvl(calc.compareSaleQuantity));
        calc.saleQuantityInc = growth(calc.saleQuantity, calc.compareSaleQuantity);
        calc.salesChange = nvl(calc.sales).subtract(nvl(calc.compareSales));
        calc.salesInc = growth(calc.sales, calc.compareSales);
        calc.grossChange = nvl(calc.gross).subtract(nvl(calc.compareGross));
        calc.grossInc = growth(calc.gross, calc.compareGross);
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
        calc.gmroi = ratio(calc.gross, calc.stockQuantity);
    }

    private String normalizeSpecUniqueKey(String specName) {
        String value = hasText(specName) ? specName.trim() : TYPE_NONE_NAME;
        return value.replaceAll("\\s+", "").toUpperCase();
    }

    private void batchInsertMetrics(List<DiagnosisSpecMetricRow> rows) {
        for (int start = 0; start < rows.size(); start += METRIC_INSERT_BATCH_SIZE) {
            int end = Math.min(start + METRIC_INSERT_BATCH_SIZE, rows.size());
            specMapper.batchInsertMetrics(rows.subList(start, end));
        }
    }

    private void insertEmptyOverviewAndJson(DiagnosisFinalizeContext context) {
        LocalDateTime snapshotTime = LocalDateTime.now();
        DiagnosisSpecOverviewRow overviewRow = new DiagnosisSpecOverviewRow();
        overviewRow.setTenantId(TENANT_ID);
        overviewRow.setQueryHash(context.getQueryHash());
        overviewRow.setDataVersion(context.getDataVersion());
        overviewRow.setTotalNum(0);
        overviewRow.setNewNum(0);
        overviewRow.setSnapshotTime(snapshotTime);
        specMapper.insertOverview(overviewRow);
        specMapper.batchInsertJson(buildJsonRows(List.of(), context, snapshotTime));
    }

    private void mergeSpecKeys(Map<String, SpecMetricCalc> calcMap,
                               Map<String, DiagnosisSourceSpecAggRow> currentMap,
                               Map<String, DiagnosisSourceSpecAggRow> compareMap,
                               Map<String, DiagnosisSourceSpecStockRow> currentStockMap,
                               Map<String, DiagnosisSourceSpecStockRow> compareStockMap,
                               Map<String, DiagnosisSourceSpecMetaRow> metaMap,
                               DiagnosisFinalizeContext context) {
        LinkedHashMap<String, String> keys = new LinkedHashMap<>();
        addKeys(keys, currentMap.keySet());
        addKeys(keys, compareMap.keySet());
        addKeys(keys, currentStockMap.keySet());
        addKeys(keys, compareStockMap.keySet());
        addKeys(keys, metaMap.keySet());

        for (String specNo : keys.keySet()) {
            DiagnosisSourceSpecAggRow current = currentMap.get(specNo);
            DiagnosisSourceSpecAggRow compare = compareMap.get(specNo);
            DiagnosisSourceSpecStockRow currentStock = currentStockMap.get(specNo);
            DiagnosisSourceSpecStockRow compareStock = compareStockMap.get(specNo);
            DiagnosisSourceSpecMetaRow meta = metaMap.get(specNo);

            SpecMetricCalc calc = new SpecMetricCalc();
            calc.specNo = specNo;
            calc.specName = resolveSpecName(specNo, current, compare, meta);
            calc.specType = resolveSpecType(specNo, current, compare, meta);
            calc.specTypeName = resolveSpecTypeName(calc.specType);
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
            calc.gmroi = ratio(calc.gross, calc.stockQuantity);
            calc.salesRate = null;
            calc.newSpecType = isNewSpec(meta == null ? null : meta.getFirstSaleDate(), context.getPeriodStart(), context.getPeriodEnd())
                ? NEW_SPEC_YES
                : NEW_SPEC_NO;
            calc.newSpecTypeName = NEW_SPEC_YES.equals(calc.newSpecType) ? "是" : "否";
            calcMap.put(specNo, calc);
        }
    }

    private Totals computeTotals(List<SpecMetricCalc> calcs) {
        Totals totals = new Totals();
        for (SpecMetricCalc calc : calcs) {
            totals.totalSku += Math.max(0, calc.currentSku);
            totals.totalSaleQuantity = totals.totalSaleQuantity.add(nvl(calc.saleQuantity));
            totals.totalSales = totals.totalSales.add(nvl(calc.sales));
            totals.totalGross = totals.totalGross.add(nvl(calc.gross));
        }
        return totals;
    }

    private List<DiagnosisSpecMetricRow> buildMetricRows(List<SpecMetricCalc> calcs,
                                                         Totals totals,
                                                         DiagnosisFinalizeContext context,
                                                         LocalDateTime snapshotTime) {
        List<DiagnosisSpecMetricRow> rows = new ArrayList<>(calcs.size());
        for (SpecMetricCalc calc : calcs) {
            DiagnosisSpecMetricRow row = new DiagnosisSpecMetricRow();
            row.setTenantId(TENANT_ID);
            row.setQueryHash(context.getQueryHash());
            row.setDataVersion(context.getDataVersion());
            row.setSpecNo(calc.specNo);
            row.setSpecName(calc.specName);
            row.setSpecType(calc.specType);
            row.setSpecTypeName(calc.specTypeName);
            row.setNewSpecType(calc.newSpecType);
            row.setNewSpecTypeName(calc.newSpecTypeName);
            row.setSkuCount(calc.currentSku);
            row.setCompareSkuCount(calc.compareSku);
            row.setSkuChange(calc.skuChange);
            row.setSkuInc(scale6(calc.skuInc));
            row.setSkuPer(scale6(nonNullRatio(BigDecimal.valueOf(Math.max(0, calc.currentSku)), BigDecimal.valueOf(Math.max(1, totals.totalSku)))));
            row.setSaleQuantity(scale4(calc.saleQuantity));
            row.setCompareSaleQuantity(scale4(calc.compareSaleQuantity));
            row.setSaleQuantityChange(scale4(calc.saleQuantityChange));
            row.setSaleQuantityInc(scale6(calc.saleQuantityInc));
            row.setSaleQuantityPer(scale6(nonNullRatio(calc.saleQuantity, totals.totalSaleQuantity)));
            row.setSaleQuantityPsd(scale6(calc.saleQuantityPsd));
            row.setSales(scale4(calc.sales));
            row.setCompareSales(scale4(calc.compareSales));
            row.setSalesChange(scale4(calc.salesChange));
            row.setSalesInc(scale6(calc.salesInc));
            row.setSalesPer(scale6(nonNullRatio(calc.sales, totals.totalSales)));
            row.setSalesPsd(scale6(calc.salesPsd));
            row.setGross(scale4(calc.gross));
            row.setCompareGross(scale4(calc.compareGross));
            row.setGrossChange(scale4(calc.grossChange));
            row.setGrossInc(scale6(calc.grossInc));
            row.setGrossPer(scale6(nonNullRatio(calc.gross, totals.totalGross)));
            row.setGrossPsd(scale6(calc.grossPsd));
            row.setGrossRate(scale6(calc.grossRate));
            row.setCompareGrossRate(scale6(calc.compareGrossRate));
            row.setGrossRateInc(scale6(calc.grossRateInc));
            row.setStockQuantity(scale6(calc.stockQuantity));
            row.setCompareStockQuantity(scale6(calc.compareStockQuantity));
            row.setSalesCost(scale4(calc.salesCost));
            row.setCompareSalesCost(scale4(calc.compareSalesCost));
            row.setTurnoverRate(scale6(calc.turnoverRate));
            row.setTurnoverDays(scale6(calc.turnoverDays));
            row.setStockSalesRate(scale6(calc.stockSalesRate));
            row.setContributionRate(scale6(ratio(calc.sales, totals.totalSales)));
            row.setGmroi(scale6(calc.gmroi));
            row.setSalesRate(scale6(calc.salesRate));
            row.setActivitySku(calc.activitySku);
            row.setActiveStoreCount(calc.activeStoreCount);
            row.setPeriodDays(calc.periodDays);
            row.setSnapshotTime(snapshotTime);
            rows.add(row);
        }
        return rows;
    }

    private DiagnosisSpecOverviewRow buildOverviewRow(List<SpecMetricCalc> calcs,
                                                      DiagnosisFinalizeContext context,
                                                      LocalDateTime snapshotTime) {
        int totalNum = calcs.size();
        int newNum = 0;
        for (SpecMetricCalc calc : calcs) {
            if (NEW_SPEC_YES.equals(calc.newSpecType)) {
                newNum++;
            }
        }
        DiagnosisSpecOverviewRow row = new DiagnosisSpecOverviewRow();
        row.setTenantId(TENANT_ID);
        row.setQueryHash(context.getQueryHash());
        row.setDataVersion(context.getDataVersion());
        row.setTotalNum(totalNum);
        row.setNewNum(newNum);
        row.setSnapshotTime(snapshotTime);
        return row;
    }

    private List<DiagnosisSpecJsonRow> buildJsonRows(List<SpecMetricCalc> calcs,
                                                     DiagnosisFinalizeContext context,
                                                     LocalDateTime snapshotTime) {
        List<DiagnosisSpecJsonRow> rows = new ArrayList<>();
        rows.add(jsonRow(context, snapshotTime, "specType", "规格筛选", JsonUtils.toJsonString(buildSpecTypePayload(calcs))));
        rows.add(jsonRow(context, snapshotTime, "salesShare", "规格销售额占比", JsonUtils.toJsonString(buildSalesSharePayload(calcs))));
        rows.add(jsonRow(context, snapshotTime, "skuSalesChange", "规格SKU与销售变化", JsonUtils.toJsonString(buildSkuSalesChangePayload(calcs))));
        return rows;
    }

    private DiagnosisSpecJsonRow jsonRow(DiagnosisFinalizeContext context,
                                         LocalDateTime snapshotTime,
                                         String code,
                                         String name,
                                         String payloadJson) {
        DiagnosisSpecJsonRow row = new DiagnosisSpecJsonRow();
        row.setTenantId(TENANT_ID);
        row.setQueryHash(context.getQueryHash());
        row.setDataVersion(context.getDataVersion());
        row.setPayloadCode(code);
        row.setPayloadName(name);
        row.setPayloadJson(payloadJson);
        row.setSnapshotTime(snapshotTime);
        return row;
    }

    private List<Map<String, Object>> buildSpecTypePayload(List<SpecMetricCalc> calcs) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (SpecMetricCalc calc : calcs) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", calc.specType);
            item.put("specNo", calc.specNo);
            item.put("specName", calc.specName);
            list.add(item);
        }
        return list;
    }

    private List<Map<String, Object>> buildSalesSharePayload(List<SpecMetricCalc> calcs) {
        BigDecimal totalSales = sumSales(calcs);
        List<Map<String, Object>> list = new ArrayList<>();
        for (SpecMetricCalc calc : calcs) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("productSpec", calc.specName);
            item.put("sales", scale2(calc.sales));
            item.put("salesPer", scale4(ratio(calc.sales, totalSales)));
            list.add(item);
        }
        return list;
    }

    private List<Map<String, Object>> buildSkuSalesChangePayload(List<SpecMetricCalc> calcs) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (SpecMetricCalc calc : calcs) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("productSpec", calc.specName);
            item.put("productNo", null);
            item.put("sku", calc.skuChange);
            item.put("sales", scale2(percent(calc.salesInc)));
            item.put("currentSales", scale2(calc.sales));
            item.put("compareSales", scale2(calc.compareSales));
            list.add(item);
        }
        return list;
    }

    private BigDecimal sumSales(List<SpecMetricCalc> calcs) {
        BigDecimal total = BigDecimal.ZERO;
        for (SpecMetricCalc calc : calcs) {
            total = total.add(nvl(calc.sales));
        }
        return total;
    }

    private Map<String, DiagnosisSourceSpecAggRow> toAggMap(List<DiagnosisSourceSpecAggRow> rows) {
        Map<String, DiagnosisSourceSpecAggRow> map = new LinkedHashMap<>();
        for (DiagnosisSourceSpecAggRow row : rows) {
            if (row == null || !hasText(row.getSpecNo())) {
                continue;
            }
            map.put(row.getSpecNo(), row);
        }
        return map;
    }

    private Map<String, DiagnosisSourceSpecStockRow> toStockMap(List<DiagnosisSourceSpecStockRow> rows) {
        Map<String, DiagnosisSourceSpecStockRow> map = new LinkedHashMap<>();
        for (DiagnosisSourceSpecStockRow row : rows) {
            if (row == null || !hasText(row.getSpecNo())) {
                continue;
            }
            map.put(row.getSpecNo(), row);
        }
        return map;
    }

    private Map<String, DiagnosisSourceSpecMetaRow> toMetaMap(List<DiagnosisSourceSpecMetaRow> rows) {
        Map<String, DiagnosisSourceSpecMetaRow> map = new LinkedHashMap<>();
        for (DiagnosisSourceSpecMetaRow row : rows) {
            if (row == null || !hasText(row.getSpecNo())) {
                continue;
            }
            map.put(row.getSpecNo(), row);
        }
        return map;
    }

    private void addKeys(Map<String, String> target, Iterable<String> source) {
        for (String key : source) {
            if (!hasText(key)) {
                continue;
            }
            target.putIfAbsent(key, key);
        }
    }

    private String resolveSpecName(String specNo,
                                   DiagnosisSourceSpecAggRow current,
                                   DiagnosisSourceSpecAggRow compare,
                                   DiagnosisSourceSpecMetaRow meta) {
        String value = firstText(
            current == null ? null : current.getSpecName(),
            compare == null ? null : compare.getSpecName(),
            meta == null ? null : meta.getSpecName()
        );
        if (hasText(value)) {
            return value;
        }
        return TYPE_NONE.equals(specNo) ? TYPE_NONE_NAME : specNo;
    }

    private String resolveSpecType(String specNo,
                                   DiagnosisSourceSpecAggRow current,
                                   DiagnosisSourceSpecAggRow compare,
                                   DiagnosisSourceSpecMetaRow meta) {
        String value = firstText(
            current == null ? null : current.getSpecType(),
            compare == null ? null : compare.getSpecType(),
            meta == null ? null : meta.getSpecType()
        );
        if (hasText(value)) {
            return value;
        }
        return TYPE_NONE.equals(specNo) ? TYPE_NONE : TYPE_OTHER;
    }

    private String resolveSpecTypeName(String specType) {
        if (TYPE_NONE.equals(specType)) {
            return TYPE_NONE_NAME;
        }
        return TYPE_OTHER_NAME;
    }

    private boolean isNewSpec(LocalDate firstSaleDate, LocalDate periodStart, LocalDate periodEnd) {
        return firstSaleDate != null
            && periodStart != null
            && periodEnd != null
            && !firstSaleDate.isBefore(periodStart)
            && !firstSaleDate.isAfter(periodEnd);
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
        return value != null && !value.trim().isEmpty();
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

    private BigDecimal nonNullRatio(BigDecimal numerator, BigDecimal denominator) {
        BigDecimal value = ratio(numerator, denominator);
        return value == null ? BigDecimal.ZERO : value;
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

    private <T> List<T> safeList(List<T> rows) {
        return rows == null ? List.of() : rows;
    }

    private static class Totals {
        private int totalSku;
        private BigDecimal totalSaleQuantity = BigDecimal.ZERO;
        private BigDecimal totalSales = BigDecimal.ZERO;
        private BigDecimal totalGross = BigDecimal.ZERO;
    }

    private static class SpecMetricCalc {
        private String specNo;
        private String specName;
        private String specType;
        private String specTypeName;
        private String newSpecType;
        private String newSpecTypeName;
        private int currentSku;
        private int compareSku;
        private int skuChange;
        private BigDecimal skuInc;
        private BigDecimal saleQuantity = BigDecimal.ZERO;
        private BigDecimal compareSaleQuantity = BigDecimal.ZERO;
        private BigDecimal saleQuantityChange = BigDecimal.ZERO;
        private BigDecimal saleQuantityInc;
        private BigDecimal sales = BigDecimal.ZERO;
        private BigDecimal compareSales = BigDecimal.ZERO;
        private BigDecimal salesChange = BigDecimal.ZERO;
        private BigDecimal salesInc;
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

        private String getSpecNo() {
            return specNo;
        }
    }
}

package org.dromara.diagnosis.application.batch.service;

import lombok.RequiredArgsConstructor;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.diagnosis.application.batch.model.DiagnosisBrandFinalizeResult;
import org.dromara.diagnosis.application.batch.model.DiagnosisFinalizeContext;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisBatchSourceMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisBrandMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisBrandJsonRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisBrandMetricRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisBrandOverviewRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceBrandAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceBrandMetaRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceBrandStockRow;
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
public class DiagnosisBrandFinalizeService {

    private static final String TENANT_ID = "000000";
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final int SUMMARY_LIMIT = 10;
    private static final String TYPE_OTHER = "1";
    private static final String TYPE_OWN = "2";
    private static final String TYPE_NONE = "/";
    private static final String TYPE_OTHER_NAME = "其他品牌";
    private static final String TYPE_OWN_NAME = "自由品牌";
    private static final String TYPE_NONE_NAME = "无品牌";
    private static final String NEW_BRAND_YES = "1";
    private static final String NEW_BRAND_NO = "0";

    private final DiagnosisBatchSourceMapper batchSourceMapper;
    private final DiagnosisBrandMapper brandMapper;

    @Transactional(rollbackFor = Exception.class)
    public DiagnosisBrandFinalizeResult finalizeBrand(DiagnosisFinalizeContext context) {
        List<DiagnosisSourceBrandAggRow> currentAggRows = safeList(batchSourceMapper.aggregateBrandMetrics(context.getParam()));
        List<DiagnosisSourceBrandAggRow> compareAggRows = context.getCompareParam() == null
            ? List.of()
            : safeList(batchSourceMapper.aggregateBrandMetrics(context.getCompareParam()));
        List<DiagnosisSourceBrandStockRow> currentStockRows = safeList(batchSourceMapper.aggregateBrandStock(context.getParam()));
        List<DiagnosisSourceBrandStockRow> compareStockRows = context.getCompareParam() == null
            ? List.of()
            : safeList(batchSourceMapper.aggregateBrandStock(context.getCompareParam()));
        List<DiagnosisSourceBrandMetaRow> metaRows = safeList(batchSourceMapper.selectBrandMeta(context.getParam()));

        brandMapper.deleteByVersion(TENANT_ID, context.getQueryHash(), context.getDataVersion());

        Map<String, DiagnosisSourceBrandAggRow> currentMap = toAggMap(currentAggRows);
        Map<String, DiagnosisSourceBrandAggRow> compareMap = toAggMap(compareAggRows);
        Map<String, DiagnosisSourceBrandStockRow> currentStockMap = toStockMap(currentStockRows);
        Map<String, DiagnosisSourceBrandStockRow> compareStockMap = toStockMap(compareStockRows);
        Map<String, DiagnosisSourceBrandMetaRow> metaMap = toMetaMap(metaRows);

        LinkedHashMap<String, BrandMetricCalc> calcMap = new LinkedHashMap<>();
        mergeBrandKeys(calcMap, currentMap, compareMap, currentStockMap, compareStockMap, metaMap, context);
        if (calcMap.isEmpty()) {
            insertEmptyOverviewAndJson(context);
            return DiagnosisBrandFinalizeResult.builder()
                .overviewRows(1L)
                .metricRows(0L)
                .jsonRows(7L)
                .build();
        }

        List<BrandMetricCalc> calcs = new ArrayList<>(calcMap.values());
        calcs.sort(Comparator.comparing(BrandMetricCalc::getSales, Comparator.nullsFirst(BigDecimal::compareTo)).reversed()
            .thenComparing(BrandMetricCalc::getBrandNo, Comparator.nullsFirst(String::compareTo)));

        Totals totals = computeTotals(calcs);
        LocalDateTime snapshotTime = LocalDateTime.now();
        List<DiagnosisBrandMetricRow> metricRows = buildMetricRows(calcs, totals, context, snapshotTime);
        DiagnosisBrandOverviewRow overviewRow = buildOverviewRow(calcs, context, snapshotTime);
        List<DiagnosisBrandJsonRow> jsonRows = buildJsonRows(calcs, context, snapshotTime);

        brandMapper.insertOverview(overviewRow);
        if (!metricRows.isEmpty()) {
            brandMapper.batchInsertMetrics(metricRows);
        }
        if (!jsonRows.isEmpty()) {
            brandMapper.batchInsertJson(jsonRows);
        }

        return DiagnosisBrandFinalizeResult.builder()
            .overviewRows(1L)
            .metricRows(metricRows.size())
            .jsonRows(jsonRows.size())
            .build();
    }

    private void insertEmptyOverviewAndJson(DiagnosisFinalizeContext context) {
        LocalDateTime snapshotTime = LocalDateTime.now();
        DiagnosisBrandOverviewRow overviewRow = new DiagnosisBrandOverviewRow();
        overviewRow.setTenantId(TENANT_ID);
        overviewRow.setQueryHash(context.getQueryHash());
        overviewRow.setDataVersion(context.getDataVersion());
        overviewRow.setTotalNum(0);
        overviewRow.setNewNum(0);
        overviewRow.setOwnNum(0);
        overviewRow.setSnapshotTime(snapshotTime);
        brandMapper.insertOverview(overviewRow);
        brandMapper.batchInsertJson(buildJsonRows(List.of(), context, snapshotTime));
    }

    private void mergeBrandKeys(Map<String, BrandMetricCalc> calcMap,
                                Map<String, DiagnosisSourceBrandAggRow> currentMap,
                                Map<String, DiagnosisSourceBrandAggRow> compareMap,
                                Map<String, DiagnosisSourceBrandStockRow> currentStockMap,
                                Map<String, DiagnosisSourceBrandStockRow> compareStockMap,
                                Map<String, DiagnosisSourceBrandMetaRow> metaMap,
                                DiagnosisFinalizeContext context) {
        LinkedHashMap<String, String> keys = new LinkedHashMap<>();
        addKeys(keys, currentMap.keySet());
        addKeys(keys, compareMap.keySet());
        addKeys(keys, currentStockMap.keySet());
        addKeys(keys, compareStockMap.keySet());
        addKeys(keys, metaMap.keySet());

        for (String brandNo : keys.keySet()) {
            DiagnosisSourceBrandAggRow current = currentMap.get(brandNo);
            DiagnosisSourceBrandAggRow compare = compareMap.get(brandNo);
            DiagnosisSourceBrandStockRow currentStock = currentStockMap.get(brandNo);
            DiagnosisSourceBrandStockRow compareStock = compareStockMap.get(brandNo);
            DiagnosisSourceBrandMetaRow meta = metaMap.get(brandNo);

            BrandMetricCalc calc = new BrandMetricCalc();
            calc.brandNo = brandNo;
            calc.productBrand = resolveBrandName(brandNo, current, compare, meta);
            calc.brandType = resolveBrandType(brandNo, current, compare, meta);
            calc.brandTypeName = resolveBrandTypeName(calc.brandType);
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
            calc.newBrandType = isNewBrand(meta == null ? null : meta.getFirstSaleDate(), context.getPeriodStart(), context.getPeriodEnd())
                ? NEW_BRAND_YES
                : NEW_BRAND_NO;
            calc.newBrandTypeName = NEW_BRAND_YES.equals(calc.newBrandType) ? "是" : "否";
            calc.snapshotTime = LocalDateTime.now();
            calcMap.put(brandNo, calc);
        }
    }

    private Totals computeTotals(List<BrandMetricCalc> calcs) {
        Totals totals = new Totals();
        for (BrandMetricCalc calc : calcs) {
            totals.totalSku += Math.max(0, calc.currentSku);
            totals.totalSaleQuantity = totals.totalSaleQuantity.add(nvl(calc.saleQuantity));
            totals.totalSales = totals.totalSales.add(nvl(calc.sales));
            totals.totalGross = totals.totalGross.add(nvl(calc.gross));
        }
        return totals;
    }

    private List<DiagnosisBrandMetricRow> buildMetricRows(List<BrandMetricCalc> calcs,
                                                          Totals totals,
                                                          DiagnosisFinalizeContext context,
                                                          LocalDateTime snapshotTime) {
        List<DiagnosisBrandMetricRow> rows = new ArrayList<>(calcs.size());
        for (BrandMetricCalc calc : calcs) {
            DiagnosisBrandMetricRow row = new DiagnosisBrandMetricRow();
            row.setTenantId(TENANT_ID);
            row.setQueryHash(context.getQueryHash());
            row.setDataVersion(context.getDataVersion());
            row.setBrandNo(calc.brandNo);
            row.setProductBrand(calc.productBrand);
            row.setBrandType(calc.brandType);
            row.setBrandTypeName(calc.brandTypeName);
            row.setNewBrandType(calc.newBrandType);
            row.setNewBrandTypeName(calc.newBrandTypeName);
            row.setSkuCount(calc.currentSku);
            row.setCompareSkuCount(calc.compareSku);
            row.setSkuChange(calc.skuChange);
            row.setSkuInc(scale6(calc.skuInc));
            row.setSkuPer(scale6(ratio(BigDecimal.valueOf(Math.max(0, calc.currentSku)), BigDecimal.valueOf(Math.max(1, totals.totalSku)))));
            row.setSaleQuantity(scale4(calc.saleQuantity));
            row.setCompareSaleQuantity(scale4(calc.compareSaleQuantity));
            row.setSaleQuantityChange(scale4(calc.saleQuantityChange));
            row.setSaleQuantityInc(scale6(calc.saleQuantityInc));
            row.setSaleQuantityPer(scale6(ratio(calc.saleQuantity, totals.totalSaleQuantity)));
            row.setSaleQuantityPsd(scale6(calc.saleQuantityPsd));
            row.setSales(scale4(calc.sales));
            row.setCompareSales(scale4(calc.compareSales));
            row.setSalesChange(scale4(calc.salesChange));
            row.setSalesInc(scale6(calc.salesInc));
            row.setSalesPer(scale6(ratio(calc.sales, totals.totalSales)));
            row.setSalesPsd(scale6(calc.salesPsd));
            row.setGross(scale4(calc.gross));
            row.setCompareGross(scale4(calc.compareGross));
            row.setGrossChange(scale4(calc.grossChange));
            row.setGrossInc(scale6(calc.grossInc));
            row.setGrossPer(scale6(ratio(calc.gross, totals.totalGross)));
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

    private DiagnosisBrandOverviewRow buildOverviewRow(List<BrandMetricCalc> calcs,
                                                       DiagnosisFinalizeContext context,
                                                       LocalDateTime snapshotTime) {
        int totalNum = calcs.size();
        int newNum = 0;
        int ownNum = 0;
        for (BrandMetricCalc calc : calcs) {
            if (NEW_BRAND_YES.equals(calc.newBrandType)) {
                newNum++;
            }
            if (TYPE_OWN.equals(calc.brandType)) {
                ownNum++;
            }
        }
        DiagnosisBrandOverviewRow row = new DiagnosisBrandOverviewRow();
        row.setTenantId(TENANT_ID);
        row.setQueryHash(context.getQueryHash());
        row.setDataVersion(context.getDataVersion());
        row.setTotalNum(totalNum);
        row.setNewNum(newNum);
        row.setOwnNum(ownNum);
        row.setSnapshotTime(snapshotTime);
        return row;
    }

    private List<DiagnosisBrandJsonRow> buildJsonRows(List<BrandMetricCalc> calcs,
                                                      DiagnosisFinalizeContext context,
                                                      LocalDateTime snapshotTime) {
        List<DiagnosisBrandJsonRow> rows = new ArrayList<>();
        rows.add(jsonRow(context, snapshotTime, "brandType", "品牌筛选", JsonUtils.toJsonString(buildBrandTypePayload(calcs))));
        rows.add(jsonRow(context, snapshotTime, "salesShare", "品牌销售额占比", JsonUtils.toJsonString(buildSalesSharePayload(calcs))));
        rows.add(jsonRow(context, snapshotTime, "skuSalesChange", "品牌SKU与销售变化", JsonUtils.toJsonString(buildSkuSalesChangePayload(calcs))));
        rows.add(jsonRow(context, snapshotTime, "summaryOne", "高销售额品牌组", JsonUtils.toJsonString(topBrandsBySales(calcs, true))));
        rows.add(jsonRow(context, snapshotTime, "summaryTwo", "低销售额品牌组", JsonUtils.toJsonString(topBrandsBySales(calcs, false))));
        rows.add(jsonRow(context, snapshotTime, "summaryThree", "高销售额增长品牌组", JsonUtils.toJsonString(topBrandsBySalesInc(calcs, true))));
        rows.add(jsonRow(context, snapshotTime, "summaryFour", "高销售额下滑品牌组", JsonUtils.toJsonString(topBrandsBySalesInc(calcs, false))));
        return rows;
    }

    private DiagnosisBrandJsonRow jsonRow(DiagnosisFinalizeContext context,
                                          LocalDateTime snapshotTime,
                                          String code,
                                          String name,
                                          String payloadJson) {
        DiagnosisBrandJsonRow row = new DiagnosisBrandJsonRow();
        row.setTenantId(TENANT_ID);
        row.setQueryHash(context.getQueryHash());
        row.setDataVersion(context.getDataVersion());
        row.setPayloadCode(code);
        row.setPayloadName(name);
        row.setPayloadJson(payloadJson);
        row.setSnapshotTime(snapshotTime);
        return row;
    }

    private List<Map<String, Object>> buildBrandTypePayload(List<BrandMetricCalc> calcs) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (BrandMetricCalc calc : calcs) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", calc.brandType);
            item.put("brandNo", calc.brandNo);
            item.put("brandName", calc.productBrand);
            list.add(item);
        }
        return list;
    }

    private List<Map<String, Object>> buildSalesSharePayload(List<BrandMetricCalc> calcs) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (BrandMetricCalc calc : calcs) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("productBrand", calc.productBrand);
            item.put("sales", scale2(calc.sales));
            item.put("salesPer", scale4(ratio(calc.sales, sumSales(calcs))));
            list.add(item);
        }
        return list;
    }

    private List<Map<String, Object>> buildSkuSalesChangePayload(List<BrandMetricCalc> calcs) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (BrandMetricCalc calc : calcs) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("brandNo", calc.brandNo);
            item.put("productBrand", calc.productBrand);
            item.put("ownBrandNo", null);
            item.put("ownBrandName", null);
            item.put("productNo", null);
            item.put("sku", calc.skuChange);
            item.put("sales", scale2(percent(calc.salesInc)));
            item.put("currentSales", scale2(calc.sales));
            item.put("compareSales", scale2(calc.compareSales));
            list.add(item);
        }
        return list;
    }

    private List<String> topBrandsBySales(List<BrandMetricCalc> calcs, boolean desc) {
        List<BrandMetricCalc> sorted = new ArrayList<>(calcs);
        Comparator<BrandMetricCalc> comparator = Comparator.comparing(BrandMetricCalc::getSales, Comparator.nullsFirst(BigDecimal::compareTo))
            .thenComparing(BrandMetricCalc::getBrandNo, Comparator.nullsFirst(String::compareTo));
        sorted.sort(desc ? comparator.reversed() : comparator);
        return takeBrandNames(sorted);
    }

    private List<String> topBrandsBySalesInc(List<BrandMetricCalc> calcs, boolean desc) {
        List<BrandMetricCalc> sorted = new ArrayList<>(calcs);
        Comparator<BrandMetricCalc> comparator = Comparator.comparing(BrandMetricCalc::getSalesIncSortable, Comparator.nullsFirst(BigDecimal::compareTo))
            .thenComparing(BrandMetricCalc::getBrandNo, Comparator.nullsFirst(String::compareTo));
        sorted.sort(desc ? comparator.reversed() : comparator);
        return takeBrandNames(sorted);
    }

    private List<String> takeBrandNames(List<BrandMetricCalc> sorted) {
        List<String> result = new ArrayList<>();
        for (BrandMetricCalc calc : sorted) {
            if (calc.productBrand == null || calc.productBrand.isBlank()) {
                continue;
            }
            result.add(calc.productBrand);
            if (result.size() >= SUMMARY_LIMIT) {
                break;
            }
        }
        return result;
    }

    private BigDecimal sumSales(List<BrandMetricCalc> calcs) {
        BigDecimal total = BigDecimal.ZERO;
        for (BrandMetricCalc calc : calcs) {
            total = total.add(nvl(calc.sales));
        }
        return total;
    }

    private Map<String, DiagnosisSourceBrandAggRow> toAggMap(List<DiagnosisSourceBrandAggRow> rows) {
        Map<String, DiagnosisSourceBrandAggRow> map = new LinkedHashMap<>();
        for (DiagnosisSourceBrandAggRow row : rows) {
            if (row == null || row.getBrandNo() == null || row.getBrandNo().isBlank()) {
                continue;
            }
            map.put(row.getBrandNo(), row);
        }
        return map;
    }

    private Map<String, DiagnosisSourceBrandStockRow> toStockMap(List<DiagnosisSourceBrandStockRow> rows) {
        Map<String, DiagnosisSourceBrandStockRow> map = new LinkedHashMap<>();
        for (DiagnosisSourceBrandStockRow row : rows) {
            if (row == null || row.getBrandNo() == null || row.getBrandNo().isBlank()) {
                continue;
            }
            map.put(row.getBrandNo(), row);
        }
        return map;
    }

    private Map<String, DiagnosisSourceBrandMetaRow> toMetaMap(List<DiagnosisSourceBrandMetaRow> rows) {
        Map<String, DiagnosisSourceBrandMetaRow> map = new LinkedHashMap<>();
        for (DiagnosisSourceBrandMetaRow row : rows) {
            if (row == null || row.getBrandNo() == null || row.getBrandNo().isBlank()) {
                continue;
            }
            map.put(row.getBrandNo(), row);
        }
        return map;
    }

    private void addKeys(Map<String, String> target, Iterable<String> source) {
        for (String key : source) {
            if (key == null || key.isBlank()) {
                continue;
            }
            target.putIfAbsent(key, key);
        }
    }

    private String resolveBrandName(String brandNo,
                                    DiagnosisSourceBrandAggRow current,
                                    DiagnosisSourceBrandAggRow compare,
                                    DiagnosisSourceBrandMetaRow meta) {
        if (current != null && hasText(current.getProductBrand())) {
            return current.getProductBrand();
        }
        if (compare != null && hasText(compare.getProductBrand())) {
            return compare.getProductBrand();
        }
        if (meta != null && hasText(meta.getProductBrand())) {
            return meta.getProductBrand();
        }
        return TYPE_NONE.equals(brandNo) ? TYPE_NONE_NAME : brandNo;
    }

    private String resolveBrandType(String brandNo,
                                    DiagnosisSourceBrandAggRow current,
                                    DiagnosisSourceBrandAggRow compare,
                                    DiagnosisSourceBrandMetaRow meta) {
        String value = firstText(
            current == null ? null : current.getBrandType(),
            compare == null ? null : compare.getBrandType(),
            meta == null ? null : meta.getBrandType()
        );
        if (hasText(value)) {
            return value;
        }
        return TYPE_NONE.equals(brandNo) ? TYPE_NONE : TYPE_OTHER;
    }

    private String resolveBrandTypeName(String brandType) {
        if (TYPE_OWN.equals(brandType)) {
            return TYPE_OWN_NAME;
        }
        if (TYPE_NONE.equals(brandType)) {
            return TYPE_NONE_NAME;
        }
        return TYPE_OTHER_NAME;
    }

    private boolean isNewBrand(LocalDate firstSaleDate, LocalDate periodStart, LocalDate periodEnd) {
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

    private static class BrandMetricCalc {
        private String brandNo;
        private String productBrand;
        private String brandType;
        private String brandTypeName;
        private String newBrandType;
        private String newBrandTypeName;
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
        private LocalDateTime snapshotTime;

        private BigDecimal getSales() {
            return sales;
        }

        private String getBrandNo() {
            return brandNo;
        }

        private BigDecimal getSalesIncSortable() {
            return salesInc == null ? new BigDecimal("-999999") : salesInc;
        }
    }
}

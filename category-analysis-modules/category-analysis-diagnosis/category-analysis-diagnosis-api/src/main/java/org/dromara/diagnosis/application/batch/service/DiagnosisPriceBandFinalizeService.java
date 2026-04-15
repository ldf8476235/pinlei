package org.dromara.diagnosis.application.batch.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.application.batch.model.DiagnosisFinalizeContext;
import org.dromara.diagnosis.application.batch.model.DiagnosisPriceBandFinalizeResult;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisBatchSourceMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPriceBandConfigMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPriceBandMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPriceBandConfigRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPriceBandLineRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPriceBandPointRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPriceBandRangeRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPriceBandSkuRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceAbcProductAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceAbcProductMetaRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceAbcProductStockRow;
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
public class DiagnosisPriceBandFinalizeService {

    private static final String TENANT_ID = "000000";
    private static final String SYSTEM_USER = "system";
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal MIN_BAND_WIDTH = new BigDecimal("0.01");
    private static final int DEFAULT_BAND_COUNT = 10;

    private final DiagnosisBatchSourceMapper batchSourceMapper;
    private final DiagnosisPriceBandConfigMapper priceBandConfigMapper;
    private final DiagnosisPriceBandMapper priceBandMapper;

    @Transactional(rollbackFor = Exception.class)
    public DiagnosisPriceBandFinalizeResult finalizePriceBand(DiagnosisFinalizeContext context) {
        List<DiagnosisSourceAbcProductAggRow> aggRows = safeAggRows(batchSourceMapper.aggregateAbcProductMetrics(context.getParam()));
        List<DiagnosisSourceAbcProductStockRow> stockRows = safeStockRows(
            batchSourceMapper.aggregateAbcProductStock(context.getParam(), context.getPeriodEnd()));
        List<DiagnosisSourceAbcProductMetaRow> metaRows = safeMetaRows(batchSourceMapper.selectAbcProductMeta(context.getParam()));

        List<DiagnosisPriceBandConfigRow> configRows = resolveConfigRows(context, aggRows);
        priceBandMapper.deleteByVersion(TENANT_ID, context.getQueryHash(), context.getDataVersion());
        if (aggRows.isEmpty() || configRows.isEmpty()) {
            return emptyResult();
        }

        Map<String, DiagnosisSourceAbcProductMetaRow> metaMap = toMetaMap(metaRows);
        Map<String, BigDecimal> stockMap = toStockMap(stockRows);
        List<PriceBandProductCalc> products = buildProductCalcs(context, aggRows, configRows, metaMap, stockMap);
        if (products.isEmpty()) {
            return emptyResult();
        }

        BigDecimal totalSales = BigDecimal.ZERO;
        BigDecimal totalSaleQuantity = BigDecimal.ZERO;
        BigDecimal totalGross = BigDecimal.ZERO;
        for (PriceBandProductCalc product : products) {
            totalSales = totalSales.add(product.sales);
            totalSaleQuantity = totalSaleQuantity.add(product.saleQuantity);
            totalGross = totalGross.add(product.gross);
        }

        List<DiagnosisPriceBandRangeRow> rangeRows = buildRangeRows(context, configRows, products, totalSales, totalSaleQuantity);
        List<DiagnosisPriceBandLineRow> lineRows = buildLineRows(context, products);
        List<DiagnosisPriceBandPointRow> pointRows = buildPointRows(context, configRows, products, rangeRows);
        List<DiagnosisPriceBandSkuRow> skuRows = buildSkuRows(context, products, totalSales, totalGross);

        if (!rangeRows.isEmpty()) {
            priceBandMapper.batchInsertRange(rangeRows);
        }
        if (!lineRows.isEmpty()) {
            priceBandMapper.batchInsertLine(lineRows);
        }
        if (!pointRows.isEmpty()) {
            priceBandMapper.batchInsertPoint(pointRows);
        }
        if (!skuRows.isEmpty()) {
            priceBandMapper.batchInsertSku(skuRows);
        }

        return DiagnosisPriceBandFinalizeResult.builder()
            .rangeRows((long) rangeRows.size())
            .lineRows((long) lineRows.size())
            .pointRows((long) pointRows.size())
            .skuRows((long) skuRows.size())
            .build();
    }

    private DiagnosisPriceBandFinalizeResult emptyResult() {
        return DiagnosisPriceBandFinalizeResult.builder()
            .rangeRows(0L)
            .lineRows(0L)
            .pointRows(0L)
            .skuRows(0L)
            .build();
    }

    private List<DiagnosisPriceBandConfigRow> resolveConfigRows(DiagnosisFinalizeContext context,
                                                                List<DiagnosisSourceAbcProductAggRow> aggRows) {
        List<DiagnosisPriceBandConfigRow> rows = priceBandConfigMapper.selectActiveConfig(TENANT_ID, context.getQueryHash());
        if (rows != null && !rows.isEmpty()) {
            rows.sort(Comparator.comparing(row -> row.getSortNo() == null ? Integer.MAX_VALUE : row.getSortNo()));
            return rows;
        }
        List<DiagnosisPriceBandConfigRow> generated = buildDefaultConfigRows(context, aggRows);
        if (generated.isEmpty()) {
            return generated;
        }
        priceBandConfigMapper.deleteActiveConfig(TENANT_ID, context.getQueryHash());
        priceBandConfigMapper.batchInsertConfig(generated);
        return generated;
    }

    private List<DiagnosisPriceBandConfigRow> buildDefaultConfigRows(DiagnosisFinalizeContext context,
                                                                     List<DiagnosisSourceAbcProductAggRow> aggRows) {
        BigDecimal min = null;
        BigDecimal max = null;
        for (DiagnosisSourceAbcProductAggRow row : aggRows) {
            BigDecimal qty = nvl(row == null ? null : row.getSaleQuantity());
            BigDecimal sales = nvl(row == null ? null : row.getSales());
            if (qty.compareTo(BigDecimal.ZERO) <= 0 || sales.compareTo(BigDecimal.ZERO) < 0) {
                continue;
            }
            BigDecimal avgDealPrice = sales.divide(qty, 6, RoundingMode.HALF_UP);
            if (min == null || avgDealPrice.compareTo(min) < 0) {
                min = avgDealPrice;
            }
            if (max == null || avgDealPrice.compareTo(max) > 0) {
                max = avgDealPrice;
            }
        }
        if (min == null || max == null) {
            return List.of();
        }

        LocalDateTime now = LocalDateTime.now();
        List<DiagnosisPriceBandConfigRow> rows = new ArrayList<>();
        BigDecimal start = min.setScale(2, RoundingMode.DOWN);
        BigDecimal endMax = max.setScale(2, RoundingMode.UP);
        BigDecimal width = endMax.subtract(start)
            .divide(BigDecimal.valueOf(Math.max(1, DEFAULT_BAND_COUNT - 1L)), 2, RoundingMode.UP);
        if (width.compareTo(MIN_BAND_WIDTH) < 0) {
            width = MIN_BAND_WIDTH;
        }

        for (int i = 0; i < DEFAULT_BAND_COUNT; i++) {
            DiagnosisPriceBandConfigRow row = new DiagnosisPriceBandConfigRow();
            row.setTenantId(TENANT_ID);
            row.setQueryHash(context.getQueryHash());
            row.setSortNo(i + 1);
            row.setPriceBandMin(start);
            if (i == DEFAULT_BAND_COUNT - 1) {
                row.setPriceBandMax(null);
                row.setIsOpenEnded("1");
            } else {
                BigDecimal next = start.add(width).setScale(2, RoundingMode.HALF_UP);
                row.setPriceBandMax(next);
                row.setIsOpenEnded("0");
                start = next;
            }
            row.setIsActive("1");
            row.setCreatedBy(SYSTEM_USER);
            row.setCreatedTime(now);
            row.setUpdatedBy(SYSTEM_USER);
            row.setUpdatedTime(now);
            rows.add(row);
        }
        return rows;
    }

    private List<PriceBandProductCalc> buildProductCalcs(DiagnosisFinalizeContext context,
                                                         List<DiagnosisSourceAbcProductAggRow> aggRows,
                                                         List<DiagnosisPriceBandConfigRow> configRows,
                                                         Map<String, DiagnosisSourceAbcProductMetaRow> metaMap,
                                                         Map<String, BigDecimal> stockMap) {
        List<PriceBandProductCalc> rows = new ArrayList<>();
        LocalDateTime snapshotTime = LocalDateTime.now();
        for (DiagnosisSourceAbcProductAggRow agg : aggRows) {
            if (agg == null || agg.getProductNo() == null || agg.getProductNo().isBlank()) {
                continue;
            }
            BigDecimal saleQuantity = nvl(agg.getSaleQuantity());
            if (saleQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal sales = nvl(agg.getSales());
            BigDecimal avgDealPrice = sales.divide(saleQuantity, 6, RoundingMode.HALF_UP);
            DiagnosisPriceBandConfigRow band = findBand(configRows, avgDealPrice);
            if (band == null) {
                continue;
            }
            DiagnosisSourceAbcProductMetaRow meta = metaMap.get(agg.getProductNo());
            PriceBandProductCalc calc = new PriceBandProductCalc();
            calc.productNo = agg.getProductNo();
            calc.productName = firstText(agg.getProductName(), meta == null ? null : meta.getProductName(), agg.getProductNo());
            calc.productBarcode = firstText(agg.getProductBarcode(), meta == null ? null : meta.getProductBarcode());
            calc.storeNum = agg.getStoreNum() == null ? 0 : agg.getStoreNum();
            calc.saleQuantity = saleQuantity;
            calc.sales = sales;
            calc.gross = nvl(agg.getGross());
            calc.promotionFlag = firstText(agg.getPromotionFlag(), "0");
            calc.firstSaleDate = agg.getFirstSaleDate() != null ? agg.getFirstSaleDate() : meta == null ? null : meta.getFirstSaleDate();
            calc.stockQuantity = stockMap.getOrDefault(agg.getProductNo(), BigDecimal.ZERO);
            calc.avgDealPrice = avgDealPrice;
            calc.bandMin = band.getPriceBandMin();
            calc.bandMax = band.getPriceBandMax();
            calc.bandLabel = buildBandLabel(band.getPriceBandMin(), band.getPriceBandMax(), "1".equals(band.getIsOpenEnded()));
            calc.meta = meta;
            calc.snapshotTime = snapshotTime;
            calc.periodDays = Math.max(1L, context.getPeriodDays());
            rows.add(calc);
        }
        rows.sort(Comparator.comparing((PriceBandProductCalc row) -> row.bandMin, Comparator.nullsFirst(BigDecimal::compareTo))
            .thenComparing(row -> row.avgDealPrice)
            .thenComparing(row -> row.productNo));
        return rows;
    }

    private List<DiagnosisPriceBandRangeRow> buildRangeRows(DiagnosisFinalizeContext context,
                                                            List<DiagnosisPriceBandConfigRow> configRows,
                                                            List<PriceBandProductCalc> products,
                                                            BigDecimal totalSales,
                                                            BigDecimal totalSaleQuantity) {
        Map<String, List<PriceBandProductCalc>> bandGroups = groupByBand(products);
        BigDecimal avgShare = configRows.isEmpty()
            ? BigDecimal.ZERO
            : HUNDRED.divide(BigDecimal.valueOf(configRows.size()), 6, RoundingMode.HALF_UP);
        List<DiagnosisPriceBandRangeRow> rows = new ArrayList<>();
        int totalSku = products.size();
        LocalDateTime snapshotTime = LocalDateTime.now();
        for (DiagnosisPriceBandConfigRow config : configRows) {
            String label = buildBandLabel(config.getPriceBandMin(), config.getPriceBandMax(), "1".equals(config.getIsOpenEnded()));
            List<PriceBandProductCalc> group = bandGroups.getOrDefault(label, List.of());
            BigDecimal bandSales = BigDecimal.ZERO;
            BigDecimal bandQty = BigDecimal.ZERO;
            int activitySku = 0;
            for (PriceBandProductCalc product : group) {
                bandSales = bandSales.add(product.sales);
                bandQty = bandQty.add(product.saleQuantity);
                if ("1".equals(product.promotionFlag)) {
                    activitySku++;
                }
            }

            BigDecimal salesPer = percent(bandSales, totalSales);
            BigDecimal saleQuantityPer = percent(bandQty, totalSaleQuantity);
            int suggestSku = totalSku == 0
                ? 0
                : BigDecimal.valueOf(totalSku).multiply(salesPer).divide(HUNDRED, 0, RoundingMode.HALF_UP).intValue();
            if (!group.isEmpty() && suggestSku <= 0) {
                suggestSku = 1;
            }

            DiagnosisPriceBandRangeRow row = new DiagnosisPriceBandRangeRow();
            row.setTenantId(TENANT_ID);
            row.setQueryHash(context.getQueryHash());
            row.setDataVersion(context.getDataVersion());
            row.setPriceBandMin(config.getPriceBandMin());
            row.setPriceBandMax(config.getPriceBandMax());
            row.setPriceBandLabel(label);
            row.setSku(group.size());
            row.setSkuPer(scale2(percent(BigDecimal.valueOf(group.size()), BigDecimal.valueOf(Math.max(1, totalSku)))));
            row.setSaleQuantity(scale2(bandQty));
            row.setSaleQuantityPer(scale2(saleQuantityPer));
            row.setSaleQuantityUnit(scale2(bandQty));
            row.setSales(scale2(bandSales));
            row.setSalesPer(scale2(salesPer));
            row.setActivitySku(activitySku);
            row.setSuggestSku(suggestSku);
            row.setSuggestSkuPer(scale2(percent(BigDecimal.valueOf(suggestSku), BigDecimal.valueOf(Math.max(1, totalSku)))));
            row.setSalePrice(scale2(ratio(bandSales, bandQty)));
            row.setBandLevel(resolveBandLevel(salesPer, saleQuantityPer, avgShare, avgShare));
            row.setStrategyCode(resolveStrategyCode(row.getBandLevel()));
            row.setSnapshotTime(snapshotTime);
            rows.add(row);
        }
        return rows;
    }

    private List<DiagnosisPriceBandLineRow> buildLineRows(DiagnosisFinalizeContext context,
                                                          List<PriceBandProductCalc> products) {
        Map<BigDecimal, LineAggregate> lineMap = new LinkedHashMap<>();
        for (PriceBandProductCalc product : products) {
            BigDecimal priceLine = scale2(product.avgDealPrice);
            LineAggregate aggregate = lineMap.computeIfAbsent(priceLine, key -> new LineAggregate());
            aggregate.sku++;
            aggregate.sales = aggregate.sales.add(product.sales);
            aggregate.saleQuantity = aggregate.saleQuantity.add(product.saleQuantity);
        }
        List<DiagnosisPriceBandLineRow> rows = new ArrayList<>();
        LocalDateTime snapshotTime = LocalDateTime.now();
        for (Map.Entry<BigDecimal, LineAggregate> entry : lineMap.entrySet()) {
            DiagnosisPriceBandLineRow row = new DiagnosisPriceBandLineRow();
            row.setTenantId(TENANT_ID);
            row.setQueryHash(context.getQueryHash());
            row.setDataVersion(context.getDataVersion());
            row.setPriceLine(entry.getKey());
            row.setSku(entry.getValue().sku);
            row.setSales(scale2(entry.getValue().sales));
            row.setSnapshotTime(snapshotTime);
            rows.add(row);
        }
        rows.sort(Comparator.comparing(DiagnosisPriceBandLineRow::getPriceLine, Comparator.nullsFirst(BigDecimal::compareTo)));
        return rows;
    }

    private List<DiagnosisPriceBandPointRow> buildPointRows(DiagnosisFinalizeContext context,
                                                            List<DiagnosisPriceBandConfigRow> configRows,
                                                            List<PriceBandProductCalc> products,
                                                            List<DiagnosisPriceBandRangeRow> rangeRows) {
        Map<String, DiagnosisPriceBandRangeRow> rangeMap = new LinkedHashMap<>();
        for (DiagnosisPriceBandRangeRow rangeRow : rangeRows) {
            rangeMap.put(rangeRow.getPriceBandLabel(), rangeRow);
        }
        Map<String, Map<BigDecimal, LineAggregate>> bandLineMap = new LinkedHashMap<>();
        for (PriceBandProductCalc product : products) {
            Map<BigDecimal, LineAggregate> lineMap = bandLineMap.computeIfAbsent(product.bandLabel, key -> new LinkedHashMap<>());
            BigDecimal priceLine = scale2(product.avgDealPrice);
            LineAggregate aggregate = lineMap.computeIfAbsent(priceLine, key -> new LineAggregate());
            aggregate.sku++;
            aggregate.sales = aggregate.sales.add(product.sales);
            aggregate.saleQuantity = aggregate.saleQuantity.add(product.saleQuantity);
        }

        List<DiagnosisPriceBandPointRow> rows = new ArrayList<>();
        LocalDateTime snapshotTime = LocalDateTime.now();
        for (DiagnosisPriceBandConfigRow config : configRows) {
            String label = buildBandLabel(config.getPriceBandMin(), config.getPriceBandMax(), "1".equals(config.getIsOpenEnded()));
            Map<BigDecimal, LineAggregate> lineMap = bandLineMap.get(label);
            if (lineMap == null || lineMap.isEmpty()) {
                continue;
            }
            DiagnosisPriceBandRangeRow rangeRow = rangeMap.get(label);
            Map.Entry<BigDecimal, LineAggregate> peak = lineMap.entrySet().stream()
                .max((left, right) -> {
                    int salesCompare = left.getValue().sales.compareTo(right.getValue().sales);
                    if (salesCompare != 0) {
                        return salesCompare;
                    }
                    int qtyCompare = left.getValue().saleQuantity.compareTo(right.getValue().saleQuantity);
                    if (qtyCompare != 0) {
                        return qtyCompare;
                    }
                    return right.getKey().compareTo(left.getKey());
                })
                .orElse(null);
            if (peak == null) {
                continue;
            }
            DiagnosisPriceBandPointRow row = new DiagnosisPriceBandPointRow();
            row.setTenantId(TENANT_ID);
            row.setQueryHash(context.getQueryHash());
            row.setDataVersion(context.getDataVersion());
            row.setPriceBandMin(config.getPriceBandMin());
            row.setPriceBandMax(config.getPriceBandMax());
            row.setSalePrice(peak.getKey());
            row.setSku(peak.getValue().sku);
            row.setSales(scale2(peak.getValue().sales));
            row.setSaleQuantity(scale2(peak.getValue().saleQuantity));
            row.setTotalSales(rangeRow == null ? BigDecimal.ZERO : nvl(rangeRow.getSales()));
            row.setWaveType("1");
            row.setSnapshotTime(snapshotTime);
            rows.add(row);
        }
        rows.sort(Comparator.comparing(DiagnosisPriceBandPointRow::getPriceBandMin, Comparator.nullsFirst(BigDecimal::compareTo)));
        return rows;
    }

    private List<DiagnosisPriceBandSkuRow> buildSkuRows(DiagnosisFinalizeContext context,
                                                        List<PriceBandProductCalc> products,
                                                        BigDecimal totalSales,
                                                        BigDecimal totalGross) {
        List<DiagnosisPriceBandSkuRow> rows = new ArrayList<>(products.size());
        for (PriceBandProductCalc product : products) {
            DiagnosisPriceBandSkuRow row = new DiagnosisPriceBandSkuRow();
            row.setTenantId(TENANT_ID);
            row.setQueryHash(context.getQueryHash());
            row.setDataVersion(context.getDataVersion());
            row.setPriceBandMin(product.bandMin);
            row.setPriceBandMax(product.bandMax);
            row.setPriceBandLabel(product.bandLabel);
            row.setPromotionFlag(product.promotionFlag);
            row.setProductNo(product.productNo);
            row.setProductName(product.productName);
            row.setStoreNum(product.storeNum);
            row.setSaleQuantity(scale2(product.saleQuantity));
            row.setSaleQuantityPsd(scale4(perStorePerDay(product.saleQuantity, product.storeNum, product.periodDays)));
            row.setSales(scale2(product.sales));
            row.setSalesPer(scale2(percent(product.sales, totalSales)));
            row.setSalesPsd(scale4(perStorePerDay(product.sales, product.storeNum, product.periodDays)));
            row.setGross(scale2(product.gross));
            row.setGrossPer(scale2(percent(product.gross, totalGross)));
            row.setGrossPsd(scale4(perStorePerDay(product.gross, product.storeNum, product.periodDays)));
            row.setGrossRate(scale2(percent(product.gross, product.sales)));
            row.setStockQuantity(scale2(product.stockQuantity));
            BigDecimal turnoverRate = ratio(product.sales, product.stockQuantity);
            row.setTurnoverRate(scale4(turnoverRate));
            row.setTurnoverDays(scale4(turnoverRate.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(product.periodDays).divide(turnoverRate, 6, RoundingMode.HALF_UP)));
            row.setStockSalesRate(scale2(percent(product.stockQuantity, product.saleQuantity)));
            row.setContributionRate(scale2(percent(product.sales, totalSales)));
            row.setGmroi(scale4(ratio(product.gross, product.stockQuantity)));
            row.setSalesRate(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            row.setActivity("1".equals(product.promotionFlag) ? "YES" : "NO");
            row.setFirstSaleDate(product.firstSaleDate);
            row.setNewProduct(isNewProduct(product.firstSaleDate, context.getPeriodStart()) ? "YES" : "NO");
            row.setKeyProduct("NO");
            row.setAvgDealPrice(scale2(product.avgDealPrice));
            row.setSnapshotTime(product.snapshotTime);

            if (product.meta != null) {
                row.setProductStatus(firstText(product.meta.getProductStatus(), "UNKNOWN"));
                row.setProductStatusNo(firstText(product.meta.getProductStatusNo(), "0"));
                row.setSeasonableFlag(firstText(product.meta.getSeasonableFlag(), "0"));
                row.setSeasonableFlagName(firstText(product.meta.getSeasonableFlagName(), "NO"));
                row.setSeasonableStartDate(product.meta.getSeasonableStartDate());
                row.setSeasonableEndDate(product.meta.getSeasonableEndDate());
                row.setClassNo(product.meta.getClassNo());
                row.setClassName(product.meta.getClassName());
                row.setClassLevel(product.meta.getClassLevel());
                row.setProductBarcode(firstText(product.productBarcode, product.meta.getProductBarcode()));
                row.setBrandName(product.meta.getBrandName());
                row.setSpec(product.meta.getSpec());
                row.setInPrice(scale2(product.meta.getInPrice()));
                row.setSalesPrice(scale2(product.meta.getSalesPrice()));
            } else {
                row.setProductStatus("UNKNOWN");
                row.setProductStatusNo("0");
                row.setSeasonableFlag("0");
                row.setSeasonableFlagName("NO");
                row.setProductBarcode(product.productBarcode);
                row.setInPrice(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
                row.setSalesPrice(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            }
            rows.add(row);
        }
        return rows;
    }

    private boolean isNewProduct(LocalDate firstSaleDate, LocalDate periodStart) {
        return firstSaleDate != null && periodStart != null && !firstSaleDate.isBefore(periodStart);
    }

    private Map<String, List<PriceBandProductCalc>> groupByBand(List<PriceBandProductCalc> products) {
        Map<String, List<PriceBandProductCalc>> map = new LinkedHashMap<>();
        for (PriceBandProductCalc product : products) {
            map.computeIfAbsent(product.bandLabel, key -> new ArrayList<>()).add(product);
        }
        return map;
    }

    private DiagnosisPriceBandConfigRow findBand(List<DiagnosisPriceBandConfigRow> configRows, BigDecimal avgDealPrice) {
        if (avgDealPrice == null) {
            return null;
        }
        for (int i = 0; i < configRows.size(); i++) {
            DiagnosisPriceBandConfigRow row = configRows.get(i);
            BigDecimal min = nvl(row.getPriceBandMin());
            BigDecimal max = row.getPriceBandMax();
            boolean openEnded = "1".equals(row.getIsOpenEnded()) || max == null;
            if (avgDealPrice.compareTo(min) < 0) {
                continue;
            }
            if (openEnded) {
                return row;
            }
            if (avgDealPrice.compareTo(max) < 0 || (i == configRows.size() - 1 && avgDealPrice.compareTo(max) <= 0)) {
                return row;
            }
        }
        return configRows.isEmpty() ? null : configRows.get(configRows.size() - 1);
    }

    private String resolveBandLevel(BigDecimal salesPer, BigDecimal qtyPer, BigDecimal avgSalesPer, BigDecimal avgQtyPer) {
        if (salesPer.compareTo(avgSalesPer) >= 0 && qtyPer.compareTo(avgQtyPer) >= 0) {
            return "MAIN";
        }
        if (salesPer.compareTo(avgSalesPer) < 0 && qtyPer.compareTo(avgQtyPer) < 0) {
            return "TAIL";
        }
        return "NORMAL";
    }

    private String resolveStrategyCode(String bandLevel) {
        if ("MAIN".equals(bandLevel)) {
            return "KEEP_ENRICH";
        }
        if ("TAIL".equals(bandLevel)) {
            return "REDUCE_REPLACE";
        }
        return "OPTIMIZE";
    }

    private Map<String, DiagnosisSourceAbcProductMetaRow> toMetaMap(List<DiagnosisSourceAbcProductMetaRow> rows) {
        Map<String, DiagnosisSourceAbcProductMetaRow> map = new LinkedHashMap<>();
        for (DiagnosisSourceAbcProductMetaRow row : rows) {
            if (row == null || row.getProductNo() == null) {
                continue;
            }
            map.put(row.getProductNo(), row);
        }
        return map;
    }

    private Map<String, BigDecimal> toStockMap(List<DiagnosisSourceAbcProductStockRow> rows) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (DiagnosisSourceAbcProductStockRow row : rows) {
            if (row == null || row.getProductNo() == null) {
                continue;
            }
            map.put(row.getProductNo(), nvl(row.getStockQuantity()));
        }
        return map;
    }

    private List<DiagnosisSourceAbcProductAggRow> safeAggRows(List<DiagnosisSourceAbcProductAggRow> rows) {
        return rows == null ? List.of() : rows;
    }

    private List<DiagnosisSourceAbcProductStockRow> safeStockRows(List<DiagnosisSourceAbcProductStockRow> rows) {
        return rows == null ? List.of() : rows;
    }

    private List<DiagnosisSourceAbcProductMetaRow> safeMetaRows(List<DiagnosisSourceAbcProductMetaRow> rows) {
        return rows == null ? List.of() : rows;
    }

    private BigDecimal perStorePerDay(BigDecimal value, Integer storeNum, long periodDays) {
        if (storeNum == null || storeNum <= 0 || periodDays <= 0) {
            return BigDecimal.ZERO;
        }
        return nvl(value).divide(BigDecimal.valueOf((long) storeNum * periodDays), 6, RoundingMode.HALF_UP);
    }

    private BigDecimal ratio(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return nvl(numerator).divide(denominator, 6, RoundingMode.HALF_UP);
    }

    private BigDecimal percent(BigDecimal numerator, BigDecimal denominator) {
        return ratio(numerator, denominator).multiply(HUNDRED);
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

    private String buildBandLabel(BigDecimal min, BigDecimal max, boolean openEnded) {
        if (openEnded || max == null) {
            return formatBandValue(min) + "+";
        }
        return formatBandValue(min) + "-" + formatBandValue(max);
    }

    private String formatBandValue(BigDecimal value) {
        return scale2(value).stripTrailingZeros().toPlainString();
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static class PriceBandProductCalc {
        private String productNo;
        private String productName;
        private String productBarcode;
        private Integer storeNum;
        private BigDecimal saleQuantity = BigDecimal.ZERO;
        private BigDecimal sales = BigDecimal.ZERO;
        private BigDecimal gross = BigDecimal.ZERO;
        private BigDecimal stockQuantity = BigDecimal.ZERO;
        private BigDecimal avgDealPrice = BigDecimal.ZERO;
        private BigDecimal bandMin = BigDecimal.ZERO;
        private BigDecimal bandMax;
        private String bandLabel;
        private String promotionFlag = "0";
        private LocalDate firstSaleDate;
        private DiagnosisSourceAbcProductMetaRow meta;
        private LocalDateTime snapshotTime;
        private long periodDays;
    }

    private static class LineAggregate {
        private int sku;
        private BigDecimal sales = BigDecimal.ZERO;
        private BigDecimal saleQuantity = BigDecimal.ZERO;
    }
}

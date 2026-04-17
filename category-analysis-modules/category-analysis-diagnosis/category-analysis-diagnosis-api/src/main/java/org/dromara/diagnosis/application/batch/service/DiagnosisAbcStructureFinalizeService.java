package org.dromara.diagnosis.application.batch.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.application.batch.model.DiagnosisAbcFinalizeResult;
import org.dromara.diagnosis.application.batch.model.DiagnosisFinalizeContext;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisAbcStructureMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisBatchSourceMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisAbcBucketRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisAbcMatrixRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisAbcParamConfigRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisAbcParamSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisAbcSkuRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceAbcProductAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceAbcProductMetaRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceAbcProductStockRow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiagnosisAbcStructureFinalizeService {

    private static final String TENANT_ID = "000000";
    private static final List<String> ABC_TYPES = List.of("sales", "gross", "contribution");
    private static final int SKU_INSERT_BATCH_SIZE = 30;

    private final DiagnosisBatchSourceMapper batchSourceMapper;
    private final DiagnosisAbcStructureMapper abcStructureMapper;

    public DiagnosisAbcFinalizeResult finalizeAbcStructure(DiagnosisFinalizeContext context) {
        abcStructureMapper.deleteByVersion(TENANT_ID, context.getQueryHash(), context.getDataVersion());

        List<DiagnosisAbcParamConfigRow> configRows = loadParamConfig();
        List<DiagnosisAbcParamSnapshotRow> paramSnapshots = buildParamSnapshots(context, configRows);
        if (!paramSnapshots.isEmpty()) {
            abcStructureMapper.batchInsertParamSnapshot(paramSnapshots);
        }

        List<DiagnosisSourceAbcProductAggRow> currentAgg = safeAggRows(batchSourceMapper.aggregateAbcProductMetrics(context.getParam()));
        List<DiagnosisSourceAbcProductAggRow> compareAgg = context.getCompareParam() == null
            ? List.of()
            : safeAggRows(batchSourceMapper.aggregateAbcProductMetrics(context.getCompareParam()));
        List<DiagnosisSourceAbcProductStockRow> stockRows = safeStockRows(batchSourceMapper.aggregateAbcProductStock(context.getParam(), context.getPeriodEnd()));
        List<DiagnosisSourceAbcProductMetaRow> metaRows = safeMetaRows(batchSourceMapper.selectAbcProductMeta(context.getParam()));

        Map<String, ProductMetric> currentMap = toMetricMap(currentAgg);
        Map<String, ProductMetric> compareMap = toMetricMap(compareAgg);
        Map<String, BigDecimal> stockMap = toStockMap(stockRows);
        Map<String, DiagnosisSourceAbcProductMetaRow> metaMap = toMetaMap(metaRows);
        Map<String, String> productNameMap = buildProductNameMap(currentAgg, compareAgg, metaRows);

        List<DiagnosisAbcBucketRow> bucketRows = new ArrayList<>();
        List<DiagnosisAbcSkuRow> skuRows = new ArrayList<>();
        List<DiagnosisAbcMatrixRow> matrixRows = new ArrayList<>();

        for (DiagnosisAbcParamConfigRow cfg : configRows) {
            AbcResult result = calculateAbc(context, cfg, currentMap, compareMap, stockMap, metaMap, productNameMap);
            bucketRows.addAll(result.buckets());
            skuRows.addAll(result.skus());
            matrixRows.add(result.matrix());
        }

        if (!bucketRows.isEmpty()) {
            abcStructureMapper.batchInsertBucket(bucketRows);
        }
        for (DiagnosisAbcMatrixRow row : matrixRows) {
            abcStructureMapper.insertMatrix(row);
        }
        if (!skuRows.isEmpty()) {
            batchInsertSkuRows(skuRows);
        }

        return DiagnosisAbcFinalizeResult.builder()
            .paramsRows(paramSnapshots.size())
            .bucketRows(bucketRows.size())
            .matrixRows(matrixRows.size())
            .skuRows(skuRows.size())
            .build();
    }

    private void batchInsertSkuRows(List<DiagnosisAbcSkuRow> rows) {
        for (int start = 0; start < rows.size(); start += SKU_INSERT_BATCH_SIZE) {
            int end = Math.min(start + SKU_INSERT_BATCH_SIZE, rows.size());
            abcStructureMapper.batchInsertSku(rows.subList(start, end));
        }
    }

    private List<DiagnosisAbcParamConfigRow> loadParamConfig() {
        List<DiagnosisAbcParamConfigRow> rows = abcStructureMapper.selectParamConfig(TENANT_ID);
        Map<String, DiagnosisAbcParamConfigRow> map = new LinkedHashMap<>();
        if (rows != null) {
            for (DiagnosisAbcParamConfigRow row : rows) {
                if (row == null || row.getAbcType() == null) {
                    continue;
                }
                map.put(row.getAbcType().trim().toLowerCase(Locale.ROOT), normalizeConfig(row));
            }
        }
        for (String abcType : ABC_TYPES) {
            map.putIfAbsent(abcType, defaultConfig(abcType));
        }
        return new ArrayList<>(map.values());
    }

    private DiagnosisAbcParamConfigRow normalizeConfig(DiagnosisAbcParamConfigRow row) {
        row.setTenantId(TENANT_ID);
        row.setAbcType(row.getAbcType().trim().toLowerCase(Locale.ROOT));
        row.setARate(nvl(row.getARate(), new BigDecimal("50")));
        row.setBRate(nvl(row.getBRate(), new BigDecimal("40")));
        row.setCRate(nvl(row.getCRate(), new BigDecimal("10")));
        row.setASkuRate(nvl(row.getASkuRate(), new BigDecimal("10")));
        row.setBSkuRate(nvl(row.getBSkuRate(), new BigDecimal("30")));
        row.setCSkuRate(nvl(row.getCSkuRate(), new BigDecimal("60")));
        if (row.getSalesPer() == null && "contribution".equals(row.getAbcType())) {
            row.setSalesPer(new BigDecimal("0.40"));
        }
        if (row.getGrossPer() == null && "contribution".equals(row.getAbcType())) {
            row.setGrossPer(new BigDecimal("0.30"));
        }
        if (row.getSaleQuantityPer() == null && "contribution".equals(row.getAbcType())) {
            row.setSaleQuantityPer(new BigDecimal("0.30"));
        }
        return row;
    }

    private DiagnosisAbcParamConfigRow defaultConfig(String abcType) {
        DiagnosisAbcParamConfigRow row = new DiagnosisAbcParamConfigRow();
        row.setTenantId(TENANT_ID);
        row.setAbcType(abcType);
        row.setARate(new BigDecimal("50"));
        row.setBRate(new BigDecimal("40"));
        row.setCRate(new BigDecimal("10"));
        row.setASkuRate(new BigDecimal("10"));
        row.setBSkuRate(new BigDecimal("30"));
        row.setCSkuRate(new BigDecimal("60"));
        if ("sales".equals(abcType)) {
            row.setAbcTypeName("销售额ABC");
        } else if ("gross".equals(abcType)) {
            row.setAbcTypeName("毛利额ABC");
        } else {
            row.setAbcTypeName("综合业绩ABC");
            row.setSalesPer(new BigDecimal("0.40"));
            row.setGrossPer(new BigDecimal("0.30"));
            row.setSaleQuantityPer(new BigDecimal("0.30"));
        }
        return row;
    }

    private List<DiagnosisAbcParamSnapshotRow> buildParamSnapshots(DiagnosisFinalizeContext context, List<DiagnosisAbcParamConfigRow> configRows) {
        List<DiagnosisAbcParamSnapshotRow> rows = new ArrayList<>(configRows.size());
        for (DiagnosisAbcParamConfigRow cfg : configRows) {
            DiagnosisAbcParamSnapshotRow row = new DiagnosisAbcParamSnapshotRow();
            row.setTenantId(TENANT_ID);
            row.setQueryHash(context.getQueryHash());
            row.setDataVersion(context.getDataVersion());
            row.setAbcType(cfg.getAbcType());
            row.setAbcTypeName(cfg.getAbcTypeName());
            row.setSalesPer(cfg.getSalesPer());
            row.setGrossPer(cfg.getGrossPer());
            row.setSaleQuantityPer(cfg.getSaleQuantityPer());
            row.setARate(cfg.getARate());
            row.setBRate(cfg.getBRate());
            row.setCRate(cfg.getCRate());
            row.setASkuRate(cfg.getASkuRate());
            row.setBSkuRate(cfg.getBSkuRate());
            row.setCSkuRate(cfg.getCSkuRate());
            rows.add(row);
        }
        return rows;
    }

    private AbcResult calculateAbc(DiagnosisFinalizeContext context,
                                   DiagnosisAbcParamConfigRow cfg,
                                   Map<String, ProductMetric> currentMap,
                                   Map<String, ProductMetric> compareMap,
                                   Map<String, BigDecimal> stockMap,
                                   Map<String, DiagnosisSourceAbcProductMetaRow> metaMap,
                                   Map<String, String> productNameMap) {
        Map<String, ProductCalc> union = new LinkedHashMap<>();
        for (String productNo : currentMap.keySet()) {
            union.putIfAbsent(productNo, new ProductCalc(productNo));
        }
        for (String productNo : compareMap.keySet()) {
            union.putIfAbsent(productNo, new ProductCalc(productNo));
        }

        BigDecimal currentSalesTotal = BigDecimal.ZERO;
        BigDecimal currentGrossTotal = BigDecimal.ZERO;
        BigDecimal currentQtyTotal = BigDecimal.ZERO;
        BigDecimal compareSalesTotal = BigDecimal.ZERO;
        BigDecimal compareGrossTotal = BigDecimal.ZERO;
        BigDecimal compareQtyTotal = BigDecimal.ZERO;

        for (ProductCalc calc : union.values()) {
            ProductMetric current = currentMap.get(calc.productNo);
            ProductMetric compare = compareMap.get(calc.productNo);
            if (current != null) {
                calc.currentSales = current.sales;
                calc.currentGross = current.gross;
                calc.currentQty = current.saleQuantity;
                calc.currentStoreNum = current.storeNum;
                calc.promotionFlag = current.promotionFlag;
                calc.firstSaleDate = current.firstSaleDate;
            }
            if (compare != null) {
                calc.compareSales = compare.sales;
                calc.compareGross = compare.gross;
                calc.compareQty = compare.saleQuantity;
            }
            calc.stockQuantity = stockMap.getOrDefault(calc.productNo, BigDecimal.ZERO);
            calc.meta = metaMap.get(calc.productNo);
            calc.productName = productNameMap.getOrDefault(calc.productNo, calc.productNo);
            currentSalesTotal = currentSalesTotal.add(calc.currentSales);
            currentGrossTotal = currentGrossTotal.add(calc.currentGross);
            currentQtyTotal = currentQtyTotal.add(calc.currentQty);
            compareSalesTotal = compareSalesTotal.add(calc.compareSales);
            compareGrossTotal = compareGrossTotal.add(calc.compareGross);
            compareQtyTotal = compareQtyTotal.add(calc.compareQty);
        }

        for (ProductCalc calc : union.values()) {
            calc.currentMetric = metricOf(cfg.getAbcType(), calc.currentSales, calc.currentGross, calc.currentQty);
            calc.compareMetric = metricOf(cfg.getAbcType(), calc.compareSales, calc.compareGross, calc.compareQty);
            if ("contribution".equals(cfg.getAbcType())) {
                BigDecimal salesNormCurrent = ratio(calc.currentSales, currentSalesTotal);
                BigDecimal grossNormCurrent = ratio(calc.currentGross, currentGrossTotal);
                BigDecimal qtyNormCurrent = ratio(calc.currentQty, currentQtyTotal);
                BigDecimal salesNormCompare = ratio(calc.compareSales, compareSalesTotal);
                BigDecimal grossNormCompare = ratio(calc.compareGross, compareGrossTotal);
                BigDecimal qtyNormCompare = ratio(calc.compareQty, compareQtyTotal);
                calc.currentMetric = salesNormCurrent.multiply(nvl(cfg.getSalesPer(), BigDecimal.ZERO))
                    .add(grossNormCurrent.multiply(nvl(cfg.getGrossPer(), BigDecimal.ZERO)))
                    .add(qtyNormCurrent.multiply(nvl(cfg.getSaleQuantityPer(), BigDecimal.ZERO)));
                calc.compareMetric = salesNormCompare.multiply(nvl(cfg.getSalesPer(), BigDecimal.ZERO))
                    .add(grossNormCompare.multiply(nvl(cfg.getGrossPer(), BigDecimal.ZERO)))
                    .add(qtyNormCompare.multiply(nvl(cfg.getSaleQuantityPer(), BigDecimal.ZERO)));
            }
        }

        List<ProductCalc> currentRanked = new ArrayList<>(union.values());
        currentRanked.sort(Comparator
            .comparing((ProductCalc x) -> x.currentMetric, Comparator.reverseOrder())
            .thenComparing(x -> x.productNo, Comparator.naturalOrder()));
        List<ProductCalc> compareRanked = new ArrayList<>(union.values());
        compareRanked.sort(Comparator
            .comparing((ProductCalc x) -> x.compareMetric, Comparator.reverseOrder())
            .thenComparing(x -> x.productNo, Comparator.naturalOrder()));

        assignBucket(currentRanked, true, cfg);
        assignBucket(compareRanked, false, cfg);

        BigDecimal currentMetricTotal = currentRanked.stream().map(v -> v.currentMetric).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal compareMetricTotal = compareRanked.stream().map(v -> v.compareMetric).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal stockTotal = union.values().stream().map(v -> v.stockQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal currentGrossSum = union.values().stream().map(v -> v.currentGross).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, DiagnosisAbcBucketRow> bucketMap = new LinkedHashMap<>();
        for (String bucket : List.of("A", "B", "C")) {
            DiagnosisAbcBucketRow row = new DiagnosisAbcBucketRow();
            row.setTenantId(TENANT_ID);
            row.setQueryHash(context.getQueryHash());
            row.setDataVersion(context.getDataVersion());
            row.setAbcType(cfg.getAbcType());
            row.setBucket(bucket);
            row.setSetSalesPer(setSalesRate(bucket, cfg));
            row.setSetSkuPer(setSkuRate(bucket, cfg));
            row.setCurrentSku(0);
            row.setCompareSku(0);
            row.setCurrentSales(BigDecimal.ZERO);
            row.setStockQuantity(BigDecimal.ZERO);
            row.setCurrentSalesPer(BigDecimal.ZERO);
            row.setCurrentSkuPer(BigDecimal.ZERO);
            row.setCompareSalesPer(BigDecimal.ZERO);
            row.setCompareSkuPer(BigDecimal.ZERO);
            row.setStockQuantityPer(BigDecimal.ZERO);
            bucketMap.put(bucket, row);
        }

        for (ProductCalc calc : union.values()) {
            if (calc.currentAbc != null && bucketMap.containsKey(calc.currentAbc)) {
                DiagnosisAbcBucketRow row = bucketMap.get(calc.currentAbc);
                row.setCurrentSku(row.getCurrentSku() + 1);
                row.setCurrentSales(row.getCurrentSales().add(calc.currentMetric));
                row.setStockQuantity(row.getStockQuantity().add(calc.stockQuantity));
            }
            if (calc.compareAbc != null && bucketMap.containsKey(calc.compareAbc)) {
                DiagnosisAbcBucketRow row = bucketMap.get(calc.compareAbc);
                row.setCompareSku(row.getCompareSku() + 1);
            }
        }

        int skuTotalCurrent = (int) union.values().stream().filter(v -> v.currentAbc != null).count();
        int skuTotalCompare = (int) union.values().stream().filter(v -> v.compareAbc != null).count();
        for (DiagnosisAbcBucketRow row : bucketMap.values()) {
            row.setChangeSku(row.getCurrentSku() - row.getCompareSku());
            row.setCurrentSalesPer(scale2(percent(row.getCurrentSales(), currentMetricTotal)));
            row.setCurrentSkuPer(scale2(percent(BigDecimal.valueOf(row.getCurrentSku()), BigDecimal.valueOf(Math.max(1, skuTotalCurrent)))));
            row.setCompareSalesPer(scale2(percent(compareBucketMetric(union.values(), row.getBucket(), false), compareMetricTotal)));
            row.setCompareSkuPer(scale2(percent(BigDecimal.valueOf(row.getCompareSku()), BigDecimal.valueOf(Math.max(1, skuTotalCompare)))));
            row.setStockQuantityPer(scale2(percent(row.getStockQuantity(), stockTotal)));
            row.setCurrentSales(scale2(row.getCurrentSales()));
            row.setStockQuantity(scale2(row.getStockQuantity()));
        }

        DiagnosisAbcMatrixRow matrix = buildMatrix(context, cfg, union.values());
        List<DiagnosisAbcSkuRow> skuRows = new ArrayList<>(union.size());
        for (ProductCalc calc : union.values()) {
            skuRows.add(toSkuRow(context, cfg, calc, currentSalesTotal, currentGrossSum, currentMetricTotal));
        }

        return new AbcResult(new ArrayList<>(bucketMap.values()), matrix, skuRows);
    }

    private void assignBucket(List<ProductCalc> ranked, boolean current, DiagnosisAbcParamConfigRow cfg) {
        BigDecimal total = BigDecimal.ZERO;
        for (ProductCalc item : ranked) {
            total = total.add(current ? item.currentMetric : item.compareMetric);
        }
        BigDecimal cumulative = BigDecimal.ZERO;
        int rank = 0;
        BigDecimal prev = null;
        for (ProductCalc item : ranked) {
            BigDecimal metric = current ? item.currentMetric : item.compareMetric;
            if (metric.compareTo(BigDecimal.ZERO) <= 0) {
                if (current) {
                    item.currentAbc = null;
                } else {
                    item.compareAbc = null;
                }
                continue;
            }
            if (prev == null || metric.compareTo(prev) != 0) {
                rank++;
                prev = metric;
            }
            cumulative = cumulative.add(metric);
            BigDecimal cumulativePer = percent(cumulative, total);
            String bucket = resolveBucket(cumulativePer, cfg);
            if (current) {
                item.currentRank = rank;
                item.currentAbc = bucket;
            } else {
                item.compareRank = rank;
                item.compareAbc = bucket;
            }
        }
    }

    private String resolveBucket(BigDecimal cumulativePer, DiagnosisAbcParamConfigRow cfg) {
        BigDecimal a = nvl(cfg.getARate(), BigDecimal.ZERO);
        BigDecimal b = nvl(cfg.getBRate(), BigDecimal.ZERO);
        if (cumulativePer.compareTo(a) <= 0) {
            return "A";
        }
        if (cumulativePer.compareTo(a.add(b)) <= 0) {
            return "B";
        }
        return "C";
    }

    private DiagnosisAbcMatrixRow buildMatrix(DiagnosisFinalizeContext context,
                                              DiagnosisAbcParamConfigRow cfg,
                                              Iterable<ProductCalc> rows) {
        int aa = 0, ab = 0, ac = 0, an = 0;
        int ba = 0, bb = 0, bc = 0, bn = 0;
        int ca = 0, cb = 0, cc = 0, cn = 0;
        for (ProductCalc row : rows) {
            String cur = row.currentAbc;
            String cmp = row.compareAbc;
            if (cur == null) {
                continue;
            }
            if ("A".equals(cur)) {
                if ("A".equals(cmp)) aa++; else if ("B".equals(cmp)) ab++; else if ("C".equals(cmp)) ac++; else an++;
            } else if ("B".equals(cur)) {
                if ("A".equals(cmp)) ba++; else if ("B".equals(cmp)) bb++; else if ("C".equals(cmp)) bc++; else bn++;
            } else if ("C".equals(cur)) {
                if ("A".equals(cmp)) ca++; else if ("B".equals(cmp)) cb++; else if ("C".equals(cmp)) cc++; else cn++;
            }
        }
        DiagnosisAbcMatrixRow row = new DiagnosisAbcMatrixRow();
        row.setTenantId(TENANT_ID);
        row.setQueryHash(context.getQueryHash());
        row.setDataVersion(context.getDataVersion());
        row.setAbcType(cfg.getAbcType());
        row.setAaNum(aa); row.setAbNum(ab); row.setAcNum(ac); row.setAnNum(an); row.setAtNum(aa + ab + ac + an);
        row.setBaNum(ba); row.setBbNum(bb); row.setBcNum(bc); row.setBnNum(bn); row.setBtNum(ba + bb + bc + bn);
        row.setCaNum(ca); row.setCbNum(cb); row.setCcNum(cc); row.setCnNum(cn); row.setCtNum(ca + cb + cc + cn);
        return row;
    }

    private DiagnosisAbcSkuRow toSkuRow(DiagnosisFinalizeContext context,
                                        DiagnosisAbcParamConfigRow cfg,
                                        ProductCalc calc,
                                        BigDecimal currentSalesTotal,
                                        BigDecimal currentGrossTotal,
                                        BigDecimal currentMetricTotal) {
        DiagnosisAbcSkuRow row = new DiagnosisAbcSkuRow();
        row.setTenantId(TENANT_ID);
        row.setQueryHash(context.getQueryHash());
        row.setDataVersion(context.getDataVersion());
        row.setAbcType(cfg.getAbcType());
        row.setProductNo(calc.productNo);
        row.setProductName(calc.productName);
        row.setStoreNum(calc.currentStoreNum);
        row.setCurrentAbc(calc.currentAbc);
        row.setCompareAbc(calc.compareAbc);
        row.setContribution(scale6(calc.currentMetric.multiply(new BigDecimal("10000"))));
        row.setContributionPer(scale6(ratio(calc.currentMetric, currentMetricTotal)));
        row.setSaleQuantity(scale2(calc.currentQty));
        row.setSaleQuantityPsd(scale4(calc.currentStoreNum == 0 ? BigDecimal.ZERO : calc.currentQty
            .divide(BigDecimal.valueOf(calc.currentStoreNum * Math.max(1L, context.getPeriodDays())), 6, RoundingMode.HALF_UP)));
        row.setSales(scale2(calc.currentSales));
        row.setSalesPer(scale6(ratio(calc.currentSales, currentSalesTotal)));
        row.setSalesPsd(scale4(calc.currentStoreNum == 0 ? BigDecimal.ZERO : calc.currentSales
            .divide(BigDecimal.valueOf(calc.currentStoreNum * Math.max(1L, context.getPeriodDays())), 6, RoundingMode.HALF_UP)));
        row.setGross(scale2(calc.currentGross));
        row.setGrossPer(scale6(ratio(calc.currentGross, currentGrossTotal)));
        row.setGrossPsd(scale4(calc.currentStoreNum == 0 ? BigDecimal.ZERO : calc.currentGross
            .divide(BigDecimal.valueOf(calc.currentStoreNum * Math.max(1L, context.getPeriodDays())), 6, RoundingMode.HALF_UP)));
        row.setGrossRate(scale6(ratio(calc.currentGross, calc.currentSales)));
        row.setStockQuantity(scale2(calc.stockQuantity));
        BigDecimal turnoverRate = ratio(calc.currentSales, calc.stockQuantity);
        row.setTurnoverRate(scale4(turnoverRate));
        row.setTurnoverDays(scale4(turnoverRate.compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(Math.max(1L, context.getPeriodDays())).divide(turnoverRate, 6, RoundingMode.HALF_UP)));
        row.setStockSalesRate(scale4(ratio(calc.stockQuantity, calc.currentQty)));
        row.setContributionRate(scale6(ratio(calc.currentGross, currentGrossTotal)));
        row.setGmroi(scale4(ratio(calc.currentGross, calc.stockQuantity)));
        row.setSalesRate(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        row.setActivity("1".equals(calc.promotionFlag) ? "是" : "否");
        row.setFirstSaleDate(calc.firstSaleDate);
        row.setNewProduct(calc.firstSaleDate != null && !calc.firstSaleDate.isBefore(context.getPeriodStart()) ? "是" : "否");
        row.setKeyProduct("否");
        row.setPromotionFlag(calc.promotionFlag);

        if (calc.meta != null) {
            row.setProductStatus(calc.meta.getProductStatus());
            row.setProductStatusNo(calc.meta.getProductStatusNo());
            row.setSeasonableFlag(calc.meta.getSeasonableFlag());
            row.setSeasonableFlagName(calc.meta.getSeasonableFlagName());
            row.setSeasonableStartDate(calc.meta.getSeasonableStartDate());
            row.setSeasonableEndDate(calc.meta.getSeasonableEndDate());
            row.setClassNo(calc.meta.getClassNo());
            row.setClassName(calc.meta.getClassName());
            row.setClassLevel(calc.meta.getClassLevel());
            row.setProductBarcode(calc.meta.getProductBarcode());
            row.setBrandName(calc.meta.getBrandName());
            row.setSpec(calc.meta.getSpec());
            row.setInPrice(scale2(calc.meta.getInPrice()));
            row.setSalesPrice(scale2(calc.meta.getSalesPrice()));
        } else {
            row.setProductStatus("未知");
            row.setProductStatusNo("0");
            row.setSeasonableFlag("0");
            row.setSeasonableFlagName("否");
            row.setInPrice(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            row.setSalesPrice(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
        return row;
    }

    private BigDecimal compareBucketMetric(Iterable<ProductCalc> rows, String bucket, boolean current) {
        BigDecimal total = BigDecimal.ZERO;
        for (ProductCalc row : rows) {
            String b = current ? row.currentAbc : row.compareAbc;
            if (bucket.equals(b)) {
                total = total.add(current ? row.currentMetric : row.compareMetric);
            }
        }
        return total;
    }

    private BigDecimal setSalesRate(String bucket, DiagnosisAbcParamConfigRow cfg) {
        if ("A".equals(bucket)) return scale2(cfg.getARate());
        if ("B".equals(bucket)) return scale2(cfg.getBRate());
        return scale2(cfg.getCRate());
    }

    private BigDecimal setSkuRate(String bucket, DiagnosisAbcParamConfigRow cfg) {
        if ("A".equals(bucket)) return scale2(cfg.getASkuRate());
        if ("B".equals(bucket)) return scale2(cfg.getBSkuRate());
        return scale2(cfg.getCSkuRate());
    }

    private Map<String, ProductMetric> toMetricMap(List<DiagnosisSourceAbcProductAggRow> rows) {
        Map<String, ProductMetric> map = new LinkedHashMap<>();
        for (DiagnosisSourceAbcProductAggRow row : rows) {
            if (row == null || row.getProductNo() == null) continue;
            map.put(row.getProductNo(), new ProductMetric(
                nvl(row.getSales(), BigDecimal.ZERO),
                nvl(row.getGross(), BigDecimal.ZERO),
                nvl(row.getSaleQuantity(), BigDecimal.ZERO),
                row.getStoreNum() == null ? 0 : row.getStoreNum(),
                row.getPromotionFlag() == null ? "0" : row.getPromotionFlag(),
                row.getFirstSaleDate()
            ));
        }
        return map;
    }

    private Map<String, BigDecimal> toStockMap(List<DiagnosisSourceAbcProductStockRow> rows) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (DiagnosisSourceAbcProductStockRow row : rows) {
            if (row == null || row.getProductNo() == null) continue;
            map.put(row.getProductNo(), nvl(row.getStockQuantity(), BigDecimal.ZERO));
        }
        return map;
    }

    private Map<String, DiagnosisSourceAbcProductMetaRow> toMetaMap(List<DiagnosisSourceAbcProductMetaRow> rows) {
        Map<String, DiagnosisSourceAbcProductMetaRow> map = new LinkedHashMap<>();
        for (DiagnosisSourceAbcProductMetaRow row : rows) {
            if (row == null || row.getProductNo() == null) continue;
            map.put(row.getProductNo(), row);
        }
        return map;
    }

    private Map<String, String> buildProductNameMap(List<DiagnosisSourceAbcProductAggRow> current,
                                                    List<DiagnosisSourceAbcProductAggRow> compare,
                                                    List<DiagnosisSourceAbcProductMetaRow> metaRows) {
        Map<String, String> map = new LinkedHashMap<>();
        for (DiagnosisSourceAbcProductAggRow row : current) {
            if (row != null && row.getProductNo() != null && row.getProductName() != null) map.putIfAbsent(row.getProductNo(), row.getProductName());
        }
        for (DiagnosisSourceAbcProductAggRow row : compare) {
            if (row != null && row.getProductNo() != null && row.getProductName() != null) map.putIfAbsent(row.getProductNo(), row.getProductName());
        }
        for (DiagnosisSourceAbcProductMetaRow row : metaRows) {
            if (row != null && row.getProductNo() != null && row.getProductName() != null) map.putIfAbsent(row.getProductNo(), row.getProductName());
        }
        return map;
    }

    private List<DiagnosisSourceAbcProductAggRow> safeAggRows(List<DiagnosisSourceAbcProductAggRow> rows) { return rows == null ? List.of() : rows; }
    private List<DiagnosisSourceAbcProductStockRow> safeStockRows(List<DiagnosisSourceAbcProductStockRow> rows) { return rows == null ? List.of() : rows; }
    private List<DiagnosisSourceAbcProductMetaRow> safeMetaRows(List<DiagnosisSourceAbcProductMetaRow> rows) { return rows == null ? List.of() : rows; }

    private BigDecimal metricOf(String abcType, BigDecimal sales, BigDecimal gross, BigDecimal qty) {
        if ("gross".equalsIgnoreCase(abcType)) return gross;
        if ("sales".equalsIgnoreCase(abcType)) return sales;
        return sales;
    }

    private BigDecimal percent(BigDecimal numerator, BigDecimal denominator) { return ratio(numerator, denominator).multiply(new BigDecimal("100")); }

    private BigDecimal ratio(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return nvl(numerator, BigDecimal.ZERO).divide(denominator, 6, RoundingMode.HALF_UP);
    }

    private BigDecimal nvl(BigDecimal value, BigDecimal defaultValue) { return value == null ? defaultValue : value; }
    private BigDecimal scale2(BigDecimal value) { return nvl(value, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP); }
    private BigDecimal scale4(BigDecimal value) { return nvl(value, BigDecimal.ZERO).setScale(4, RoundingMode.HALF_UP); }
    private BigDecimal scale6(BigDecimal value) { return nvl(value, BigDecimal.ZERO).setScale(6, RoundingMode.HALF_UP); }

    private record ProductMetric(BigDecimal sales, BigDecimal gross, BigDecimal saleQuantity, Integer storeNum, String promotionFlag, java.time.LocalDate firstSaleDate) { }

    private static class ProductCalc {
        private final String productNo;
        private String productName;
        private DiagnosisSourceAbcProductMetaRow meta;
        private BigDecimal currentSales = BigDecimal.ZERO;
        private BigDecimal currentGross = BigDecimal.ZERO;
        private BigDecimal currentQty = BigDecimal.ZERO;
        private BigDecimal compareSales = BigDecimal.ZERO;
        private BigDecimal compareGross = BigDecimal.ZERO;
        private BigDecimal compareQty = BigDecimal.ZERO;
        private BigDecimal currentMetric = BigDecimal.ZERO;
        private BigDecimal compareMetric = BigDecimal.ZERO;
        private BigDecimal stockQuantity = BigDecimal.ZERO;
        private Integer currentStoreNum = 0;
        private String promotionFlag = "0";
        private java.time.LocalDate firstSaleDate;
        private String currentAbc;
        private String compareAbc;
        private Integer currentRank = 0;
        private Integer compareRank = 0;
        private ProductCalc(String productNo) { this.productNo = productNo; }
    }

    private record AbcResult(List<DiagnosisAbcBucketRow> buckets, DiagnosisAbcMatrixRow matrix, List<DiagnosisAbcSkuRow> skus) { }
}

package org.dromara.diagnosis.application.batch.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.application.batch.model.DiagnosisFinalizeContext;
import org.dromara.diagnosis.application.batch.model.DiagnosisGmroiFinalizeResult;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisBatchSourceMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisGmroiContributionMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisGmroiSkuRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceAbcProductAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceAbcProductMetaRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceAbcProductStockRow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiagnosisGmroiContributionFinalizeService {

    private static final String TENANT_ID = "000000";
    private static final BigDecimal GROSS_THRESHOLD = new BigDecimal("17");

    private final DiagnosisBatchSourceMapper batchSourceMapper;
    private final DiagnosisGmroiContributionMapper gmroiContributionMapper;

    public DiagnosisGmroiFinalizeResult finalizeGmroi(DiagnosisFinalizeContext context) {
        gmroiContributionMapper.deleteByVersion(TENANT_ID, context.getQueryHash(), context.getDataVersion());

        List<DiagnosisSourceAbcProductAggRow> currentAgg = safeAggRows(batchSourceMapper.aggregateAbcProductMetrics(context.getParam()));
        List<DiagnosisSourceAbcProductAggRow> compareAgg = context.getCompareParam() == null
            ? List.of()
            : safeAggRows(batchSourceMapper.aggregateAbcProductMetrics(context.getCompareParam()));
        List<DiagnosisSourceAbcProductStockRow> currentStockRows = safeStockRows(batchSourceMapper.aggregateAbcProductStock(context.getParam(), context.getPeriodEnd()));
        List<DiagnosisSourceAbcProductStockRow> compareStockRows = context.getCompareParam() == null || context.getCompareEnd() == null
            ? List.of()
            : safeStockRows(batchSourceMapper.aggregateAbcProductStock(context.getCompareParam(), context.getCompareEnd()));
        List<DiagnosisSourceAbcProductMetaRow> metaRows = safeMetaRows(batchSourceMapper.selectAbcProductMeta(context.getParam()));

        Map<String, DiagnosisSourceAbcProductAggRow> currentMap = toAggMap(currentAgg);
        Map<String, DiagnosisSourceAbcProductAggRow> compareMap = toAggMap(compareAgg);
        Map<String, DiagnosisSourceAbcProductStockRow> currentStockMap = toStockMap(currentStockRows);
        Map<String, DiagnosisSourceAbcProductStockRow> compareStockMap = toStockMap(compareStockRows);
        Map<String, DiagnosisSourceAbcProductMetaRow> metaMap = toMetaMap(metaRows);
        Map<String, String> productNameMap = buildProductNameMap(currentAgg, compareAgg, metaRows);

        Map<String, CalcItem> union = new LinkedHashMap<>();
        for (String productNo : currentMap.keySet()) {
            union.put(productNo, new CalcItem(productNo));
        }
        for (String productNo : compareMap.keySet()) {
            union.putIfAbsent(productNo, new CalcItem(productNo));
        }

        BigDecimal totalSales = BigDecimal.ZERO;
        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal currentTurnoverSum = BigDecimal.ZERO;
        int currentTurnoverCount = 0;
        BigDecimal compareTurnoverSum = BigDecimal.ZERO;
        int compareTurnoverCount = 0;
        BigDecimal currentGrowthSum = BigDecimal.ZERO;
        int currentGrowthCount = 0;
        BigDecimal compareGrowthSum = BigDecimal.ZERO;
        int compareGrowthCount = 0;

        for (CalcItem item : union.values()) {
            DiagnosisSourceAbcProductAggRow current = currentMap.get(item.productNo);
            DiagnosisSourceAbcProductAggRow compare = compareMap.get(item.productNo);
            DiagnosisSourceAbcProductStockRow currentStock = currentStockMap.get(item.productNo);
            DiagnosisSourceAbcProductStockRow compareStock = compareStockMap.get(item.productNo);

            item.productName = productNameMap.getOrDefault(item.productNo, item.productNo);
            item.meta = metaMap.get(item.productNo);

            item.currentSales = nvl(current == null ? null : current.getSales());
            item.currentGross = nvl(current == null ? null : current.getGross());
            item.currentQty = nvl(current == null ? null : current.getSaleQuantity());
            item.currentStoreNum = current == null || current.getStoreNum() == null ? 0 : current.getStoreNum();
            item.promotionFlag = current == null || current.getPromotionFlag() == null ? "0" : current.getPromotionFlag();
            item.firstSaleDate = current == null ? null : current.getFirstSaleDate();

            item.compareSales = nvl(compare == null ? null : compare.getSales());
            item.compareGross = nvl(compare == null ? null : compare.getGross());
            item.compareQty = nvl(compare == null ? null : compare.getSaleQuantity());

            item.currentStockQuantity = nvl(currentStock == null ? null : currentStock.getStockQuantity());
            item.currentStockSaleMoney = nvl(currentStock == null ? null : currentStock.getStockSaleMoney());
            item.compareStockSaleMoney = nvl(compareStock == null ? null : compareStock.getStockSaleMoney());

            item.currentGrossRate = percent(item.currentGross, item.currentSales);
            item.compareGrossRate = percent(item.compareGross, item.compareSales);
            item.currentTurnoverRate = ratio(item.currentSales, item.currentStockSaleMoney);
            item.compareTurnoverRate = ratio(item.compareSales, item.compareStockSaleMoney);
            item.currentGmroi = ratio(item.currentGross, item.currentStockSaleMoney);
            item.compareGmroi = ratio(item.compareGross, item.compareStockSaleMoney);
            item.currentGrowthRate = growthRate(item.currentSales, item.compareSales);
            item.compareGrowthRate = growthRate(item.compareSales, item.currentSales);

            if (item.currentTurnoverRate.compareTo(BigDecimal.ZERO) != 0 || item.currentSales.compareTo(BigDecimal.ZERO) != 0) {
                currentTurnoverSum = currentTurnoverSum.add(item.currentTurnoverRate);
                currentTurnoverCount++;
            }
            if (item.compareTurnoverRate.compareTo(BigDecimal.ZERO) != 0 || item.compareSales.compareTo(BigDecimal.ZERO) != 0) {
                compareTurnoverSum = compareTurnoverSum.add(item.compareTurnoverRate);
                compareTurnoverCount++;
            }

            currentGrowthSum = currentGrowthSum.add(item.currentGrowthRate);
            currentGrowthCount++;
            compareGrowthSum = compareGrowthSum.add(item.compareGrowthRate);
            compareGrowthCount++;

            totalSales = totalSales.add(item.currentSales);
            totalGross = totalGross.add(item.currentGross);
            totalQty = totalQty.add(item.currentQty);
        }

        BigDecimal currentAvgTurnover = currentTurnoverCount == 0
            ? BigDecimal.ZERO
            : currentTurnoverSum.divide(BigDecimal.valueOf(currentTurnoverCount), 6, RoundingMode.HALF_UP);
        BigDecimal compareAvgTurnover = compareTurnoverCount == 0
            ? BigDecimal.ZERO
            : compareTurnoverSum.divide(BigDecimal.valueOf(compareTurnoverCount), 6, RoundingMode.HALF_UP);
        BigDecimal currentAvgGrowth = currentGrowthCount == 0
            ? BigDecimal.ZERO
            : currentGrowthSum.divide(BigDecimal.valueOf(currentGrowthCount), 6, RoundingMode.HALF_UP);
        BigDecimal compareAvgGrowth = compareGrowthCount == 0
            ? BigDecimal.ZERO
            : compareGrowthSum.divide(BigDecimal.valueOf(compareGrowthCount), 6, RoundingMode.HALF_UP);

        List<DiagnosisGmroiSkuRow> rows = new ArrayList<>(union.size());
        for (CalcItem item : union.values()) {
            item.currentRole = classifyGmroi(item.currentTurnoverRate, item.currentGrossRate, currentAvgTurnover);
            item.compareRole = classifyGmroi(item.compareTurnoverRate, item.compareGrossRate, compareAvgTurnover);
            item.currentGrossRole = classifyGross(item.currentGrossRate, item.currentGrowthRate, currentAvgGrowth);
            item.compareGrossRole = classifyGross(item.compareGrossRate, item.compareGrowthRate, compareAvgGrowth);
            rows.add(toRow(context, item, totalSales, totalGross, totalQty));
        }

        if (!rows.isEmpty()) {
            gmroiContributionMapper.batchInsertSku(rows);
        }
        return DiagnosisGmroiFinalizeResult.builder()
            .skuRows(rows.size())
            .build();
    }

    private DiagnosisGmroiSkuRow toRow(DiagnosisFinalizeContext context,
                                       CalcItem item,
                                       BigDecimal totalSales,
                                       BigDecimal totalGross,
                                       BigDecimal totalQty) {
        DiagnosisGmroiSkuRow row = new DiagnosisGmroiSkuRow();
        row.setTenantId(TENANT_ID);
        row.setQueryHash(context.getQueryHash());
        row.setDataVersion(context.getDataVersion());
        row.setProductNo(item.productNo);
        row.setProductName(item.productName);
        row.setStoreNum(item.currentStoreNum);
        row.setCurrentGrossRole(item.currentGrossRole);
        row.setCompareGrossRole(item.compareGrossRole);
        row.setCurrentGmroiRole(item.currentRole);
        row.setCompareGmroiRole(item.compareRole);
        row.setSaleQuantity(scale2(item.currentQty));
        row.setSaleQuantityPsd(scale4(perStorePerDay(item.currentQty, item.currentStoreNum, context.getPeriodDays())));
        row.setSales(scale2(item.currentSales));
        row.setSalesPer(scale6(ratio(item.currentSales, totalSales)));
        row.setSalesPsd(scale4(perStorePerDay(item.currentSales, item.currentStoreNum, context.getPeriodDays())));
        row.setGross(scale2(item.currentGross));
        row.setGrossPer(scale6(ratio(item.currentGross, totalGross)));
        row.setGrossPsd(scale4(perStorePerDay(item.currentGross, item.currentStoreNum, context.getPeriodDays())));
        row.setGrossRate(scale2(item.currentGrossRate));
        row.setStockQuantity(scale2(item.currentStockQuantity));
        row.setStockSalesRate(scale4(ratio(item.currentStockQuantity, item.currentQty)));
        row.setTurnoverRate(scale4(item.currentTurnoverRate));
        row.setTurnoverDays(scale4(item.currentTurnoverRate.compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(Math.max(1L, context.getPeriodDays())).divide(item.currentTurnoverRate, 6, RoundingMode.HALF_UP)));
        row.setContributionRate(scale6(ratio(item.currentGross, totalGross)));
        row.setGmroi(scale4(item.currentGmroi));
        row.setSalesRate(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        row.setActivity("1".equals(item.promotionFlag) ? "是" : "否");
        row.setFirstSaleDate(item.firstSaleDate);
        row.setNewProduct(item.firstSaleDate != null && !item.firstSaleDate.isBefore(context.getPeriodStart()) ? "是" : "否");
        row.setKeyProduct("否");
        row.setPromotionFlag(item.promotionFlag);
        row.setCompareSales(scale2(item.compareSales));
        row.setCompareGross(scale2(item.compareGross));
        row.setCompareTurnoverRate(scale4(item.compareTurnoverRate));
        row.setCompareGrossRate(scale2(item.compareGrossRate));
        row.setCompareGmroi(scale4(item.compareGmroi));

        if (item.meta != null) {
            row.setProductStatus(item.meta.getProductStatus());
            row.setProductStatusNo(item.meta.getProductStatusNo());
            row.setSeasonableFlag(item.meta.getSeasonableFlag());
            row.setSeasonableFlagName(item.meta.getSeasonableFlagName());
            row.setSeasonableStartDate(item.meta.getSeasonableStartDate());
            row.setSeasonableEndDate(item.meta.getSeasonableEndDate());
            row.setClassNo(item.meta.getClassNo());
            row.setClassName(item.meta.getClassName());
            row.setClassLevel(item.meta.getClassLevel());
            row.setProductBarcode(item.meta.getProductBarcode());
            row.setBrandName(item.meta.getBrandName());
            row.setSpec(item.meta.getSpec());
            row.setInPrice(scale2(item.meta.getInPrice()));
            row.setSalesPrice(scale2(item.meta.getSalesPrice()));
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

    private String classifyGmroi(BigDecimal turnoverRate, BigDecimal grossRate, BigDecimal avgTurnover) {
        boolean highTurnover = turnoverRate.compareTo(avgTurnover) >= 0;
        boolean highGross = grossRate.compareTo(GROSS_THRESHOLD) >= 0;
        if (highTurnover && highGross) {
            return "1";
        }
        if (!highTurnover && highGross) {
            return "2";
        }
        if (!highTurnover) {
            return "3";
        }
        return "4";
    }

    private String classifyGross(BigDecimal grossRate, BigDecimal growthRate, BigDecimal avgGrowth) {
        if (grossRate.compareTo(BigDecimal.ZERO) < 0 || growthRate.compareTo(avgGrowth) < 0) {
            return "3";
        }
        if (grossRate.compareTo(GROSS_THRESHOLD) >= 0 && growthRate.compareTo(avgGrowth) >= 0) {
            return "1";
        }
        if (grossRate.compareTo(BigDecimal.ZERO) >= 0 && grossRate.compareTo(GROSS_THRESHOLD) < 0
            && growthRate.compareTo(avgGrowth) >= 0) {
            return "2";
        }
        if (grossRate.compareTo(GROSS_THRESHOLD) >= 0 && growthRate.compareTo(avgGrowth) < 0) {
            return "4";
        }
        return "3";
    }

    private BigDecimal growthRate(BigDecimal currentValue, BigDecimal compareValue) {
        if (compareValue == null || compareValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return nvl(currentValue).subtract(compareValue).multiply(new BigDecimal("100"))
            .divide(compareValue, 6, RoundingMode.HALF_UP);
    }

    private BigDecimal perStorePerDay(BigDecimal value, Integer storeNum, long periodDays) {
        if (storeNum == null || storeNum <= 0 || periodDays <= 0) {
            return BigDecimal.ZERO;
        }
        return nvl(value).divide(BigDecimal.valueOf((long) storeNum * periodDays), 6, RoundingMode.HALF_UP);
    }

    private Map<String, DiagnosisSourceAbcProductAggRow> toAggMap(List<DiagnosisSourceAbcProductAggRow> rows) {
        Map<String, DiagnosisSourceAbcProductAggRow> map = new LinkedHashMap<>();
        for (DiagnosisSourceAbcProductAggRow row : rows) {
            if (row == null || row.getProductNo() == null) {
                continue;
            }
            map.put(row.getProductNo(), row);
        }
        return map;
    }

    private Map<String, DiagnosisSourceAbcProductStockRow> toStockMap(List<DiagnosisSourceAbcProductStockRow> rows) {
        Map<String, DiagnosisSourceAbcProductStockRow> map = new LinkedHashMap<>();
        for (DiagnosisSourceAbcProductStockRow row : rows) {
            if (row == null || row.getProductNo() == null) {
                continue;
            }
            map.put(row.getProductNo(), row);
        }
        return map;
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

    private Map<String, String> buildProductNameMap(List<DiagnosisSourceAbcProductAggRow> current,
                                                    List<DiagnosisSourceAbcProductAggRow> compare,
                                                    List<DiagnosisSourceAbcProductMetaRow> metaRows) {
        Map<String, String> map = new LinkedHashMap<>();
        for (DiagnosisSourceAbcProductAggRow row : current) {
            if (row != null && row.getProductNo() != null && row.getProductName() != null) {
                map.putIfAbsent(row.getProductNo(), row.getProductName());
            }
        }
        for (DiagnosisSourceAbcProductAggRow row : compare) {
            if (row != null && row.getProductNo() != null && row.getProductName() != null) {
                map.putIfAbsent(row.getProductNo(), row.getProductName());
            }
        }
        for (DiagnosisSourceAbcProductMetaRow row : metaRows) {
            if (row != null && row.getProductNo() != null && row.getProductName() != null) {
                map.putIfAbsent(row.getProductNo(), row.getProductName());
            }
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

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal ratio(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return nvl(numerator).divide(denominator, 6, RoundingMode.HALF_UP);
    }

    private BigDecimal percent(BigDecimal numerator, BigDecimal denominator) {
        return ratio(numerator, denominator).multiply(new BigDecimal("100"));
    }

    private BigDecimal scale2(BigDecimal value) {
        return nvl(value).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scale4(BigDecimal value) {
        return nvl(value).setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal scale6(BigDecimal value) {
        return nvl(value).setScale(6, RoundingMode.HALF_UP);
    }

    private static class CalcItem {
        private final String productNo;
        private String productName;
        private DiagnosisSourceAbcProductMetaRow meta;
        private BigDecimal currentSales = BigDecimal.ZERO;
        private BigDecimal currentGross = BigDecimal.ZERO;
        private BigDecimal currentQty = BigDecimal.ZERO;
        private BigDecimal compareSales = BigDecimal.ZERO;
        private BigDecimal compareGross = BigDecimal.ZERO;
        private BigDecimal compareQty = BigDecimal.ZERO;
        private BigDecimal currentStockQuantity = BigDecimal.ZERO;
        private BigDecimal currentStockSaleMoney = BigDecimal.ZERO;
        private BigDecimal compareStockSaleMoney = BigDecimal.ZERO;
        private BigDecimal currentGrossRate = BigDecimal.ZERO;
        private BigDecimal compareGrossRate = BigDecimal.ZERO;
        private BigDecimal currentTurnoverRate = BigDecimal.ZERO;
        private BigDecimal compareTurnoverRate = BigDecimal.ZERO;
        private BigDecimal currentGmroi = BigDecimal.ZERO;
        private BigDecimal compareGmroi = BigDecimal.ZERO;
        private BigDecimal currentGrowthRate = BigDecimal.ZERO;
        private BigDecimal compareGrowthRate = BigDecimal.ZERO;
        private Integer currentStoreNum = 0;
        private String promotionFlag = "0";
        private java.time.LocalDate firstSaleDate;
        private String currentRole = "3";
        private String compareRole = "3";
        private String currentGrossRole = "0";
        private String compareGrossRole = "0";

        private CalcItem(String productNo) {
            this.productNo = productNo;
        }
    }
}

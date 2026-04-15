package org.dromara.diagnosis.application.batch.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.application.batch.model.DiagnosisFinalizeContext;
import org.dromara.diagnosis.application.batch.model.DiagnosisGrossFinalizeResult;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisBatchSourceMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisGrossContributionMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisGrossSkuRow;
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
public class DiagnosisGrossContributionFinalizeService {

    private static final String TENANT_ID = "000000";
    private static final BigDecimal GROSS_THRESHOLD = new BigDecimal("17");

    private final DiagnosisBatchSourceMapper batchSourceMapper;
    private final DiagnosisGrossContributionMapper grossContributionMapper;

    public DiagnosisGrossFinalizeResult finalizeGrossContribution(DiagnosisFinalizeContext context) {
        grossContributionMapper.deleteByVersion(TENANT_ID, context.getQueryHash(), context.getDataVersion());

        List<DiagnosisSourceAbcProductAggRow> currentAgg = safeAggRows(batchSourceMapper.aggregateAbcProductMetrics(context.getParam()));
        List<DiagnosisSourceAbcProductAggRow> compareAgg = context.getCompareParam() == null
            ? List.of()
            : safeAggRows(batchSourceMapper.aggregateAbcProductMetrics(context.getCompareParam()));
        List<DiagnosisSourceAbcProductStockRow> stockRows = safeStockRows(batchSourceMapper.aggregateAbcProductStock(context.getParam(), context.getPeriodEnd()));
        List<DiagnosisSourceAbcProductMetaRow> metaRows = safeMetaRows(batchSourceMapper.selectAbcProductMeta(context.getParam()));

        Map<String, DiagnosisSourceAbcProductAggRow> currentMap = toAggMap(currentAgg);
        Map<String, DiagnosisSourceAbcProductAggRow> compareMap = toAggMap(compareAgg);
        Map<String, BigDecimal> stockMap = toStockMap(stockRows);
        Map<String, DiagnosisSourceAbcProductMetaRow> metaMap = toMetaMap(metaRows);
        Map<String, String> nameMap = buildProductNameMap(currentAgg, compareAgg, metaRows);

        Map<String, GrossCalc> union = new LinkedHashMap<>();
        for (String productNo : currentMap.keySet()) {
            union.put(productNo, new GrossCalc(productNo));
        }
        for (String productNo : compareMap.keySet()) {
            union.putIfAbsent(productNo, new GrossCalc(productNo));
        }

        BigDecimal totalCurrentSales = BigDecimal.ZERO;
        BigDecimal totalCurrentGross = BigDecimal.ZERO;
        BigDecimal totalCurrentQty = BigDecimal.ZERO;
        BigDecimal growthSum = BigDecimal.ZERO;
        int growthCount = 0;

        for (GrossCalc calc : union.values()) {
            DiagnosisSourceAbcProductAggRow current = currentMap.get(calc.productNo);
            DiagnosisSourceAbcProductAggRow compare = compareMap.get(calc.productNo);
            DiagnosisSourceAbcProductMetaRow meta = metaMap.get(calc.productNo);

            calc.productName = nameMap.getOrDefault(calc.productNo, calc.productNo);
            calc.currentSales = nvl(current == null ? null : current.getSales());
            calc.currentGross = nvl(current == null ? null : current.getGross());
            calc.currentQty = nvl(current == null ? null : current.getSaleQuantity());
            calc.currentStoreNum = current == null || current.getStoreNum() == null ? 0 : current.getStoreNum();
            calc.promotionFlag = current == null || current.getPromotionFlag() == null ? "0" : current.getPromotionFlag();
            calc.firstSaleDate = current == null ? null : current.getFirstSaleDate();

            calc.compareSales = nvl(compare == null ? null : compare.getSales());
            calc.compareGross = nvl(compare == null ? null : compare.getGross());
            calc.compareQty = nvl(compare == null ? null : compare.getSaleQuantity());

            calc.currentGrossRate = percent(calc.currentGross, calc.currentSales);
            calc.compareGrossRate = percent(calc.compareGross, calc.compareSales);
            calc.currentGrowthRate = growthRate(calc.currentSales, calc.compareSales);
            calc.compareGrowthRate = growthRate(calc.compareSales, calc.currentSales);

            calc.stockQuantity = stockMap.getOrDefault(calc.productNo, BigDecimal.ZERO);
            calc.meta = meta;

            totalCurrentSales = totalCurrentSales.add(calc.currentSales);
            totalCurrentGross = totalCurrentGross.add(calc.currentGross);
            totalCurrentQty = totalCurrentQty.add(calc.currentQty);
            growthSum = growthSum.add(calc.currentGrowthRate);
            growthCount++;
        }

        BigDecimal avgGrowthRate = growthCount == 0
            ? BigDecimal.ZERO
            : growthSum.divide(BigDecimal.valueOf(growthCount), 6, RoundingMode.HALF_UP);

        List<DiagnosisGrossSkuRow> rows = new ArrayList<>(union.size());
        for (GrossCalc calc : union.values()) {
            calc.currentRole = classify(calc.currentGrossRate, calc.currentGrowthRate, avgGrowthRate);
            calc.compareRole = classify(calc.compareGrossRate, calc.compareGrowthRate, BigDecimal.ZERO);
            rows.add(toRow(context, calc, totalCurrentSales, totalCurrentGross, totalCurrentQty));
        }

        if (!rows.isEmpty()) {
            grossContributionMapper.batchInsertSku(rows);
        }
        return DiagnosisGrossFinalizeResult.builder()
            .skuRows(rows.size())
            .build();
    }

    private DiagnosisGrossSkuRow toRow(DiagnosisFinalizeContext context,
                                       GrossCalc calc,
                                       BigDecimal totalCurrentSales,
                                       BigDecimal totalCurrentGross,
                                       BigDecimal totalCurrentQty) {
        DiagnosisGrossSkuRow row = new DiagnosisGrossSkuRow();
        row.setTenantId(TENANT_ID);
        row.setQueryHash(context.getQueryHash());
        row.setDataVersion(context.getDataVersion());
        row.setProductNo(calc.productNo);
        row.setProductName(calc.productName);
        row.setStoreNum(calc.currentStoreNum);
        row.setCurrentGrossRole(calc.currentRole);
        row.setCompareGrossRole(calc.compareRole);
        row.setCurrentGmroiRole(calc.currentRole);
        row.setCompareGmroiRole(calc.compareRole);
        row.setSaleQuantity(scale2(calc.currentQty));
        row.setSaleQuantityPsd(scale4(perStorePerDay(calc.currentQty, calc.currentStoreNum, context.getPeriodDays())));
        row.setSales(scale2(calc.currentSales));
        row.setSalesPer(scale2(percent(calc.currentSales, totalCurrentSales)));
        row.setSalesPsd(scale4(perStorePerDay(calc.currentSales, calc.currentStoreNum, context.getPeriodDays())));
        row.setGross(scale2(calc.currentGross));
        row.setGrossPer(scale2(percent(calc.currentGross, totalCurrentGross)));
        row.setGrossPsd(scale4(perStorePerDay(calc.currentGross, calc.currentStoreNum, context.getPeriodDays())));
        row.setGrossRate(scale2(calc.currentGrossRate));
        row.setStockQuantity(scale2(calc.stockQuantity));
        BigDecimal turnoverRate = ratio(calc.currentSales, calc.stockQuantity);
        row.setTurnoverRate(scale4(turnoverRate));
        row.setTurnoverDays(scale4(turnoverRate.compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(Math.max(1L, context.getPeriodDays())).divide(turnoverRate, 6, RoundingMode.HALF_UP)));
        row.setStockSalesRate(scale2(percent(calc.stockQuantity, calc.currentQty)));
        row.setContributionRate(scale2(percent(calc.currentGross, totalCurrentGross)));
        row.setGmroi(scale4(ratio(calc.currentGross, calc.stockQuantity)));
        row.setSalesRate(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        row.setActivity("1".equals(calc.promotionFlag) ? "是" : "否");
        row.setFirstSaleDate(calc.firstSaleDate);
        row.setNewProduct(calc.firstSaleDate != null && !calc.firstSaleDate.isBefore(context.getPeriodStart()) ? "是" : "否");
        row.setKeyProduct("否");
        row.setPromotionFlag(calc.promotionFlag);
        row.setCompareSales(scale2(calc.compareSales));
        row.setCompareGross(scale2(calc.compareGross));
        row.setCurrentGrowthRate(scale2(calc.currentGrowthRate));
        row.setCompareGrowthRate(scale2(calc.compareGrowthRate));

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

    private String classify(BigDecimal grossRate, BigDecimal growthRate, BigDecimal avgGrowth) {
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
        return currentValue.subtract(compareValue).multiply(new BigDecimal("100"))
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

    private static class GrossCalc {
        private final String productNo;
        private String productName;
        private DiagnosisSourceAbcProductMetaRow meta;
        private BigDecimal currentSales = BigDecimal.ZERO;
        private BigDecimal currentGross = BigDecimal.ZERO;
        private BigDecimal currentQty = BigDecimal.ZERO;
        private BigDecimal compareSales = BigDecimal.ZERO;
        private BigDecimal compareGross = BigDecimal.ZERO;
        private BigDecimal compareQty = BigDecimal.ZERO;
        private BigDecimal currentGrowthRate = BigDecimal.ZERO;
        private BigDecimal compareGrowthRate = BigDecimal.ZERO;
        private BigDecimal currentGrossRate = BigDecimal.ZERO;
        private BigDecimal compareGrossRate = BigDecimal.ZERO;
        private BigDecimal stockQuantity = BigDecimal.ZERO;
        private Integer currentStoreNum = 0;
        private String promotionFlag = "0";
        private java.time.LocalDate firstSaleDate;
        private String currentRole = "3";
        private String compareRole = "3";

        private GrossCalc(String productNo) {
            this.productNo = productNo;
        }
    }
}


package org.dromara.diagnosis.application.batch.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.application.batch.model.DiagnosisCategorySalesListFinalizeResult;
import org.dromara.diagnosis.application.batch.model.DiagnosisFinalizeContext;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisBatchSourceMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisCategorySalesListMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisCategorySalesSkuRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceAbcProductAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceAbcProductMetaRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceAbcProductStockRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceProductVendorMetaRow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiagnosisCategorySalesListFinalizeService {

    private static final String TENANT_ID = "000000";
    private static final int INSERT_BATCH_SIZE = 40;

    private final DiagnosisBatchSourceMapper batchSourceMapper;
    private final DiagnosisCategorySalesListMapper categorySalesListMapper;
    private final DiagnosisFinalizeSupport finalizeSupport;

    public DiagnosisCategorySalesListFinalizeResult finalizeCategorySalesList(DiagnosisFinalizeContext context) {
        List<DiagnosisSourceAbcProductAggRow> metricRows = safeList(batchSourceMapper.aggregateAbcProductMetrics(context.getParam()));
        List<DiagnosisSourceAbcProductStockRow> stockRows =
            safeList(batchSourceMapper.aggregateAbcProductStock(context.getParam(), context.getPeriodEnd()));
        List<DiagnosisSourceAbcProductMetaRow> metaRows = safeList(batchSourceMapper.selectAbcProductMeta(context.getParam()));
        List<DiagnosisSourceProductVendorMetaRow> vendorRows = safeList(batchSourceMapper.selectProductVendorMeta(context.getParam()));

        Map<String, DiagnosisSourceAbcProductStockRow> stockMap = new LinkedHashMap<>();
        for (DiagnosisSourceAbcProductStockRow row : stockRows) {
            stockMap.put(row.getProductNo(), row);
        }
        Map<String, DiagnosisSourceAbcProductMetaRow> metaMap = new LinkedHashMap<>();
        for (DiagnosisSourceAbcProductMetaRow row : metaRows) {
            metaMap.put(row.getProductNo(), row);
        }
        Map<String, DiagnosisSourceProductVendorMetaRow> vendorMap = new LinkedHashMap<>();
        for (DiagnosisSourceProductVendorMetaRow row : vendorRows) {
            vendorMap.put(row.getProductNo(), row);
        }
        Map<String, DiagnosisSourceAbcProductAggRow> metricMap = new LinkedHashMap<>();
        for (DiagnosisSourceAbcProductAggRow row : metricRows) {
            metricMap.put(row.getProductNo(), row);
        }

        BigDecimal totalSales = BigDecimal.ZERO;
        BigDecimal totalGross = BigDecimal.ZERO;
        for (DiagnosisSourceAbcProductAggRow row : metricRows) {
            totalSales = totalSales.add(nvl(row.getSales()));
            totalGross = totalGross.add(nvl(row.getGross()));
        }

        List<DiagnosisCategorySalesSkuRow> resultRows = new ArrayList<>(metaRows.size());
        for (DiagnosisSourceAbcProductMetaRow metaRow : metaRows) {
            DiagnosisSourceAbcProductAggRow metricRow = metricMap.get(metaRow.getProductNo());
            resultRows.add(toSkuRow(
                context,
                metricRow == null ? emptyMetricRow(metaRow) : metricRow,
                stockMap.get(metaRow.getProductNo()),
                metaRow,
                vendorMap.get(metaRow.getProductNo()),
                totalSales,
                totalGross
            ));
        }

        categorySalesListMapper.deleteByVersion(TENANT_ID, context.getQueryHash(), context.getDataVersion());
        if (!resultRows.isEmpty()) {
            batchInsert(resultRows);
        }
        return DiagnosisCategorySalesListFinalizeResult.builder()
            .skuRows(resultRows.size())
            .build();
    }

    private void batchInsert(List<DiagnosisCategorySalesSkuRow> rows) {
        for (int start = 0; start < rows.size(); start += INSERT_BATCH_SIZE) {
            int end = Math.min(start + INSERT_BATCH_SIZE, rows.size());
            categorySalesListMapper.batchInsert(rows.subList(start, end));
        }
    }

    private DiagnosisCategorySalesSkuRow toSkuRow(DiagnosisFinalizeContext context,
                                                  DiagnosisSourceAbcProductAggRow metricRow,
                                                  DiagnosisSourceAbcProductStockRow stockRow,
                                                  DiagnosisSourceAbcProductMetaRow metaRow,
                                                  DiagnosisSourceProductVendorMetaRow vendorRow,
                                                  BigDecimal totalSales,
                                                  BigDecimal totalGross) {
        BigDecimal sales = nvl(metricRow.getSales());
        BigDecimal gross = nvl(metricRow.getGross());
        BigDecimal saleQuantity = nvl(metricRow.getSaleQuantity());
        BigDecimal stockQuantity = stockRow == null ? BigDecimal.ZERO : nvl(stockRow.getStockQuantity());
        int storeNum = metricRow.getStoreNum() == null ? 0 : metricRow.getStoreNum();

        DiagnosisCategorySalesSkuRow row = new DiagnosisCategorySalesSkuRow();
        row.setTenantId(TENANT_ID);
        row.setQueryHash(context.getQueryHash());
        row.setDataVersion(context.getDataVersion());
        row.setPromotionFlag("1".equals(metricRow.getPromotionFlag()) ? "1" : "2");
        row.setProductNo(metricRow.getProductNo());
        row.setProductName(metricRow.getProductName());
        row.setStoreNum(storeNum);
        row.setSaleQuantity(scale2(saleQuantity));
        row.setSaleQuantityPsd(scale4(perStoreDayValue(saleQuantity, storeNum, context.getPeriodDays())));
        row.setSales(scale2(sales));
        row.setSalesPer(scale4(ratio(sales, totalSales)));
        row.setSalesPsd(scale4(perStoreDayValue(sales, storeNum, context.getPeriodDays())));
        row.setGross(scale2(gross));
        row.setGrossPer(scale4(ratio(gross, totalGross)));
        row.setGrossPsd(scale4(perStoreDayValue(gross, storeNum, context.getPeriodDays())));
        row.setGrossRate(scale4(ratio(gross, sales)));
        row.setStockQuantity(scale2(stockQuantity));

        BigDecimal turnoverRate = ratio(sales, stockQuantity);
        row.setTurnoverRate(scale4(turnoverRate));
        row.setTurnoverDays(scale4(turnoverRate.compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(Math.max(1L, context.getPeriodDays())).divide(turnoverRate, 6, RoundingMode.HALF_UP)));
        row.setStockSalesRate(scale4(ratio(stockQuantity, saleQuantity)));
        row.setContributionRate(scale4(ratio(gross, totalGross)));
        row.setGmroi(scale4(ratio(gross, stockQuantity)));
        row.setSalesRate(null);
        row.setActivity("1".equals(metricRow.getPromotionFlag()) ? "\u662f" : "\u5426");
        row.setFirstSaleDate(metricRow.getFirstSaleDate());
        row.setNewProduct(metricRow.getFirstSaleDate() != null && !metricRow.getFirstSaleDate().isBefore(context.getPeriodStart())
            ? "\u662f"
            : "\u5426");
        row.setKeyProduct("\u5426");
        row.setSnapshotTime(LocalDateTime.now());

        if (metaRow != null) {
            row.setProductStatus(metaRow.getProductStatus());
            row.setProductStatusNo(metaRow.getProductStatusNo());
            row.setSeasonableFlag(metaRow.getSeasonableFlag());
            row.setSeasonableFlagName(metaRow.getSeasonableFlagName());
            row.setSeasonableStartDate(metaRow.getSeasonableStartDate());
            row.setSeasonableEndDate(metaRow.getSeasonableEndDate());
            row.setClassNo(metaRow.getClassNo());
            row.setClassName(metaRow.getClassName());
            row.setProductBarcode(metaRow.getProductBarcode());
            row.setBrandName(metaRow.getBrandName());
            row.setSpec(metaRow.getSpec());
            row.setInPrice(scale2(metaRow.getInPrice()));
            row.setSalesPrice(scale2(metaRow.getSalesPrice()));
        } else {
            row.setProductStatus("\u672a\u77e5");
            row.setProductStatusNo("0");
            row.setSeasonableFlag("0");
            row.setSeasonableFlagName("\u5426");
            row.setInPrice(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            row.setSalesPrice(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }

        if (vendorRow != null) {
            row.setProductVendorNo(vendorRow.getProductVendorNo());
            row.setProductVendorName(vendorRow.getProductVendorName());
            row.setProductVendorNoName(vendorRow.getProductVendorNoName());
        }
        return row;
    }

    private DiagnosisSourceAbcProductAggRow emptyMetricRow(DiagnosisSourceAbcProductMetaRow metaRow) {
        DiagnosisSourceAbcProductAggRow row = new DiagnosisSourceAbcProductAggRow();
        row.setProductNo(metaRow.getProductNo());
        row.setProductBarcode(metaRow.getProductBarcode());
        row.setProductName(metaRow.getProductName());
        row.setStoreNum(0);
        row.setSaleQuantity(BigDecimal.ZERO);
        row.setSales(BigDecimal.ZERO);
        row.setGross(BigDecimal.ZERO);
        row.setSaleCost(BigDecimal.ZERO);
        row.setPromotionFlag("2");
        row.setFirstSaleDate(metaRow.getFirstSaleDate());
        return row;
    }

    private <T> List<T> safeList(List<T> rows) {
        return rows == null ? List.of() : rows;
    }

    private BigDecimal perStoreDayValue(BigDecimal value, int storeNum, long periodDays) {
        if (storeNum <= 0 || periodDays <= 0) {
            return BigDecimal.ZERO;
        }
        return value.divide(BigDecimal.valueOf((long) storeNum * periodDays), 6, RoundingMode.HALF_UP);
    }

    private BigDecimal ratio(BigDecimal numerator, BigDecimal denominator) {
        BigDecimal result = finalizeSupport.safeDivide(numerator, denominator);
        return result == null ? BigDecimal.ZERO : result;
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
}

package org.dromara.diagnosis.application.batch.service;

import lombok.RequiredArgsConstructor;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.diagnosis.application.batch.model.DiagnosisFinalizeContext;
import org.dromara.diagnosis.application.batch.model.DiagnosisVendorFinalizeResult;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisBatchSourceMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisVendorMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceVendorAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisVendorJsonRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisVendorMetricRow;
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
public class DiagnosisVendorFinalizeService {

    private static final String TENANT_ID = "000000";
    private static final int SUMMARY_LIMIT = 10;
    private static final int INSERT_BATCH_SIZE = 30;
    private static final BigDecimal LOW_SALES_SHARE_THRESHOLD = new BigDecimal("1.0000");
    private static final BigDecimal HIGH_DIFF_RATE_THRESHOLD = new BigDecimal("5.0000");

    private final DiagnosisBatchSourceMapper batchSourceMapper;
    private final DiagnosisVendorMapper vendorMapper;

    @Transactional(rollbackFor = Exception.class)
    public DiagnosisVendorFinalizeResult finalizeVendor(DiagnosisFinalizeContext context) {
        List<DiagnosisSourceVendorAggRow> sourceRows = safeList(batchSourceMapper.aggregateVendorMetrics(context.getParam()));
        vendorMapper.deleteByVersion(TENANT_ID, context.getQueryHash(), context.getDataVersion());

        if (sourceRows.isEmpty()) {
            List<DiagnosisVendorJsonRow> emptyJsonRows = buildJsonRows(List.of(), context, LocalDateTime.now());
            if (!emptyJsonRows.isEmpty()) {
                vendorMapper.batchInsertJson(emptyJsonRows);
            }
            return DiagnosisVendorFinalizeResult.builder()
                .metricRows(0L)
                .jsonRows(emptyJsonRows.size())
                .build();
        }

        BigDecimal totalSales = BigDecimal.ZERO;
        for (DiagnosisSourceVendorAggRow row : sourceRows) {
            totalSales = totalSales.add(nvl(row.getSales()));
        }

        LocalDateTime snapshotTime = LocalDateTime.now();
        List<VendorMetricCalc> calcs = new ArrayList<>(sourceRows.size());
        for (DiagnosisSourceVendorAggRow row : sourceRows) {
            VendorMetricCalc calc = new VendorMetricCalc();
            calc.productVendorNo = row.getVendorNo();
            calc.productVendorName = blankToDefault(row.getVendorName(), row.getVendorNo());
            calc.productVendorNoName = calc.productVendorNo + calc.productVendorName;
            calc.productVendorStatusNo = row.getStatusNo();
            calc.productVendorStatusName = row.getStatusName();
            calc.skuCount = row.getSkuCount() == null ? 0 : row.getSkuCount();
            calc.newSkuCount = row.getNewSkuCount() == null ? 0 : row.getNewSkuCount();
            calc.sales = scale4(nvl(row.getSales()));
            calc.salesPer = percentRatio(calc.sales, totalSales);
            calc.gross = scale4(nvl(row.getGross()));
            calc.grossRate = percentRatio(calc.gross, calc.sales);
            calc.oiAmount = scale4(nvl(row.getOiAmount()));
            calc.allGross = scale4(calc.gross.add(calc.oiAmount));
            calc.allGrossRate = percentRatio(calc.allGross, calc.sales);
            calc.netOrderAmount = scale4(nvl(row.getNetOrderAmount()));
            calc.diffOrderAmount = scale4(nvl(row.getDiffOrderAmount()));
            calc.diffOrderAmountRate = percentRatio(calc.diffOrderAmount, nvl(row.getBaseOrderAmount()));
            calc.unitOutput = calc.skuCount <= 0
                ? BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP)
                : scale4(calc.sales.divide(BigDecimal.valueOf(calc.skuCount), 6, RoundingMode.HALF_UP));
            calc.snapshotTime = snapshotTime;
            calcs.add(calc);
        }

        List<DiagnosisVendorMetricRow> metricRows = buildMetricRows(calcs, context);
        List<DiagnosisVendorJsonRow> jsonRows = buildJsonRows(calcs, context, snapshotTime);
        if (!metricRows.isEmpty()) {
            batchInsertMetrics(metricRows);
        }
        if (!jsonRows.isEmpty()) {
            batchInsertJson(jsonRows);
        }
        return DiagnosisVendorFinalizeResult.builder()
            .metricRows(metricRows.size())
            .jsonRows(jsonRows.size())
            .build();
    }

    private List<DiagnosisVendorMetricRow> buildMetricRows(List<VendorMetricCalc> calcs, DiagnosisFinalizeContext context) {
        List<DiagnosisVendorMetricRow> rows = new ArrayList<>(calcs.size());
        for (VendorMetricCalc calc : calcs) {
            DiagnosisVendorMetricRow row = new DiagnosisVendorMetricRow();
            row.setTenantId(TENANT_ID);
            row.setQueryHash(context.getQueryHash());
            row.setDataVersion(context.getDataVersion());
            row.setProductVendorNo(calc.productVendorNo);
            row.setProductVendorName(calc.productVendorName);
            row.setProductVendorNoName(calc.productVendorNoName);
            row.setProductVendorStatusNo(calc.productVendorStatusNo);
            row.setProductVendorStatusName(calc.productVendorStatusName);
            row.setSkuCount(calc.skuCount);
            row.setNewSkuCount(calc.newSkuCount);
            row.setSales(calc.sales);
            row.setSalesPer(calc.salesPer);
            row.setGross(calc.gross);
            row.setGrossRate(calc.grossRate);
            row.setOiAmount(calc.oiAmount);
            row.setAllGross(calc.allGross);
            row.setAllGrossRate(calc.allGrossRate);
            row.setNetOrderAmount(calc.netOrderAmount);
            row.setDiffOrderAmount(calc.diffOrderAmount);
            row.setDiffOrderAmountRate(calc.diffOrderAmountRate);
            row.setUnitOutput(calc.unitOutput);
            row.setSnapshotTime(calc.snapshotTime);
            rows.add(row);
        }
        return rows;
    }

    private List<DiagnosisVendorJsonRow> buildJsonRows(List<VendorMetricCalc> calcs,
                                                       DiagnosisFinalizeContext context,
                                                       LocalDateTime snapshotTime) {
        List<DiagnosisVendorJsonRow> rows = new ArrayList<>();
        rows.add(jsonRow(context, snapshotTime, "salesShare", "vendor sales share", JsonUtils.toJsonString(buildSalesSharePayload(calcs))));
        rows.add(jsonRow(context, snapshotTime, "summaryOne", "zero sales vendors", JsonUtils.toJsonString(buildSummaryOne(calcs))));
        rows.add(jsonRow(context, snapshotTime, "summaryTwo", "low sales share vendors", JsonUtils.toJsonString(buildSummaryTwo(calcs))));
        rows.add(jsonRow(context, snapshotTime, "summaryThree", "abnormal vendors", JsonUtils.toJsonString(buildSummaryThree(calcs))));
        return rows;
    }

    private DiagnosisVendorJsonRow jsonRow(DiagnosisFinalizeContext context,
                                           LocalDateTime snapshotTime,
                                           String code,
                                           String name,
                                           String payloadJson) {
        DiagnosisVendorJsonRow row = new DiagnosisVendorJsonRow();
        row.setTenantId(TENANT_ID);
        row.setQueryHash(context.getQueryHash());
        row.setDataVersion(context.getDataVersion());
        row.setPayloadCode(code);
        row.setPayloadName(name);
        row.setPayloadJson(payloadJson);
        row.setSnapshotTime(snapshotTime);
        return row;
    }

    private List<Map<String, Object>> buildSalesSharePayload(List<VendorMetricCalc> calcs) {
        List<VendorMetricCalc> sorted = new ArrayList<>(calcs);
        sorted.sort(Comparator.comparing(VendorMetricCalc::getSales, Comparator.nullsFirst(BigDecimal::compareTo)).reversed()
            .thenComparing(VendorMetricCalc::getProductVendorNo, Comparator.nullsFirst(String::compareTo)));
        List<Map<String, Object>> list = new ArrayList<>(sorted.size());
        for (VendorMetricCalc calc : sorted) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("productVendorNo", calc.productVendorNo);
            item.put("productVendorName", calc.productVendorName);
            item.put("sales", scale2(calc.sales));
            item.put("salesPer", scale4(calc.salesPer));
            list.add(item);
        }
        return list;
    }

    private List<String> buildSummaryOne(List<VendorMetricCalc> calcs) {
        List<VendorMetricCalc> filtered = new ArrayList<>();
        for (VendorMetricCalc calc : calcs) {
            if (calc.sales == null || calc.sales.compareTo(BigDecimal.ZERO) <= 0) {
                filtered.add(calc);
            }
        }
        filtered.sort(Comparator.comparing(VendorMetricCalc::getProductVendorNo, Comparator.nullsFirst(String::compareTo)));
        return toSummaryNames(filtered);
    }

    private List<String> buildSummaryTwo(List<VendorMetricCalc> calcs) {
        List<VendorMetricCalc> filtered = new ArrayList<>();
        for (VendorMetricCalc calc : calcs) {
            if (calc.sales != null
                && calc.sales.compareTo(BigDecimal.ZERO) > 0
                && calc.salesPer != null
                && calc.salesPer.compareTo(LOW_SALES_SHARE_THRESHOLD) <= 0) {
                filtered.add(calc);
            }
        }
        filtered.sort(Comparator.comparing(VendorMetricCalc::getSalesPer, Comparator.nullsLast(BigDecimal::compareTo))
            .thenComparing(VendorMetricCalc::getSales, Comparator.nullsFirst(BigDecimal::compareTo))
            .thenComparing(VendorMetricCalc::getProductVendorNo, Comparator.nullsFirst(String::compareTo)));
        return toSummaryNames(filtered);
    }

    private List<String> buildSummaryThree(List<VendorMetricCalc> calcs) {
        List<VendorMetricCalc> filtered = new ArrayList<>();
        for (VendorMetricCalc calc : calcs) {
            if (calc.diffOrderAmountRate != null && calc.diffOrderAmountRate.compareTo(HIGH_DIFF_RATE_THRESHOLD) >= 0) {
                filtered.add(calc);
            }
        }
        filtered.sort(Comparator.comparing(VendorMetricCalc::getDiffOrderAmountRate, Comparator.nullsFirst(BigDecimal::compareTo)).reversed()
            .thenComparing(VendorMetricCalc::getProductVendorNo, Comparator.nullsFirst(String::compareTo)));
        return toSummaryNames(filtered);
    }

    private List<String> toSummaryNames(List<VendorMetricCalc> calcs) {
        List<String> result = new ArrayList<>();
        for (VendorMetricCalc calc : calcs) {
            if (calc.productVendorNoName == null || calc.productVendorNoName.isBlank()) {
                continue;
            }
            result.add(calc.productVendorNoName);
            if (result.size() >= SUMMARY_LIMIT) {
                break;
            }
        }
        return result;
    }

    private BigDecimal percentRatio(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return numerator.divide(denominator, 6, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"))
            .setScale(4, RoundingMode.HALF_UP);
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

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private void batchInsertMetrics(List<DiagnosisVendorMetricRow> rows) {
        for (int start = 0; start < rows.size(); start += INSERT_BATCH_SIZE) {
            int end = Math.min(start + INSERT_BATCH_SIZE, rows.size());
            vendorMapper.batchInsertMetrics(rows.subList(start, end));
        }
    }

    private void batchInsertJson(List<DiagnosisVendorJsonRow> rows) {
        for (int start = 0; start < rows.size(); start += INSERT_BATCH_SIZE) {
            int end = Math.min(start + INSERT_BATCH_SIZE, rows.size());
            vendorMapper.batchInsertJson(rows.subList(start, end));
        }
    }

    private <T> List<T> safeList(List<T> rows) {
        return rows == null ? List.of() : rows;
    }

    @lombok.Data
    private static class VendorMetricCalc {
        private String productVendorNo;
        private String productVendorName;
        private String productVendorNoName;
        private String productVendorStatusNo;
        private String productVendorStatusName;
        private Integer skuCount;
        private Integer newSkuCount;
        private BigDecimal sales;
        private BigDecimal salesPer;
        private BigDecimal gross;
        private BigDecimal grossRate;
        private BigDecimal oiAmount;
        private BigDecimal allGross;
        private BigDecimal allGrossRate;
        private BigDecimal netOrderAmount;
        private BigDecimal diffOrderAmount;
        private BigDecimal diffOrderAmountRate;
        private BigDecimal unitOutput;
        private LocalDateTime snapshotTime;
    }
}

package org.dromara.diagnosis.application.batch.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.application.batch.model.DiagnosisChannelFinalizeResult;
import org.dromara.diagnosis.application.batch.model.DiagnosisFinalizeContext;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisBatchSourceMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisChannelPerformanceMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisChannelContributionRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisChannelTrendRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisDictRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceChannelContributionAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceChannelDailyTrendAggRow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiagnosisChannelPerformanceFinalizeService {

    private static final String TENANT_ID = "000000";

    private static final List<String> ONLINE_CHANNEL_DICT_TYPES = List.of(
        "diag_online_channel_type",
        "sales_online_type",
        "online_sale_channel",
        "online_type"
    );

    private static final String OFFLINE_NAME = "线下";

    private static final Map<String, String> ONLINE_WHITELIST = Map.of(
        "201", "线上自动贩卖机",
        "205", "线上美团外卖"
    );

    private final DiagnosisBatchSourceMapper batchSourceMapper;
    private final DiagnosisChannelPerformanceMapper channelPerformanceMapper;
    private final DiagnosisFinalizeSupport finalizeSupport;

    public DiagnosisChannelFinalizeResult finalizeChannelPerformance(DiagnosisFinalizeContext context) {
        channelPerformanceMapper.deleteContributionByVersion(TENANT_ID, context.getQueryHash(), context.getDataVersion());
        channelPerformanceMapper.deleteTrendByVersion(TENANT_ID, context.getQueryHash(), context.getDataVersion());

        List<DiagnosisSourceChannelContributionAggRow> currentRows = safeContributionRows(
            batchSourceMapper.aggregateChannelContribution(context.getParam()));
        List<DiagnosisSourceChannelContributionAggRow> compareRows = context.getCompareParam() == null
            ? List.of()
            : safeContributionRows(batchSourceMapper.aggregateChannelContribution(context.getCompareParam()));
        List<DiagnosisSourceChannelDailyTrendAggRow> currentTrendRows = safeTrendRows(
            batchSourceMapper.aggregateChannelDailySales(context.getParam()));

        Map<String, String> dictNameMap = buildDictNameMap();
        LinkedHashMap<ChannelKey, Boolean> channels = buildChannelUniverse(currentRows, compareRows, currentTrendRows);
        BigDecimal currentSalesTotal = sumSales(currentRows);
        BigDecimal currentGrossTotal = sumGross(currentRows);
        BigDecimal compareSalesTotal = sumSales(compareRows);
        BigDecimal compareGrossTotal = sumGross(compareRows);

        Map<ChannelKey, DiagnosisSourceChannelContributionAggRow> currentMap = toContributionMap(currentRows);
        Map<ChannelKey, DiagnosisSourceChannelContributionAggRow> compareMap = toContributionMap(compareRows);

        List<DiagnosisChannelContributionRow> contributionRows = new ArrayList<>(channels.size());
        LocalDateTime snapshotTime = LocalDateTime.now();
        for (ChannelKey key : channels.keySet()) {
            DiagnosisSourceChannelContributionAggRow current = currentMap.get(key);
            DiagnosisSourceChannelContributionAggRow compare = compareMap.get(key);

            BigDecimal currentSales = money(current == null ? null : current.getTotalSales());
            BigDecimal currentGross = money(current == null ? null : current.getTotalGross());
            BigDecimal currentCustomerCount = count(current == null ? null : current.getTotalCustomerCount());
            BigDecimal compareSales = money(compare == null ? null : compare.getTotalSales());
            BigDecimal compareGross = money(compare == null ? null : compare.getTotalGross());
            BigDecimal compareCustomerCount = count(compare == null ? null : compare.getTotalCustomerCount());

            String onlineName = resolveOnlineName(key, dictNameMap);
            String channelName = buildChannelName(key, onlineName);

            DiagnosisChannelContributionRow row = new DiagnosisChannelContributionRow();
            row.setTenantId(TENANT_ID);
            row.setQueryHash(context.getQueryHash());
            row.setDataVersion(context.getDataVersion());
            row.setSaleChannel(key.saleChannel());
            row.setOnlineType(key.onlineType());
            row.setOnlineName(onlineName);
            row.setChannelName(channelName);
            row.setRetailTypeId(context.getParam().getRetailTypeId());
            row.setDeptId(finalizeSupport.toLong(context.getParam().getDeptId()));
            row.setBusinessCircleId(context.getParam().getBusinessCircleId());
            row.setDeptGroupId(context.getParam().getDeptGroupId());
            row.setStoreNo(context.getParam().getStoreNo());
            row.setPeriodStart(context.getPeriodStart());
            row.setPeriodEnd(context.getPeriodEnd());
            row.setCompareStart(context.getCompareStart());
            row.setCompareEnd(context.getCompareEnd());
            row.setCurrentSales(currentSales);
            row.setCurrentSalesPer(percent(currentSales, currentSalesTotal));
            row.setCurrentGross(currentGross);
            row.setCurrentGrossPer(percent(currentGross, currentGrossTotal));
            row.setCurrentGrossRate(percent(currentGross, currentSales));
            row.setCurrentCustomerCount(currentCustomerCount);
            row.setCurrentCustomerPrice(rate(currentSales, currentCustomerCount));
            row.setCompareSales(compareSales);
            row.setCompareSalesPer(percent(compareSales, compareSalesTotal));
            row.setCompareSalesInc(growthRate(compareSales, currentSales));
            row.setCompareGross(compareGross);
            row.setCompareGrossPer(percent(compareGross, compareGrossTotal));
            row.setCompareGrossInc(growthRate(compareGross, currentGross));
            row.setCompareGrossRate(percent(compareGross, compareSales));
            row.setCompareCustomerCount(compareCustomerCount);
            row.setCompareCustomerCountInc(growthRate(compareCustomerCount, currentCustomerCount));
            row.setCompareCustomerPrice(rate(compareSales, compareCustomerCount));
            row.setCompareCustomerPriceInc(growthRate(rate(compareSales, compareCustomerCount), rate(currentSales, currentCustomerCount)));
            row.setSnapshotTime(snapshotTime);
            contributionRows.add(row);
        }

        if (!contributionRows.isEmpty()) {
            channelPerformanceMapper.batchInsertContribution(contributionRows);
        }

        List<DiagnosisChannelTrendRow> trendRows = buildTrendRows(context, channels, currentTrendRows, dictNameMap);
        if (!trendRows.isEmpty()) {
            channelPerformanceMapper.batchInsertTrend(trendRows);
        }

        return DiagnosisChannelFinalizeResult.builder()
            .contributionRows(contributionRows.size())
            .trendRows(trendRows.size())
            .build();
    }

    private List<DiagnosisChannelTrendRow> buildTrendRows(DiagnosisFinalizeContext context,
                                                          LinkedHashMap<ChannelKey, Boolean> channels,
                                                          List<DiagnosisSourceChannelDailyTrendAggRow> trendAggRows,
                                                          Map<String, String> dictNameMap) {
        Map<ChannelTrendKey, BigDecimal> trendMap = new LinkedHashMap<>();
        for (DiagnosisSourceChannelDailyTrendAggRow row : trendAggRows) {
            if (row.getPointDate() == null) {
                continue;
            }
            ChannelKey key = normalizeKey(row.getSaleChannel(), row.getOnlineType());
            if (key.saleChannel() == null) {
                continue;
            }
            trendMap.put(new ChannelTrendKey(key, row.getPointDate()), finalizeSupport.nvl(row.getTotalSales()));
        }

        List<LocalDate> dates = finalizeSupport.enumerateDates(context.getPeriodStart(), context.getPeriodEnd());
        List<DiagnosisChannelTrendRow> rows = new ArrayList<>(channels.size() * dates.size());
        LocalDateTime snapshotTime = LocalDateTime.now();

        for (ChannelKey key : channels.keySet()) {
            String onlineName = resolveOnlineName(key, dictNameMap);
            String channelName = buildChannelName(key, onlineName);
            for (int i = 0; i < dates.size(); i++) {
                LocalDate pointDate = dates.get(i);
                BigDecimal sales = trendMap.getOrDefault(new ChannelTrendKey(key, pointDate), BigDecimal.ZERO);

                DiagnosisChannelTrendRow row = new DiagnosisChannelTrendRow();
                row.setTenantId(TENANT_ID);
                row.setQueryHash(context.getQueryHash());
                row.setDataVersion(context.getDataVersion());
                row.setSaleChannel(key.saleChannel());
                row.setOnlineType(key.onlineType());
                row.setOnlineName(onlineName);
                row.setChannelName(channelName);
                row.setRetailTypeId(context.getParam().getRetailTypeId());
                row.setDeptId(finalizeSupport.toLong(context.getParam().getDeptId()));
                row.setBusinessCircleId(context.getParam().getBusinessCircleId());
                row.setDeptGroupId(context.getParam().getDeptGroupId());
                row.setStoreNo(context.getParam().getStoreNo());
                row.setPointIndex(i + 1);
                row.setPointDate(pointDate);
                row.setCurrentSales(money(sales));
                row.setSnapshotTime(snapshotTime);
                rows.add(row);
            }
        }
        return rows;
    }

    private Map<String, String> buildDictNameMap() {
        List<DiagnosisDictRow> rows = batchSourceMapper.selectOnlineChannelDictRows(ONLINE_CHANNEL_DICT_TYPES);
        Map<String, String> map = new LinkedHashMap<>();
        if (rows != null) {
            for (DiagnosisDictRow row : rows) {
                if (row.getDictValue() == null || row.getDictValue().isBlank()) {
                    continue;
                }
                if (row.getDictLabel() == null || row.getDictLabel().isBlank()) {
                    continue;
                }
                map.putIfAbsent(row.getDictValue().trim(), row.getDictLabel().trim());
            }
        }
        for (Map.Entry<String, String> entry : ONLINE_WHITELIST.entrySet()) {
            map.putIfAbsent(entry.getKey(), entry.getValue());
        }
        return map;
    }

    private LinkedHashMap<ChannelKey, Boolean> buildChannelUniverse(List<DiagnosisSourceChannelContributionAggRow> currentRows,
                                                                    List<DiagnosisSourceChannelContributionAggRow> compareRows,
                                                                    List<DiagnosisSourceChannelDailyTrendAggRow> trendRows) {
        LinkedHashMap<ChannelKey, Boolean> channels = new LinkedHashMap<>();
        channels.put(new ChannelKey(1, null), Boolean.TRUE);
        for (String onlineType : ONLINE_WHITELIST.keySet()) {
            channels.put(new ChannelKey(2, onlineType), Boolean.TRUE);
        }
        appendContributionKeys(channels, currentRows);
        appendContributionKeys(channels, compareRows);
        if (trendRows != null) {
            for (DiagnosisSourceChannelDailyTrendAggRow row : trendRows) {
                ChannelKey key = normalizeKey(row.getSaleChannel(), row.getOnlineType());
                if (key.saleChannel() != null) {
                    channels.putIfAbsent(key, Boolean.TRUE);
                }
            }
        }
        return channels;
    }

    private void appendContributionKeys(LinkedHashMap<ChannelKey, Boolean> channels,
                                        List<DiagnosisSourceChannelContributionAggRow> rows) {
        if (rows == null) {
            return;
        }
        for (DiagnosisSourceChannelContributionAggRow row : rows) {
            ChannelKey key = normalizeKey(row.getSaleChannel(), row.getOnlineType());
            if (key.saleChannel() != null) {
                channels.putIfAbsent(key, Boolean.TRUE);
            }
        }
    }

    private Map<ChannelKey, DiagnosisSourceChannelContributionAggRow> toContributionMap(List<DiagnosisSourceChannelContributionAggRow> rows) {
        Map<ChannelKey, DiagnosisSourceChannelContributionAggRow> map = new LinkedHashMap<>();
        if (rows == null) {
            return map;
        }
        for (DiagnosisSourceChannelContributionAggRow row : rows) {
            ChannelKey key = normalizeKey(row.getSaleChannel(), row.getOnlineType());
            if (key.saleChannel() == null) {
                continue;
            }
            map.put(key, row);
        }
        return map;
    }

    private ChannelKey normalizeKey(Integer saleChannel, String onlineType) {
        if (saleChannel == null) {
            return new ChannelKey(null, null);
        }
        if (saleChannel == 1) {
            return new ChannelKey(1, null);
        }
        String code = onlineType == null ? null : onlineType.trim();
        if (code != null && code.isEmpty()) {
            code = null;
        }
        return new ChannelKey(saleChannel, code);
    }

    private List<DiagnosisSourceChannelContributionAggRow> safeContributionRows(List<DiagnosisSourceChannelContributionAggRow> rows) {
        return rows == null ? List.of() : rows;
    }

    private List<DiagnosisSourceChannelDailyTrendAggRow> safeTrendRows(List<DiagnosisSourceChannelDailyTrendAggRow> rows) {
        return rows == null ? List.of() : rows;
    }

    private BigDecimal sumSales(List<DiagnosisSourceChannelContributionAggRow> rows) {
        BigDecimal sum = BigDecimal.ZERO;
        for (DiagnosisSourceChannelContributionAggRow row : rows) {
            sum = sum.add(finalizeSupport.nvl(row.getTotalSales()));
        }
        return sum;
    }

    private BigDecimal sumGross(List<DiagnosisSourceChannelContributionAggRow> rows) {
        BigDecimal sum = BigDecimal.ZERO;
        for (DiagnosisSourceChannelContributionAggRow row : rows) {
            sum = sum.add(finalizeSupport.nvl(row.getTotalGross()));
        }
        return sum;
    }

    private String resolveOnlineName(ChannelKey key, Map<String, String> dictNameMap) {
        if (key.saleChannel() == null || key.saleChannel() == 1) {
            return null;
        }
        if (key.onlineType() == null) {
            return "未知渠道";
        }
        return dictNameMap.getOrDefault(key.onlineType(), "未知渠道");
    }

    private String buildChannelName(ChannelKey key, String onlineName) {
        if (key.saleChannel() != null && key.saleChannel() == 1) {
            return OFFLINE_NAME;
        }
        return "线上-" + (onlineName == null ? "未知渠道" : onlineName);
    }

    private BigDecimal money(BigDecimal value) {
        return finalizeSupport.round2(finalizeSupport.nvl(value));
    }

    private BigDecimal count(BigDecimal value) {
        return finalizeSupport.round2(finalizeSupport.nvl(value));
    }

    private BigDecimal percent(BigDecimal numerator, BigDecimal denominator) {
        return finalizeSupport.round2(finalizeSupport.ratioPercentOrZero(
            finalizeSupport.nvl(numerator), finalizeSupport.nvl(denominator)));
    }

    private BigDecimal rate(BigDecimal numerator, BigDecimal denominator) {
        return finalizeSupport.round2(finalizeSupport.divideOrZero(
            finalizeSupport.nvl(numerator), finalizeSupport.nvl(denominator)));
    }

    private BigDecimal growthRate(BigDecimal compareValue, BigDecimal currentValue) {
        return finalizeSupport.round2(finalizeSupport.nvl(finalizeSupport.calcGrowthRate(
            finalizeSupport.nvl(compareValue), finalizeSupport.nvl(currentValue))));
    }

    private record ChannelKey(Integer saleChannel, String onlineType) {
    }

    private record ChannelTrendKey(ChannelKey channel, LocalDate pointDate) {
    }
}

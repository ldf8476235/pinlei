package org.dromara.diagnosis.application.batch.service;

import cn.hutool.core.lang.Dict;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.diagnosis.api.request.DiagnosisSessionCreateRequest;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceDailyTrendRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceOverviewAggRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceShardParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DiagnosisFinalizeSupport {

    public DiagnosisSourceShardParam buildParam(LocalDate periodStart, LocalDate periodEnd, String requestJson) {
        DiagnosisSourceShardParam param = new DiagnosisSourceShardParam();
        param.setPeriodStart(periodStart);
        param.setPeriodEnd(periodEnd);
        fillFilterParam(param, requestJson);
        return param;
    }

    public void fillFilterParam(DiagnosisSourceShardParam param, String requestJson) {
        Dict map = JsonUtils.parseMap(requestJson);
        if (map == null) {
            return;
        }
        param.setClassLevel(map.getInt("classLevel"));
        param.setClassNo(trim(map.getStr("classNo")));
        param.setDeptId(trim(map.getStr("deptId")));
        param.setRetailTypeId(trim(map.getStr("retailTypeId")));
        param.setBusinessCircleId(trim(map.getStr("businessCircleId")));
        param.setDeptGroupId(trim(map.getStr("deptGroupId")));
        param.setStoreNo(trim(map.getStr("storeNo")));
    }

    public DiagnosisSessionCreateRequest buildHashRequest(LocalDate periodStart, LocalDate periodEnd,
                                                          LocalDate compareStart, LocalDate compareEnd,
                                                          String requestJson) {
        DiagnosisSessionCreateRequest request = new DiagnosisSessionCreateRequest();
        request.setPeriodStart(periodStart);
        request.setPeriodEnd(periodEnd);
        request.setCompareStart(compareStart);
        request.setCompareEnd(compareEnd);
        Dict map = JsonUtils.parseMap(requestJson);
        if (map != null) {
            request.setClassLevel(map.getInt("classLevel"));
            request.setClassNo(trim(map.getStr("classNo")));
            request.setClassName(trim(map.getStr("className")));
            request.setDeptId(trim(map.getStr("deptId")));
            request.setRetailTypeId(trim(map.getStr("retailTypeId")));
            request.setBusinessCircleId(trim(map.getStr("businessCircleId")));
            request.setDeptGroupId(trim(map.getStr("deptGroupId")));
            request.setStoreNo(trim(map.getStr("storeNo")));
            request.setExtraFilterJson(trim(map.getStr("extraFilterJson")));
        }
        return request;
    }

    public DiagnosisSourceOverviewAggRow defaultOverview(DiagnosisSourceOverviewAggRow row) {
        if (row == null) {
            DiagnosisSourceOverviewAggRow d = new DiagnosisSourceOverviewAggRow();
            d.setTotalSales(BigDecimal.ZERO);
            d.setTotalGross(BigDecimal.ZERO);
            d.setTotalSaleQuantity(BigDecimal.ZERO);
            d.setTotalSalesCost(BigDecimal.ZERO);
            d.setTotalSku(0);
            d.setActiveSku(0);
            return d;
        }
        if (row.getTotalSales() == null) {
            row.setTotalSales(BigDecimal.ZERO);
        }
        if (row.getTotalGross() == null) {
            row.setTotalGross(BigDecimal.ZERO);
        }
        if (row.getTotalSaleQuantity() == null) {
            row.setTotalSaleQuantity(BigDecimal.ZERO);
        }
        if (row.getTotalSalesCost() == null) {
            row.setTotalSalesCost(BigDecimal.ZERO);
        }
        if (row.getTotalSku() == null) {
            row.setTotalSku(0);
        }
        if (row.getActiveSku() == null) {
            row.setActiveSku(0);
        }
        return row;
    }

    public Map<LocalDate, DiagnosisSourceDailyTrendRow> toDailyMap(List<DiagnosisSourceDailyTrendRow> rows) {
        Map<LocalDate, DiagnosisSourceDailyTrendRow> map = new HashMap<>();
        if (rows == null) {
            return map;
        }
        for (DiagnosisSourceDailyTrendRow row : rows) {
            if (row == null || row.getPointDate() == null) {
                continue;
            }
            map.put(row.getPointDate(), row);
        }
        return map;
    }

    public List<LocalDate> enumerateDates(LocalDate start, LocalDate end) {
        long days = ChronoUnit.DAYS.between(start, end);
        List<LocalDate> dates = new ArrayList<>((int) days + 1);
        for (long i = 0; i <= days; i++) {
            dates.add(start.plusDays(i));
        }
        return dates;
    }

    public String trim(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }

    public Long toLong(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public BigDecimal calcMargin(BigDecimal sales, BigDecimal gross) {
        if (sales == null || sales.compareTo(BigDecimal.ZERO) == 0 || gross == null) {
            return BigDecimal.ZERO;
        }
        return gross.multiply(new BigDecimal("100")).divide(sales, 6, RoundingMode.HALF_UP);
    }

    public BigDecimal calcRate(Integer active, Integer total) {
        if (active == null || total == null || total == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(active)
            .multiply(new BigDecimal("100"))
            .divide(BigDecimal.valueOf(total), 6, RoundingMode.HALF_UP);
    }

    public BigDecimal safeDivide(BigDecimal dividend, BigDecimal divisor) {
        if (dividend == null || divisor == null || divisor.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return dividend.divide(divisor, 6, RoundingMode.HALF_UP);
    }

    public BigDecimal divideOrZero(BigDecimal dividend, BigDecimal divisor) {
        BigDecimal result = safeDivide(dividend, divisor);
        return result == null ? BigDecimal.ZERO : result;
    }

    public BigDecimal ratioPercent(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return numerator.multiply(new BigDecimal("100")).divide(denominator, 6, RoundingMode.HALF_UP);
    }

    public BigDecimal ratioPercentOrZero(BigDecimal numerator, BigDecimal denominator) {
        BigDecimal result = ratioPercent(numerator, denominator);
        return result == null ? BigDecimal.ZERO : result;
    }

    public BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public BigDecimal toDecimal(Long value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(value);
    }

    public BigDecimal calcGrowthRate(BigDecimal compareValue, BigDecimal currentValue) {
        if (compareValue == null || compareValue.compareTo(BigDecimal.ZERO) == 0 || currentValue == null) {
            return null;
        }
        return currentValue.subtract(compareValue)
            .multiply(new BigDecimal("100"))
            .divide(compareValue, 6, RoundingMode.HALF_UP);
    }

    public BigDecimal round2(BigDecimal value) {
        return nvl(value).setScale(2, RoundingMode.HALF_UP);
    }
}

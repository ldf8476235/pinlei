package org.dromara.diagnosis.application.service;

import org.dromara.diagnosis.api.request.DiagnosisCompareWindowSuggestRequest;
import org.dromara.diagnosis.api.request.DiagnosisCompareWindowValidateRequest;
import org.dromara.diagnosis.api.response.DiagnosisCompareWindowSuggestResponse;
import org.dromara.diagnosis.api.response.DiagnosisCompareWindowValidateResponse;
import org.dromara.diagnosis.api.response.DiagnosisDateRangesResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 诊断日期与对比窗口服务.
 */
@Service
public class DiagnosisDateWindowService {

    public DiagnosisDateRangesResponse getDateRanges() {
        DiagnosisDateRangesResponse response = new DiagnosisDateRangesResponse();
        response.setMinDate(LocalDate.now().minusYears(3));
        response.setMaxDate(LocalDate.now());
        response.setDefaultRecentDays(30);
        return response;
    }

    public DiagnosisCompareWindowSuggestResponse suggest(DiagnosisCompareWindowSuggestRequest request) {
        DiagnosisCompareWindowSuggestResponse response = new DiagnosisCompareWindowSuggestResponse();
        LocalDate start = request.getPeriodStart();
        LocalDate end = request.getPeriodEnd();
        if (start == null || end == null) {
            return response;
        }
        String strategy = StringUtils.hasText(request.getStrategy()) ? request.getStrategy().trim().toUpperCase() : "YOY";
        switch (strategy) {
            case "MOM" -> {
                long days = ChronoUnit.DAYS.between(start, end) + 1;
                response.setStrategy("MOM");
                response.setCompareEnd(start.minusDays(1));
                response.setCompareStart(start.minusDays(days));
            }
            case "CUSTOM" -> {
                response.setStrategy("CUSTOM");
                response.setCompareStart(null);
                response.setCompareEnd(null);
            }
            default -> {
                response.setStrategy("YOY");
                response.setCompareStart(start.minusYears(1));
                response.setCompareEnd(end.minusYears(1));
            }
        }
        return response;
    }

    public DiagnosisCompareWindowValidateResponse validate(DiagnosisCompareWindowValidateRequest request) {
        DiagnosisCompareWindowValidateResponse response = new DiagnosisCompareWindowValidateResponse();
        if (request.getPeriodStart() == null || request.getPeriodEnd() == null
            || request.getCompareStart() == null || request.getCompareEnd() == null) {
            response.setValid(Boolean.FALSE);
            response.setMessage("日期参数不完整");
            return response;
        }
        if (request.getPeriodEnd().isBefore(request.getPeriodStart())) {
            response.setValid(Boolean.FALSE);
            response.setMessage("本期结束日期不能早于开始日期");
            return response;
        }
        if (request.getCompareEnd().isBefore(request.getCompareStart())) {
            response.setValid(Boolean.FALSE);
            response.setMessage("对比期结束日期不能早于开始日期");
            return response;
        }
        long pDays = ChronoUnit.DAYS.between(request.getPeriodStart(), request.getPeriodEnd());
        long cDays = ChronoUnit.DAYS.between(request.getCompareStart(), request.getCompareEnd());
        if (pDays != cDays) {
            response.setValid(Boolean.FALSE);
            response.setMessage("本期与对比期天数不一致");
            return response;
        }
        response.setValid(Boolean.TRUE);
        response.setMessage("OK");
        return response;
    }
}


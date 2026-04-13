package org.dromara.diagnosis.api.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.request.DiagnosisSessionCreateRequest;
import org.dromara.diagnosis.api.request.LegacyCategoryTrendChangesRequest;
import org.dromara.diagnosis.api.response.DiagnosisCategoryPerformanceTrendPointResponse;
import org.dromara.diagnosis.api.response.DiagnosisCategoryPerformanceTrendResponse;
import org.dromara.diagnosis.api.response.DiagnosisSessionCreateResponse;
import org.dromara.diagnosis.api.response.LegacyCategoryTrendChangesLineResponse;
import org.dromara.diagnosis.api.response.LegacyCategoryTrendChangesResultResponse;
import org.dromara.diagnosis.api.response.LegacyNodeResponse;
import org.dromara.diagnosis.application.service.DiagnosisSessionService;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 旧版销售品类接口兼容层.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/salesStoreClass")
public class LegacySalesStoreClassController {

    private static final DateTimeFormatter LEGACY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final DiagnosisSessionService diagnosisSessionService;

    @PostMapping("/trendChanges")
    public LegacyNodeResponse<LegacyCategoryTrendChangesResultResponse> trendChanges(@RequestBody LegacyCategoryTrendChangesRequest request) {
        if (request == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "请求不能为空");
        }

        DiagnosisSessionCreateRequest sessionRequest = new DiagnosisSessionCreateRequest();
        sessionRequest.setDeptId(normalizeLegacyValue(request.getDeptId()));
        sessionRequest.setRetailTypeId(normalizeLegacyValue(request.getRetailTypeId()));
        sessionRequest.setBusinessCircleId(normalizeLegacyValue(request.getBusinessCircleId()));
        sessionRequest.setDeptGroupId(normalizeLegacyValue(request.getDeptGroupId()));
        sessionRequest.setStoreNo(normalizeLegacyValue(request.getStoreNo()));
        sessionRequest.setClassLevel(request.getClassLevel());
        sessionRequest.setClassNo(normalizeLegacyValue(request.getClassNo()));
        sessionRequest.setPeriodStart(request.getCurrentStartDate());
        sessionRequest.setPeriodEnd(request.getCurrentEndDate());
        sessionRequest.setCompareStart(request.getCompareStartDate());
        sessionRequest.setCompareEnd(request.getCompareEndDate());
        sessionRequest.setTriggerIfMissing(Boolean.TRUE);
        sessionRequest.setWaitSeconds(30);

        DiagnosisSessionCreateResponse session = diagnosisSessionService.createSession(sessionRequest);
        if (!Boolean.TRUE.equals(session.getReady())) {
            throw new DiagnosisBizException(DiagnosisErrorCode.DATA_PREPARING, "趋势结果准备中，请稍后重试");
        }

        DiagnosisCategoryPerformanceTrendResponse trendResponse = diagnosisSessionService.getTrendChanges(session.getSessionId());
        LegacyCategoryTrendChangesResultResponse result = new LegacyCategoryTrendChangesResultResponse();
        result.setFlag(Boolean.FALSE);
        result.setLineDate(mapLegacyLines(trendResponse.getLineDate()));
        result.setLineDateTQ(new ArrayList<>());
        result.setXdata(formatDates(trendResponse.getXdata()));
        result.setXdataDB(formatDates(trendResponse.getXdataDB()));
        return LegacyNodeResponse.ok(result);
    }

    private List<LegacyCategoryTrendChangesLineResponse> mapLegacyLines(List<DiagnosisCategoryPerformanceTrendPointResponse> points) {
        List<LegacyCategoryTrendChangesLineResponse> lines = new ArrayList<>();
        if (points == null) {
            return lines;
        }
        for (DiagnosisCategoryPerformanceTrendPointResponse point : points) {
            if (point == null) {
                continue;
            }
            LegacyCategoryTrendChangesLineResponse line = new LegacyCategoryTrendChangesLineResponse();
            line.setDataDate(formatDate(point.getDataDate()));
            line.setDataYearMonth(null);
            line.setSales(point.getSales());
            line.setSaleQuantity(point.getSaleQuantity());
            line.setGross(point.getGross());
            line.setGrossRate(point.getGrossRate());
            line.setCustomerCount(point.getCustomerCount());
            line.setCustomerPrice(point.getCustomerPrice());
            line.setStockCostRate(point.getStockCostRate());
            line.setStockCost(point.getStockCost());
            line.setSaleCost(point.getSaleCost());
            line.setFlag(point.getFlag());
            line.setType(null);
            lines.add(line);
        }
        return lines;
    }

    private List<String> formatDates(List<String> dates) {
        List<String> result = new ArrayList<>();
        if (dates == null) {
            return result;
        }
        for (String date : dates) {
            result.add(formatDate(date));
        }
        return result;
    }

    private String formatDate(String date) {
        if (date == null || date.isBlank()) {
            return date;
        }
        return LEGACY_DATE_FORMAT.format(java.time.LocalDate.parse(date));
    }

    private String normalizeLegacyValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || "0".equals(trimmed) || "all".equalsIgnoreCase(trimmed)) {
            return null;
        }
        return trimmed;
    }
}

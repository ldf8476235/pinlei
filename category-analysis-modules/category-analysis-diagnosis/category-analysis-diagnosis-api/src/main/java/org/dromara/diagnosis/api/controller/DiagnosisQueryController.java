package org.dromara.diagnosis.api.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.response.DiagnosisCategoryPerformanceTrendResponse;
import org.dromara.diagnosis.api.request.DiagnosisRecordQueryRequest;
import org.dromara.diagnosis.api.request.DiagnosisSessionCreateRequest;
import org.dromara.diagnosis.api.request.LegacyClassSalesListRequest;
import org.dromara.diagnosis.api.response.DiagnosisRecordPageResponse;
import org.dromara.diagnosis.api.response.DiagnosisIntroduceDirectionResponse;
import org.dromara.diagnosis.api.response.DiagnosisOverviewResponse;
import org.dromara.diagnosis.api.response.DiagnosisOverallSummaryResponse;
import org.dromara.diagnosis.api.response.DiagnosisSessionCreateResponse;
import org.dromara.diagnosis.api.response.DiagnosisSessionStatusResponse;
import org.dromara.diagnosis.api.response.DiagnosisTrendsResponse;
import org.dromara.diagnosis.api.response.LegacyClassSalesListResponse;
import org.dromara.diagnosis.api.response.LegacyNodeResponse;
import org.dromara.diagnosis.application.service.DiagnosisOverallSummaryService;
import org.dromara.diagnosis.application.service.DiagnosisRecordService;
import org.dromara.diagnosis.application.service.DiagnosisSessionService;
import org.dromara.diagnosis.application.service.DiagnosisIntroduceDirectionService;
import org.dromara.diagnosis.application.service.LegacyClassSalesListService;
import org.dromara.diagnosis.common.model.DiagnosisApiResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 诊断查询接口（Step4 首版）.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/diagnosis")
public class DiagnosisQueryController {

    private final DiagnosisSessionService diagnosisSessionService;
    private final DiagnosisRecordService diagnosisRecordService;
    private final LegacyClassSalesListService legacyClassSalesListService;
    private final DiagnosisOverallSummaryService diagnosisOverallSummaryService;
    private final DiagnosisIntroduceDirectionService diagnosisIntroduceDirectionService;

    @PostMapping("/sessions")
    public DiagnosisApiResponse<DiagnosisSessionCreateResponse> createSession(@RequestBody DiagnosisSessionCreateRequest request) {
        DiagnosisSessionCreateRequest actual = request == null ? new DiagnosisSessionCreateRequest() : request;
        return DiagnosisApiResponse.ok(diagnosisSessionService.createSession(actual), nextRequestId());
    }

    @GetMapping("/sessions/{sessionId}/status")
    public DiagnosisApiResponse<DiagnosisSessionStatusResponse> getSessionStatus(@PathVariable("sessionId") String sessionId) {
        return DiagnosisApiResponse.ok(diagnosisSessionService.getSessionStatus(sessionId), nextRequestId());
    }

    @GetMapping("/overview")
    public DiagnosisApiResponse<DiagnosisOverviewResponse> getOverview(@RequestParam("sessionId") String sessionId) {
        return DiagnosisApiResponse.ok(diagnosisSessionService.getOverview(sessionId), nextRequestId());
    }

    @GetMapping("/overall-summary")
    public DiagnosisApiResponse<DiagnosisOverallSummaryResponse> getOverallSummary(@RequestParam("sessionId") String sessionId) {
        return DiagnosisApiResponse.ok(diagnosisOverallSummaryService.getOverallSummary(sessionId), nextRequestId());
    }

    @GetMapping("/introduce-direction")
    public DiagnosisApiResponse<DiagnosisIntroduceDirectionResponse> getIntroduceDirection(@RequestParam("sessionId") String sessionId) {
        return DiagnosisApiResponse.ok(diagnosisIntroduceDirectionService.getIntroduceDirection(sessionId), nextRequestId());
    }

    @GetMapping("/trends")
    public DiagnosisApiResponse<DiagnosisTrendsResponse> getTrends(@RequestParam("sessionId") String sessionId,
                                                                   @RequestParam(value = "metricCode", required = false) String metricCode,
                                                                   @RequestParam(value = "tabType", required = false) String tabType) {
        return DiagnosisApiResponse.ok(diagnosisSessionService.getTrends(sessionId, metricCode, tabType), nextRequestId());
    }

    @GetMapping("/trend-changes")
    public DiagnosisApiResponse<DiagnosisCategoryPerformanceTrendResponse> getTrendChanges(@RequestParam("sessionId") String sessionId) {
        return DiagnosisApiResponse.ok(diagnosisSessionService.getTrendChanges(sessionId), nextRequestId());
    }

    @PostMapping("/records")
    public DiagnosisApiResponse<DiagnosisRecordPageResponse> queryRecords(@RequestBody(required = false) DiagnosisRecordQueryRequest request) {
        return DiagnosisApiResponse.ok(diagnosisRecordService.queryRecords(request), nextRequestId());
    }

    @PostMapping("/class-sales-list")
    public LegacyNodeResponse<LegacyClassSalesListResponse> classSalesList(@RequestBody LegacyClassSalesListRequest request) {
        return LegacyNodeResponse.ok(legacyClassSalesListService.getSalesList(request));
    }

    private String nextRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

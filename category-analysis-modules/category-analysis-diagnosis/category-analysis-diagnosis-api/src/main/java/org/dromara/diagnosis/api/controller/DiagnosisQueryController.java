package org.dromara.diagnosis.api.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.request.DiagnosisSessionCreateRequest;
import org.dromara.diagnosis.api.response.DiagnosisOverviewResponse;
import org.dromara.diagnosis.api.response.DiagnosisSessionCreateResponse;
import org.dromara.diagnosis.api.response.DiagnosisTrendsResponse;
import org.dromara.diagnosis.application.service.DiagnosisSessionService;
import org.dromara.diagnosis.common.model.DiagnosisApiResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @PostMapping("/sessions")
    public DiagnosisApiResponse<DiagnosisSessionCreateResponse> createSession(@RequestBody DiagnosisSessionCreateRequest request) {
        DiagnosisSessionCreateRequest actual = request == null ? new DiagnosisSessionCreateRequest() : request;
        return DiagnosisApiResponse.ok(diagnosisSessionService.createSession(actual), nextRequestId());
    }

    @GetMapping("/overview")
    public DiagnosisApiResponse<DiagnosisOverviewResponse> getOverview(@RequestParam("sessionId") String sessionId) {
        return DiagnosisApiResponse.ok(diagnosisSessionService.getOverview(sessionId), nextRequestId());
    }

    @GetMapping("/trends")
    public DiagnosisApiResponse<DiagnosisTrendsResponse> getTrends(@RequestParam("sessionId") String sessionId,
                                                                   @RequestParam(value = "metricCode", required = false) String metricCode) {
        return DiagnosisApiResponse.ok(diagnosisSessionService.getTrends(sessionId, metricCode), nextRequestId());
    }

    private String nextRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

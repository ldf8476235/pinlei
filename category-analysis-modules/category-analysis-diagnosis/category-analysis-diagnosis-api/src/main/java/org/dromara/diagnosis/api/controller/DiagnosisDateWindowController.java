package org.dromara.diagnosis.api.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.request.DiagnosisCompareWindowSuggestRequest;
import org.dromara.diagnosis.api.request.DiagnosisCompareWindowValidateRequest;
import org.dromara.diagnosis.api.response.DiagnosisCompareWindowSuggestResponse;
import org.dromara.diagnosis.api.response.DiagnosisCompareWindowValidateResponse;
import org.dromara.diagnosis.api.response.DiagnosisDateRangesResponse;
import org.dromara.diagnosis.application.service.DiagnosisDateWindowService;
import org.dromara.diagnosis.common.model.DiagnosisApiResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 诊断日期与对比窗口接口.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/diagnosis")
public class DiagnosisDateWindowController {

    private final DiagnosisDateWindowService diagnosisDateWindowService;

    @GetMapping("/date-ranges")
    public DiagnosisApiResponse<DiagnosisDateRangesResponse> getDateRanges() {
        return DiagnosisApiResponse.ok(diagnosisDateWindowService.getDateRanges(), nextRequestId());
    }

    @PostMapping("/compare-windows/suggest")
    public DiagnosisApiResponse<DiagnosisCompareWindowSuggestResponse> suggest(@RequestBody DiagnosisCompareWindowSuggestRequest request) {
        DiagnosisCompareWindowSuggestRequest actual = request == null ? new DiagnosisCompareWindowSuggestRequest() : request;
        return DiagnosisApiResponse.ok(diagnosisDateWindowService.suggest(actual), nextRequestId());
    }

    @PostMapping("/compare-windows/validate")
    public DiagnosisApiResponse<DiagnosisCompareWindowValidateResponse> validate(@RequestBody DiagnosisCompareWindowValidateRequest request) {
        DiagnosisCompareWindowValidateRequest actual = request == null ? new DiagnosisCompareWindowValidateRequest() : request;
        return DiagnosisApiResponse.ok(diagnosisDateWindowService.validate(actual), nextRequestId());
    }

    private String nextRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}


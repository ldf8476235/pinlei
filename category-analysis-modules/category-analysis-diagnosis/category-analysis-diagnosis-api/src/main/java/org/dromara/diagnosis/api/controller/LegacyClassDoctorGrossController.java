package org.dromara.diagnosis.api.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.request.LegacyGrossSalesListRequest;
import org.dromara.diagnosis.api.request.LegacyGrossSessionRequest;
import org.dromara.diagnosis.api.response.DiagnosisSessionStatusResponse;
import org.dromara.diagnosis.api.response.LegacyGrossFourQuadrantResponse;
import org.dromara.diagnosis.api.response.LegacyGrossSalesListResponse;
import org.dromara.diagnosis.api.response.LegacyGrossSalesPerResponse;
import org.dromara.diagnosis.api.response.LegacyGrossSkuChangeResponse;
import org.dromara.diagnosis.api.response.LegacyGrossSkuPerResponse;
import org.dromara.diagnosis.api.response.LegacyNodeResponse;
import org.dromara.diagnosis.application.service.DiagnosisSessionService;
import org.dromara.diagnosis.application.service.GrossContributionService;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/classDoctorGross")
public class LegacyClassDoctorGrossController {

    private final DiagnosisSessionService diagnosisSessionService;
    private final GrossContributionService grossContributionService;

    @PostMapping("/queryGrossFourQuadrant")
    public LegacyNodeResponse<LegacyGrossFourQuadrantResponse> queryGrossFourQuadrant(@RequestBody LegacyGrossSessionRequest request) {
        String sessionId = resolveSessionId(request, "gross contribution snapshot is preparing");
        return LegacyNodeResponse.ok(grossContributionService.getGrossFourQuadrant(sessionId));
    }

    @PostMapping("/queryGrossSalesPer")
    public LegacyNodeResponse<LegacyGrossSalesPerResponse> queryGrossSalesPer(@RequestBody LegacyGrossSessionRequest request) {
        String sessionId = resolveSessionId(request, "gross contribution snapshot is preparing");
        return LegacyNodeResponse.ok(grossContributionService.getGrossSalesPer(sessionId));
    }

    @PostMapping("/queryGrossSkuPer")
    public LegacyNodeResponse<LegacyGrossSkuPerResponse> queryGrossSkuPer(@RequestBody LegacyGrossSessionRequest request) {
        String sessionId = resolveSessionId(request, "gross contribution snapshot is preparing");
        return LegacyNodeResponse.ok(grossContributionService.getGrossSkuPer(sessionId));
    }

    @PostMapping("/queryGrossSkuChange")
    public LegacyNodeResponse<LegacyGrossSkuChangeResponse> queryGrossSkuChange(@RequestBody LegacyGrossSessionRequest request) {
        String sessionId = resolveSessionId(request, "gross contribution snapshot is preparing");
        return LegacyNodeResponse.ok(grossContributionService.getGrossSkuChange(sessionId));
    }

    @PostMapping("/queryGrossSalesList")
    public LegacyNodeResponse<LegacyGrossSalesListResponse> queryGrossSalesList(@RequestBody LegacyGrossSalesListRequest request) {
        String sessionId = resolveSessionId(request, "gross contribution snapshot is preparing");
        return LegacyNodeResponse.ok(grossContributionService.getGrossSalesList(
            sessionId,
            request.getStatus(),
            request.getPromotion(),
            request.getCurrentGross(),
            request.getCompareGross(),
            request.getPage(),
            request.getSize(),
            request.getOrder(),
            request.getOrderType()
        ));
    }

    private String resolveSessionId(LegacyGrossSessionRequest request, String preparingMessage) {
        if (request == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "request must not be null");
        }
        if (request.getSessionId() == null || request.getSessionId().isBlank()) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "sessionId is required");
        }
        DiagnosisSessionStatusResponse status = diagnosisSessionService.getSessionStatus(request.getSessionId());
        if (!Boolean.TRUE.equals(status.getReady())) {
            throw new DiagnosisBizException(DiagnosisErrorCode.DATA_PREPARING, preparingMessage);
        }
        return request.getSessionId();
    }
}


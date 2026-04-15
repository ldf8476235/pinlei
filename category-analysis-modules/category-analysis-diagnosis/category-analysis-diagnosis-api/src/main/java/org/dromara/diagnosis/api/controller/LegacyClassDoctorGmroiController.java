package org.dromara.diagnosis.api.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.request.LegacyGmroiSalesListRequest;
import org.dromara.diagnosis.api.request.LegacyGmroiSessionRequest;
import org.dromara.diagnosis.api.response.DiagnosisSessionStatusResponse;
import org.dromara.diagnosis.api.response.LegacyGmroiFourQuadrantResponse;
import org.dromara.diagnosis.api.response.LegacyGmroiSalesListResponse;
import org.dromara.diagnosis.api.response.LegacyGmroiSkuChangeResponse;
import org.dromara.diagnosis.api.response.LegacyGmroiSkuNumResponse;
import org.dromara.diagnosis.api.response.LegacyGmroiSkuPerResponse;
import org.dromara.diagnosis.api.response.LegacyNodeResponse;
import org.dromara.diagnosis.application.service.DiagnosisSessionService;
import org.dromara.diagnosis.application.service.GmroiContributionService;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/classDoctorGmroi")
public class LegacyClassDoctorGmroiController {

    private final DiagnosisSessionService diagnosisSessionService;
    private final GmroiContributionService gmroiContributionService;

    @PostMapping("/queryGmroiFourQuadrant")
    public LegacyNodeResponse<LegacyGmroiFourQuadrantResponse> queryGmroiFourQuadrant(@RequestBody LegacyGmroiSessionRequest request) {
        String sessionId = resolveSessionId(request, "gmroi snapshot is preparing");
        return LegacyNodeResponse.ok(gmroiContributionService.getGmroiFourQuadrant(sessionId));
    }

    @PostMapping("/queryGmroiSkuNum")
    public LegacyNodeResponse<LegacyGmroiSkuNumResponse> queryGmroiSkuNum(@RequestBody LegacyGmroiSessionRequest request) {
        String sessionId = resolveSessionId(request, "gmroi snapshot is preparing");
        return LegacyNodeResponse.ok(gmroiContributionService.getGmroiSkuNum(sessionId));
    }

    @PostMapping("/queryGmroiSkuPer")
    public LegacyNodeResponse<LegacyGmroiSkuPerResponse> queryGmroiSkuPer(@RequestBody LegacyGmroiSessionRequest request) {
        String sessionId = resolveSessionId(request, "gmroi snapshot is preparing");
        return LegacyNodeResponse.ok(gmroiContributionService.getGmroiSkuPer(sessionId));
    }

    @PostMapping("/queryGmroiSkuChange")
    public LegacyNodeResponse<LegacyGmroiSkuChangeResponse> queryGmroiSkuChange(@RequestBody LegacyGmroiSessionRequest request) {
        String sessionId = resolveSessionId(request, "gmroi snapshot is preparing");
        return LegacyNodeResponse.ok(gmroiContributionService.getGmroiSkuChange(sessionId));
    }

    @PostMapping("/queryGmroiSalesList")
    public LegacyNodeResponse<LegacyGmroiSalesListResponse> queryGmroiSalesList(@RequestBody LegacyGmroiSalesListRequest request) {
        String sessionId = resolveSessionId(request, "gmroi snapshot is preparing");
        return LegacyNodeResponse.ok(gmroiContributionService.getGmroiSalesList(
            sessionId,
            request.getStatus(),
            request.getPromotion(),
            request.getCurrentGmroi(),
            request.getCompareGmroi(),
            request.getGmroiList(),
            request.getPage(),
            request.getSize(),
            request.getOrder(),
            request.getOrderType()
        ));
    }

    private String resolveSessionId(LegacyGmroiSessionRequest request, String preparingMessage) {
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


package org.dromara.diagnosis.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.request.PriceBandConfigUpdateRequest;
import org.dromara.diagnosis.api.response.PriceBandConfigItemResponse;
import org.dromara.diagnosis.api.response.PriceBandConfigUpdateResponse;
import org.dromara.diagnosis.api.response.PriceBandDetailPageResponse;
import org.dromara.diagnosis.api.response.PriceBandDiagramResponse;
import org.dromara.diagnosis.api.response.PriceBandRangeSummaryResponse;
import org.dromara.diagnosis.application.service.PriceBandAnalysisService;
import org.dromara.diagnosis.application.service.PriceBandConfigService;
import org.dromara.diagnosis.common.model.DiagnosisApiResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/price-band")
public class PriceBandAnalysisController {

    private final PriceBandAnalysisService priceBandAnalysisService;
    private final PriceBandConfigService priceBandConfigService;

    @GetMapping("/diagram")
    public DiagnosisApiResponse<PriceBandDiagramResponse> getDiagram(@RequestParam("sessionId") String sessionId) {
        return DiagnosisApiResponse.ok(priceBandAnalysisService.getDiagram(sessionId), nextRequestId());
    }

    @GetMapping("/range-summary")
    public DiagnosisApiResponse<PriceBandRangeSummaryResponse> getRangeSummary(@RequestParam("sessionId") String sessionId,
                                                                               @RequestParam(value = "order", required = false) String order,
                                                                               @RequestParam(value = "orderType", required = false) String orderType) {
        return DiagnosisApiResponse.ok(priceBandAnalysisService.getRangeSummary(sessionId, order, orderType), nextRequestId());
    }

    @GetMapping("/details")
    public DiagnosisApiResponse<PriceBandDetailPageResponse> getDetails(@RequestParam("sessionId") String sessionId,
                                                                        @RequestParam(value = "status", required = false) List<String> status,
                                                                        @RequestParam(value = "promotion", required = false) String promotion,
                                                                        @RequestParam(value = "priceBandList", required = false) List<String> priceBandList,
                                                                        @RequestParam(value = "page", required = false) Integer page,
                                                                        @RequestParam(value = "size", required = false) Integer size,
                                                                        @RequestParam(value = "order", required = false) String order,
                                                                        @RequestParam(value = "orderType", required = false) String orderType) {
        return DiagnosisApiResponse.ok(
            priceBandAnalysisService.getDetails(sessionId, status, promotion, priceBandList, page, size, order, orderType),
            nextRequestId());
    }

    @GetMapping("/config")
    public DiagnosisApiResponse<List<PriceBandConfigItemResponse>> getConfig(@RequestParam("sessionId") String sessionId) {
        return DiagnosisApiResponse.ok(priceBandAnalysisService.getConfig(sessionId), nextRequestId());
    }

    @PostMapping("/config/update")
    public DiagnosisApiResponse<PriceBandConfigUpdateResponse> updateConfig(@Valid @RequestBody PriceBandConfigUpdateRequest request) {
        return DiagnosisApiResponse.ok(priceBandConfigService.updateConfig(request), nextRequestId());
    }

    private String nextRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

package org.dromara.diagnosis.api.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.response.VendorRankingResponse;
import org.dromara.diagnosis.api.response.VendorSalesShareItemResponse;
import org.dromara.diagnosis.api.response.VendorSummaryResponse;
import org.dromara.diagnosis.application.service.VendorAnalysisService;
import org.dromara.diagnosis.common.model.DiagnosisApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vendor-analysis")
public class VendorAnalysisController {

    private final VendorAnalysisService vendorAnalysisService;

    @GetMapping("/sales-share")
    public DiagnosisApiResponse<List<VendorSalesShareItemResponse>> getSalesShare(@RequestParam("sessionId") String sessionId) {
        return DiagnosisApiResponse.ok(vendorAnalysisService.getSalesShare(sessionId), nextRequestId());
    }

    @GetMapping("/summary")
    public DiagnosisApiResponse<VendorSummaryResponse> getSummary(@RequestParam("sessionId") String sessionId) {
        return DiagnosisApiResponse.ok(vendorAnalysisService.getSummary(sessionId), nextRequestId());
    }

    @GetMapping("/ranking")
    public DiagnosisApiResponse<VendorRankingResponse> getRanking(@RequestParam("sessionId") String sessionId,
                                                                  @RequestParam(value = "type", required = false) String type,
                                                                  @RequestParam(value = "page", required = false) Integer page,
                                                                  @RequestParam(value = "size", required = false) Integer size,
                                                                  @RequestParam(value = "orderType", required = false) String orderType) {
        return DiagnosisApiResponse.ok(vendorAnalysisService.getRanking(sessionId, type, page, size, orderType), nextRequestId());
    }

    private String nextRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

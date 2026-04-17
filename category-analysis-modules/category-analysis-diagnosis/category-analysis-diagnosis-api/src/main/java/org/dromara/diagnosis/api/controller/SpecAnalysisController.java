package org.dromara.diagnosis.api.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.response.SpecDetailPageResponse;
import org.dromara.diagnosis.api.response.SpecFilterOptionsResponse;
import org.dromara.diagnosis.api.response.SpecOverviewResponse;
import org.dromara.diagnosis.api.response.SpecRankingResponse;
import org.dromara.diagnosis.api.response.SpecSalesShareItemResponse;
import org.dromara.diagnosis.api.response.SpecSkuSalesChangeItemResponse;
import org.dromara.diagnosis.application.service.SpecAnalysisService;
import org.dromara.diagnosis.common.model.DiagnosisApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/spec-analysis")
public class SpecAnalysisController {

    private final SpecAnalysisService specAnalysisService;

    @GetMapping("/overview")
    public DiagnosisApiResponse<SpecOverviewResponse> getOverview(@RequestParam("sessionId") String sessionId) {
        return DiagnosisApiResponse.ok(specAnalysisService.getOverview(sessionId), nextRequestId());
    }

    @GetMapping("/sales-share")
    public DiagnosisApiResponse<List<SpecSalesShareItemResponse>> getSalesShare(@RequestParam("sessionId") String sessionId) {
        return DiagnosisApiResponse.ok(specAnalysisService.getSalesShare(sessionId), nextRequestId());
    }

    @GetMapping("/ranking")
    public DiagnosisApiResponse<SpecRankingResponse> getRanking(@RequestParam("sessionId") String sessionId,
                                                                @RequestParam(value = "type", required = false) String type,
                                                                @RequestParam(value = "page", required = false) Integer page,
                                                                @RequestParam(value = "size", required = false) Integer size,
                                                                @RequestParam(value = "orderType", required = false) String orderType) {
        return DiagnosisApiResponse.ok(specAnalysisService.getRanking(sessionId, type, page, size, orderType), nextRequestId());
    }

    @GetMapping("/sku-sales-change")
    public DiagnosisApiResponse<List<SpecSkuSalesChangeItemResponse>> getSkuSalesChange(@RequestParam("sessionId") String sessionId) {
        return DiagnosisApiResponse.ok(specAnalysisService.getSkuSalesChange(sessionId), nextRequestId());
    }

    @GetMapping("/list")
    public DiagnosisApiResponse<SpecDetailPageResponse> getSpecList(@RequestParam("sessionId") String sessionId,
                                                                    @RequestParam(value = "specTypeList", required = false) List<String> specTypeList,
                                                                    @RequestParam(value = "specList", required = false) List<String> specList,
                                                                    @RequestParam(value = "newSpecType", required = false) String newSpecType,
                                                                    @RequestParam(value = "page", required = false) Integer page,
                                                                    @RequestParam(value = "size", required = false) Integer size,
                                                                    @RequestParam(value = "order", required = false) String order,
                                                                    @RequestParam(value = "orderType", required = false) String orderType) {
        return DiagnosisApiResponse.ok(
            specAnalysisService.getSpecList(sessionId, specTypeList, specList, newSpecType, page, size, order, orderType),
            nextRequestId());
    }

    @GetMapping("/filter-options")
    public DiagnosisApiResponse<SpecFilterOptionsResponse> getFilterOptions(@RequestParam("sessionId") String sessionId) {
        return DiagnosisApiResponse.ok(specAnalysisService.getFilterOptions(sessionId), nextRequestId());
    }

    private String nextRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

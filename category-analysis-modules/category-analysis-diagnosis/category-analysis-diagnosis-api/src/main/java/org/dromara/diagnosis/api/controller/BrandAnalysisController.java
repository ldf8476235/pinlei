package org.dromara.diagnosis.api.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.response.BrandDetailPageResponse;
import org.dromara.diagnosis.api.response.BrandFilterOptionsResponse;
import org.dromara.diagnosis.api.response.BrandOverviewResponse;
import org.dromara.diagnosis.api.response.BrandRankingResponse;
import org.dromara.diagnosis.api.response.BrandSalesShareItemResponse;
import org.dromara.diagnosis.api.response.BrandSkuDetailPageResponse;
import org.dromara.diagnosis.api.response.BrandSkuSalesChangeItemResponse;
import org.dromara.diagnosis.application.service.BrandAnalysisService;
import org.dromara.diagnosis.common.model.DiagnosisApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/brand-analysis")
public class BrandAnalysisController {

    private final BrandAnalysisService brandAnalysisService;

    @GetMapping("/overview")
    public DiagnosisApiResponse<BrandOverviewResponse> getOverview(@RequestParam("sessionId") String sessionId) {
        return DiagnosisApiResponse.ok(brandAnalysisService.getOverview(sessionId), nextRequestId());
    }

    @GetMapping("/sales-share")
    public DiagnosisApiResponse<List<BrandSalesShareItemResponse>> getSalesShare(@RequestParam("sessionId") String sessionId) {
        return DiagnosisApiResponse.ok(brandAnalysisService.getSalesShare(sessionId), nextRequestId());
    }

    @GetMapping("/ranking")
    public DiagnosisApiResponse<BrandRankingResponse> getRanking(@RequestParam("sessionId") String sessionId,
                                                                 @RequestParam(value = "type", required = false) String type,
                                                                 @RequestParam(value = "page", required = false) Integer page,
                                                                 @RequestParam(value = "size", required = false) Integer size,
                                                                 @RequestParam(value = "orderType", required = false) String orderType) {
        return DiagnosisApiResponse.ok(brandAnalysisService.getRanking(sessionId, type, page, size, orderType), nextRequestId());
    }

    @GetMapping("/sku-sales-change")
    public DiagnosisApiResponse<List<BrandSkuSalesChangeItemResponse>> getSkuSalesChange(@RequestParam("sessionId") String sessionId) {
        return DiagnosisApiResponse.ok(brandAnalysisService.getSkuSalesChange(sessionId), nextRequestId());
    }

    @GetMapping("/list")
    public DiagnosisApiResponse<BrandDetailPageResponse> getBrandList(@RequestParam("sessionId") String sessionId,
                                                                      @RequestParam(value = "brandTypeList", required = false) List<String> brandTypeList,
                                                                      @RequestParam(value = "brandList", required = false) List<String> brandList,
                                                                      @RequestParam(value = "newBrandType", required = false) String newBrandType,
                                                                      @RequestParam(value = "page", required = false) Integer page,
                                                                      @RequestParam(value = "size", required = false) Integer size,
                                                                      @RequestParam(value = "order", required = false) String order,
                                                                      @RequestParam(value = "orderType", required = false) String orderType) {
        return DiagnosisApiResponse.ok(
            brandAnalysisService.getBrandList(sessionId, brandTypeList, brandList, newBrandType, page, size, order, orderType),
            nextRequestId());
    }

    @GetMapping("/sku-list")
    public DiagnosisApiResponse<BrandSkuDetailPageResponse> getBrandSkuList(@RequestParam("sessionId") String sessionId,
                                                                             @RequestParam(value = "brandList", required = false) List<String> brandList,
                                                                             @RequestParam(value = "status", required = false) List<String> status,
                                                                             @RequestParam(value = "promotion", required = false) String promotion,
                                                                             @RequestParam(value = "page", required = false) Integer page,
                                                                             @RequestParam(value = "size", required = false) Integer size,
                                                                             @RequestParam(value = "order", required = false) String order,
                                                                             @RequestParam(value = "orderType", required = false) String orderType) {
        return DiagnosisApiResponse.ok(
            brandAnalysisService.getBrandSkuList(sessionId, brandList, status, promotion, page, size, order, orderType),
            nextRequestId());
    }

    @GetMapping("/filter-options")
    public DiagnosisApiResponse<BrandFilterOptionsResponse> getFilterOptions(@RequestParam("sessionId") String sessionId) {
        return DiagnosisApiResponse.ok(brandAnalysisService.getFilterOptions(sessionId), nextRequestId());
    }

    private String nextRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

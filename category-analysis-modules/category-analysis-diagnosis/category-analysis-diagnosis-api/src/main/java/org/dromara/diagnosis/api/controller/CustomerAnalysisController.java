package org.dromara.diagnosis.api.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.response.CustomerAgeBucketResponse;
import org.dromara.diagnosis.api.response.CustomerSalesDetailsResponse;
import org.dromara.diagnosis.api.response.CustomerSalesRadarItemResponse;
import org.dromara.diagnosis.application.service.CustomerAnalysisService;
import org.dromara.diagnosis.common.model.DiagnosisApiResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 客户分析
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customer-analysis")
public class CustomerAnalysisController {

    private final CustomerAnalysisService customerAnalysisService;

    @GetMapping("/ages")
    public DiagnosisApiResponse<List<CustomerAgeBucketResponse>> getAgeBuckets(@RequestParam("sessionId") String sessionId) {
        return DiagnosisApiResponse.ok(customerAnalysisService.getAgeBuckets(sessionId), nextRequestId());
    }

    @GetMapping("/radar")
    public DiagnosisApiResponse<List<CustomerSalesRadarItemResponse>> getSalesRadar(@RequestParam("sessionId") String sessionId) {
        return DiagnosisApiResponse.ok(customerAnalysisService.getSalesRadar(sessionId), nextRequestId());
    }

    @GetMapping("/details")
    public DiagnosisApiResponse<CustomerSalesDetailsResponse> getSalesDetails(@RequestParam("sessionId") String sessionId,
                                                                              @RequestParam(value = "page", required = false) Integer page,
                                                                              @RequestParam(value = "size", required = false) Integer size,
                                                                              @RequestParam(value = "order", required = false) String order,
                                                                              @RequestParam(value = "orderType", required = false) String orderType) {
        return DiagnosisApiResponse.ok(customerAnalysisService.getSalesDetails(sessionId, page, size, order, orderType), nextRequestId());
    }

    private String nextRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

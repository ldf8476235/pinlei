package org.dromara.diagnosis.api.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.response.ChannelSalesDetailsResponse;
import org.dromara.diagnosis.api.response.ChannelSalesPieItemResponse;
import org.dromara.diagnosis.api.response.ChannelSalesTrendResponse;
import org.dromara.diagnosis.application.service.ChannelPerformanceService;
import org.dromara.diagnosis.common.model.DiagnosisApiResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/channel-performance")
public class ChannelPerformanceController {

    private final ChannelPerformanceService channelPerformanceService;

    @GetMapping("/pie")
    public DiagnosisApiResponse<List<ChannelSalesPieItemResponse>> getSalesPie(@RequestParam("sessionId") String sessionId) {
        return DiagnosisApiResponse.ok(channelPerformanceService.getSalesPie(sessionId), nextRequestId());
    }

    @GetMapping("/trend")
    public DiagnosisApiResponse<ChannelSalesTrendResponse> getSalesTrend(@RequestParam("sessionId") String sessionId) {
        return DiagnosisApiResponse.ok(channelPerformanceService.getSalesTrend(sessionId), nextRequestId());
    }

    @GetMapping("/details")
    public DiagnosisApiResponse<ChannelSalesDetailsResponse> getSalesDetails(@RequestParam("sessionId") String sessionId,
                                                                              @RequestParam(value = "page", required = false) Integer page,
                                                                              @RequestParam(value = "size", required = false) Integer size,
                                                                              @RequestParam(value = "order", required = false) String order,
                                                                              @RequestParam(value = "orderType", required = false) String orderType) {
        return DiagnosisApiResponse.ok(channelPerformanceService.getSalesDetails(sessionId, page, size, order, orderType), nextRequestId());
    }

    private String nextRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

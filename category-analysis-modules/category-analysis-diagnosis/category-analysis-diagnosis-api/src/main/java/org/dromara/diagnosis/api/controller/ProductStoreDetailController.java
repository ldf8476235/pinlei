package org.dromara.diagnosis.api.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.response.ProductStoreDetailPageResponse;
import org.dromara.diagnosis.application.service.ProductStoreDetailService;
import org.dromara.diagnosis.common.model.DiagnosisApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/product-store")
public class ProductStoreDetailController {

    private final ProductStoreDetailService productStoreDetailService;

    @GetMapping("/details")
    public DiagnosisApiResponse<ProductStoreDetailPageResponse> getStoreDetails(@RequestParam("sessionId") String sessionId,
                                                                                @RequestParam("productNo") String productNo,
                                                                                @RequestParam(value = "status", required = false) List<String> status,
                                                                                @RequestParam(value = "storeKeyword", required = false) String storeKeyword,
                                                                                @RequestParam(value = "page", required = false) Integer page,
                                                                                @RequestParam(value = "size", required = false) Integer size,
                                                                                @RequestParam(value = "order", required = false) String order,
                                                                                @RequestParam(value = "orderType", required = false) String orderType) {
        String requestId = nextRequestId();
        return DiagnosisApiResponse.ok(
            productStoreDetailService.getStoreDetails(requestId, sessionId, productNo, status, storeKeyword, page, size, order, orderType),
            requestId);
    }

    private String nextRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

package org.dromara.diagnosis.api.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.request.AllClassCheckListRequest;
import org.dromara.diagnosis.api.request.AllClassCheckRequest;
import org.dromara.diagnosis.api.response.AllClassCheckSalesChangeResponse;
import org.dromara.diagnosis.api.response.AllClassCheckScatterResponse;
import org.dromara.diagnosis.api.response.AllClassCheckSkuDifferResponse;
import org.dromara.diagnosis.api.response.AllClassCheckSkuResponse;
import org.dromara.diagnosis.application.service.AllClassCheckService;
import org.dromara.diagnosis.common.model.DiagnosisApiResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/salesStoreClass")
public class AllClassCheckController {

    private final AllClassCheckService allClassCheckService;

    @PostMapping("/classSalesChange")
    public DiagnosisApiResponse<AllClassCheckSalesChangeResponse> salesChange(@RequestBody AllClassCheckListRequest request) {
        return DiagnosisApiResponse.ok(allClassCheckService.getSalesChange(request), nextRequestId());
    }

    @PostMapping("/allClassCheck")
    public DiagnosisApiResponse<AllClassCheckScatterResponse> scatter(@RequestBody AllClassCheckRequest request) {
        return DiagnosisApiResponse.ok(allClassCheckService.getAllClassCheck(request), nextRequestId());
    }

    @PostMapping("/findClassSku")
    public DiagnosisApiResponse<AllClassCheckSkuResponse> sku(@RequestBody AllClassCheckRequest request) {
        return DiagnosisApiResponse.ok(allClassCheckService.getFindClassSku(request), nextRequestId());
    }

    @PostMapping("/findClassSkuDiffer")
    public DiagnosisApiResponse<AllClassCheckSkuDifferResponse> skuDiffer(@RequestBody AllClassCheckRequest request) {
        return DiagnosisApiResponse.ok(allClassCheckService.getFindClassSkuDiffer(request), nextRequestId());
    }

    private String nextRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

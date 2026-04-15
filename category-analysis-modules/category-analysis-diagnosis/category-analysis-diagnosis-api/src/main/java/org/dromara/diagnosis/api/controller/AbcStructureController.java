package org.dromara.diagnosis.api.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.request.AbcParamUpdateRequest;
import org.dromara.diagnosis.api.response.AbcDetailsItemResponse;
import org.dromara.diagnosis.api.response.AbcImageItemResponse;
import org.dromara.diagnosis.api.response.AbcMatrixResponse;
import org.dromara.diagnosis.api.response.AbcParamUpdateResponse;
import org.dromara.diagnosis.api.response.AbcSalesListResponse;
import org.dromara.diagnosis.api.response.AbcTypeParamResponse;
import org.dromara.diagnosis.application.service.AbcStructureService;
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
@RequestMapping("/api/v1/abc-structure")
public class AbcStructureController {

    private final AbcStructureService abcStructureService;

    @GetMapping("/params")
    public DiagnosisApiResponse<List<AbcTypeParamResponse>> getParams(@RequestParam("sessionId") String sessionId) {
        return DiagnosisApiResponse.ok(abcStructureService.getAbcParams(sessionId), nextRequestId());
    }

    @PostMapping("/params/update")
    public DiagnosisApiResponse<AbcParamUpdateResponse> updateParams(@RequestBody AbcParamUpdateRequest request) {
        return DiagnosisApiResponse.ok(abcStructureService.updateAbcParams(request), nextRequestId());
    }

    @GetMapping("/image")
    public DiagnosisApiResponse<List<AbcImageItemResponse>> getImage(@RequestParam("sessionId") String sessionId,
                                                                     @RequestParam("abcType") String abcType) {
        return DiagnosisApiResponse.ok(abcStructureService.getAbcImage(sessionId, abcType), nextRequestId());
    }

    @GetMapping("/matrix")
    public DiagnosisApiResponse<AbcMatrixResponse> getMatrix(@RequestParam("sessionId") String sessionId,
                                                             @RequestParam("abcType") String abcType) {
        return DiagnosisApiResponse.ok(abcStructureService.getAbcMatrix(sessionId, abcType), nextRequestId());
    }

    @GetMapping("/details")
    public DiagnosisApiResponse<List<AbcDetailsItemResponse>> getDetails(@RequestParam("sessionId") String sessionId,
                                                                         @RequestParam("abcType") String abcType) {
        return DiagnosisApiResponse.ok(abcStructureService.getAbcDetails(sessionId, abcType), nextRequestId());
    }

    @GetMapping("/sales-list")
    public DiagnosisApiResponse<AbcSalesListResponse> getSalesList(@RequestParam("sessionId") String sessionId,
                                                                   @RequestParam("abcType") String abcType,
                                                                   @RequestParam(value = "status", required = false) List<String> status,
                                                                   @RequestParam(value = "promotion", required = false) String promotion,
                                                                   @RequestParam(value = "currentAbc", required = false) String currentAbc,
                                                                   @RequestParam(value = "compareAbc", required = false) String compareAbc,
                                                                   @RequestParam(value = "page", required = false) Integer page,
                                                                   @RequestParam(value = "size", required = false) Integer size,
                                                                   @RequestParam(value = "order", required = false) String order,
                                                                   @RequestParam(value = "orderType", required = false) String orderType) {
        return DiagnosisApiResponse.ok(
            abcStructureService.getAbcSalesList(sessionId, abcType, status, promotion, currentAbc, compareAbc, page, size, order, orderType),
            nextRequestId()
        );
    }

    private String nextRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

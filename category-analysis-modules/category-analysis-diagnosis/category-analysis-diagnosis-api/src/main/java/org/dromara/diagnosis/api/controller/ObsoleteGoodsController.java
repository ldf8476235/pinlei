package org.dromara.diagnosis.api.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.request.ObsoleteGoodsListRequest;
import org.dromara.diagnosis.api.response.ObsoleteGoodsListResponse;
import org.dromara.diagnosis.application.service.ObsoleteGoodsService;
import org.dromara.diagnosis.common.model.DiagnosisApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/obsolete-goods")
public class ObsoleteGoodsController {

    private final ObsoleteGoodsService obsoleteGoodsService;

    @PostMapping("/list")
    public DiagnosisApiResponse<ObsoleteGoodsListResponse> queryObsoleteList(@RequestBody ObsoleteGoodsListRequest request) {
        return DiagnosisApiResponse.ok(obsoleteGoodsService.queryObsoleteList(request), nextRequestId());
    }

    private String nextRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

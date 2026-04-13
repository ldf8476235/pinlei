package org.dromara.diagnosis.api.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.request.StoreFindRequest;
import org.dromara.diagnosis.api.response.CategoryTreeResponse;
import org.dromara.diagnosis.api.response.StoreOptionResponse;
import org.dromara.diagnosis.application.service.StoreService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 门店接口.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/store")
public class StoreController {

    private final StoreService storeService;

    @PostMapping("/findStore")
    public CategoryTreeResponse<List<StoreOptionResponse>> findStore(@RequestBody(required = false) StoreFindRequest request) {
        return CategoryTreeResponse.ok(storeService.findStore(request));
    }
}


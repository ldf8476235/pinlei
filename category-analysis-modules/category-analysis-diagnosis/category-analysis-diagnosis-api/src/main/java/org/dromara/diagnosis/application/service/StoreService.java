package org.dromara.diagnosis.application.service;

import org.dromara.diagnosis.api.request.StoreFindRequest;
import org.dromara.diagnosis.api.response.StoreOptionResponse;

import java.util.List;

/**
 * 门店查询服务.
 */
public interface StoreService {

    List<StoreOptionResponse> findStore(StoreFindRequest request);
}


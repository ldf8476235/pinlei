package org.dromara.diagnosis.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.request.StoreFindRequest;
import org.dromara.diagnosis.api.response.StoreOptionResponse;
import org.dromara.diagnosis.application.service.StoreService;
import org.dromara.diagnosis.infrastructure.mapper.CategoryTreeMapper;
import org.dromara.diagnosis.infrastructure.model.CategoryTreeQueryParam;
import org.dromara.diagnosis.infrastructure.model.StoreBaseRow;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 门店查询服务实现.
 */
@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private static final int DEFAULT_LIMIT = 20;

    private static final int MAX_LIMIT = 200;

    private final CategoryTreeMapper categoryTreeMapper;

    @Override
    public List<StoreOptionResponse> findStore(StoreFindRequest request) {
        StoreFindRequest actual = request == null ? new StoreFindRequest() : request;

        CategoryTreeQueryParam param = new CategoryTreeQueryParam();
        param.setRetailTypeId(normalize(actual.getRetailTypeId()));
        param.setBusinessCircleId(normalize(actual.getBusinessCircleId()));
        param.setDeptGroupId(normalize(actual.getDeptGroupId()));
        param.setKeyword(normalize(actual.getKeyword()));
        param.setLimit(normalizeLimit(actual.getLimit()));

        List<StoreBaseRow> rows = categoryTreeMapper.selectStores(param);
        return rows.stream().map(this::toResponse).toList();
    }

    private StoreOptionResponse toResponse(StoreBaseRow row) {
        StoreOptionResponse response = new StoreOptionResponse();
        response.setStoreNo(row.getStoreNo());
        response.setStoreName(row.getStoreName());
        return response;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        return text.isEmpty() ? null : text;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        return Math.max(1, Math.min(limit, MAX_LIMIT));
    }
}


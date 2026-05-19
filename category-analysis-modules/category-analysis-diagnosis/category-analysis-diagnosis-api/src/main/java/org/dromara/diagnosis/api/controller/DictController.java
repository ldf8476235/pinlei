package org.dromara.diagnosis.api.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.response.CategoryTreeResponse;
import org.dromara.diagnosis.api.response.DictDetailResponse;
import org.dromara.diagnosis.infrastructure.mapper.CategoryTreeMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisDictRow;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dict")
public class DictController {

    private static final String DICT_TYPE_CATEGORY_ROLE = "class_role_type";

    private final CategoryTreeMapper categoryTreeMapper;

    @RequestMapping(value = "/findClassTreeRoleType", method = {RequestMethod.GET, RequestMethod.POST})
    public CategoryTreeResponse<List<DictDetailResponse>> findClassTreeRoleType() {
        List<DiagnosisDictRow> rows = categoryTreeMapper.selectDictRowsByType(DICT_TYPE_CATEGORY_ROLE);
        return CategoryTreeResponse.ok(rows == null ? List.of() : rows.stream().map(this::toResponse).toList());
    }

    private DictDetailResponse toResponse(DiagnosisDictRow row) {
        DictDetailResponse response = new DictDetailResponse();
        response.setCreateTime(row.getCreateTime());
        response.setDictId(row.getDictId());
        response.setId(row.getDictCode());
        response.setLabel(row.getDictLabel());
        response.setSort(row.getDictSort() == null ? null : String.valueOf(row.getDictSort()));
        response.setValue(row.getDictValue());
        return response;
    }
}

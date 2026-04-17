package org.dromara.diagnosis.api.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.response.TagDetailPageResponse;
import org.dromara.diagnosis.api.response.TagSalesPerResponse;
import org.dromara.diagnosis.api.response.TagTypeGroupResponse;
import org.dromara.diagnosis.application.service.TagAnalysisService;
import org.dromara.diagnosis.common.model.DiagnosisApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tag-analysis")
public class TagAnalysisController {

    private final TagAnalysisService tagAnalysisService;

    @GetMapping("/types")
    public DiagnosisApiResponse<List<TagTypeGroupResponse>> getTypes(@RequestParam("sessionId") String sessionId) {
        return DiagnosisApiResponse.ok(tagAnalysisService.getTypes(sessionId), nextRequestId());
    }

    @GetMapping("/sales-share")
    public DiagnosisApiResponse<TagSalesPerResponse> getSalesShare(@RequestParam("sessionId") String sessionId,
                                                                   @RequestParam("tagType") String tagType) {
        return DiagnosisApiResponse.ok(tagAnalysisService.getSalesShare(sessionId, tagType), nextRequestId());
    }

    @GetMapping("/list")
    public DiagnosisApiResponse<TagDetailPageResponse> getTagList(@RequestParam("sessionId") String sessionId,
                                                                  @RequestParam("tagType") String tagType,
                                                                  @RequestParam(value = "tagList", required = false) List<String> tagList,
                                                                  @RequestParam(value = "page", required = false) Integer page,
                                                                  @RequestParam(value = "size", required = false) Integer size,
                                                                  @RequestParam(value = "order", required = false) String order,
                                                                  @RequestParam(value = "orderType", required = false) String orderType) {
        return DiagnosisApiResponse.ok(
            tagAnalysisService.getTagList(sessionId, tagType, tagList, page, size, order, orderType),
            nextRequestId());
    }

    private String nextRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

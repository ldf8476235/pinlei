package org.dromara.diagnosis.api.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.request.PrecomputeJobCreateRequest;
import org.dromara.diagnosis.api.response.PrecomputeJobProgressResponse;
import org.dromara.diagnosis.api.response.PrecomputeJobResponse;
import org.dromara.diagnosis.api.response.PrecomputeEventResponse;
import org.dromara.diagnosis.api.response.PrecomputeWindowResponse;
import org.dromara.diagnosis.application.service.PrecomputeJobService;
import org.dromara.diagnosis.common.model.DiagnosisApiResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 预计算任务中心接口.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/precompute/jobs")
public class PrecomputeJobController {

    private final PrecomputeJobService precomputeJobService;

    @PostMapping
    public DiagnosisApiResponse<PrecomputeJobResponse> createJob(@RequestBody(required = false) PrecomputeJobCreateRequest request) {
        PrecomputeJobCreateRequest actual = request == null ? new PrecomputeJobCreateRequest() : request;
        return DiagnosisApiResponse.ok(precomputeJobService.createJob(actual), nextRequestId());
    }

    @GetMapping("/{jobId}")
    public DiagnosisApiResponse<PrecomputeJobResponse> getJob(@PathVariable("jobId") Long jobId) {
        return DiagnosisApiResponse.ok(precomputeJobService.getJob(jobId), nextRequestId());
    }

    @GetMapping("/{jobId}/progress")
    public DiagnosisApiResponse<PrecomputeJobProgressResponse> getJobProgress(@PathVariable("jobId") Long jobId) {
        return DiagnosisApiResponse.ok(precomputeJobService.getJobProgress(jobId), nextRequestId());
    }

    @GetMapping("/{jobId}/windows")
    public DiagnosisApiResponse<List<PrecomputeWindowResponse>> getJobWindows(@PathVariable("jobId") Long jobId) {
        return DiagnosisApiResponse.ok(precomputeJobService.getJobWindows(jobId), nextRequestId());
    }

    @GetMapping("/{jobId}/events")
    public DiagnosisApiResponse<List<PrecomputeEventResponse>> getJobEvents(@PathVariable("jobId") Long jobId,
                                                                             @RequestParam(value = "limit", required = false) Integer limit) {
        return DiagnosisApiResponse.ok(precomputeJobService.getJobEvents(jobId, limit), nextRequestId());
    }

    @PostMapping("/{jobId}/stop")
    public DiagnosisApiResponse<Void> stopJob(@PathVariable("jobId") Long jobId) {
        precomputeJobService.stopJob(jobId);
        return DiagnosisApiResponse.ok(null, nextRequestId());
    }

    @PostMapping("/{jobId}/retry")
    public DiagnosisApiResponse<Void> retryJob(@PathVariable("jobId") Long jobId) {
        precomputeJobService.retryJob(jobId);
        return DiagnosisApiResponse.ok(null, nextRequestId());
    }

    private String nextRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

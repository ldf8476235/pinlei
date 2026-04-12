package org.dromara.diagnosis.application.service;

import org.dromara.diagnosis.api.request.PrecomputeJobCreateRequest;
import org.dromara.diagnosis.api.response.PrecomputeJobProgressResponse;
import org.dromara.diagnosis.api.response.PrecomputeJobResponse;
import org.dromara.diagnosis.api.response.PrecomputeEventResponse;
import org.dromara.diagnosis.api.response.PrecomputeWindowResponse;

import java.util.List;

/**
 * 预计算任务服务.
 */
public interface PrecomputeJobService {

    PrecomputeJobResponse createJob(PrecomputeJobCreateRequest request);

    PrecomputeJobResponse getJob(Long jobId);

    PrecomputeJobProgressResponse getJobProgress(Long jobId);

    List<PrecomputeWindowResponse> getJobWindows(Long jobId);

    List<PrecomputeEventResponse> getJobEvents(Long jobId, Integer limit);

    void stopJob(Long jobId);

    void retryJob(Long jobId);
}

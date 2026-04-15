package org.dromara.diagnosis.application.service;

import lombok.RequiredArgsConstructor;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.diagnosis.api.response.PrecomputeJobProgressResponse;
import org.dromara.diagnosis.application.config.DiagnosisCacheProperties;
import org.dromara.diagnosis.application.config.DiagnosisRedisKeyPrefixProperties;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 预计算任务进度缓存服务.
 */
@Service
@RequiredArgsConstructor
public class DiagnosisProgressCacheService {

    private final DiagnosisCacheProperties cacheProperties;

    private final DiagnosisRedisKeyPrefixProperties keyPrefixProperties;

    public void save(PrecomputeJobProgressResponse progress) {
        if (progress == null || progress.getJobId() == null) {
            return;
        }
        RedisUtils.setCacheObject(cacheKey(progress.getJobId()),
            JsonUtils.toJsonString(progress),
            Duration.ofMinutes(Math.max(1, cacheProperties.getProgressTtlMinutes())));
    }

    public void saveFromJobRow(DiagnosisPrecomputeJobRow row) {
        if (row == null || row.getJobId() == null) {
            return;
        }
        PrecomputeJobProgressResponse response = new PrecomputeJobProgressResponse();
        response.setJobId(row.getJobId());
        response.setStatus(row.getStatusCode());
        response.setProgressPercent(row.getProgressPercent());
        response.setCurrentStage(row.getCurrentStage());
        response.setWindowDone(row.getDoneWindows());
        response.setTotalWindow(row.getTotalWindows());
        response.setRowsRead(row.getRowsRead());
        response.setRowsWritten(row.getRowsWritten());
        response.setOrchestratorStatus(row.getOrchestratorStatus());
        response.setModuleProgressJson(row.getModuleProgressJson());
        save(response);
    }

    public PrecomputeJobProgressResponse get(Long jobId) {
        if (jobId == null) {
            return null;
        }
        String value = RedisUtils.getCacheObject(cacheKey(jobId));
        if (value == null || value.isBlank()) {
            return null;
        }
        return JsonUtils.parseObject(value, PrecomputeJobProgressResponse.class);
    }

    public void remove(Long jobId) {
        if (jobId == null) {
            return;
        }
        RedisUtils.deleteObject(cacheKey(jobId));
    }

    private String cacheKey(Long jobId) {
        return keyPrefixProperties.getJobProgress() + ":" + jobId;
    }
}

package org.dromara.diagnosis.application.service;

import lombok.RequiredArgsConstructor;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.diagnosis.api.request.DiagnosisSessionCreateRequest;
import org.dromara.diagnosis.api.request.PrecomputeJobCreateRequest;
import org.dromara.diagnosis.api.response.DiagnosisOverviewResponse;
import org.dromara.diagnosis.api.response.DiagnosisSessionCreateResponse;
import org.dromara.diagnosis.api.response.DiagnosisTrendsResponse;
import org.dromara.diagnosis.application.config.DiagnosisCacheProperties;
import org.dromara.diagnosis.application.config.DiagnosisRedisKeyPrefixProperties;
import org.dromara.diagnosis.application.model.DiagnosisSessionCacheModel;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisOverviewSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisTrendSnapshotRow;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * 诊断会话服务.
 */
@Service
@RequiredArgsConstructor
public class DiagnosisSessionService {

    private static final String TENANT_ID = "000000";

    private final DiagnosisSnapshotMapper snapshotMapper;

    private final DiagnosisQueryHashService queryHashService;

    private final PrecomputeJobService precomputeJobService;

    private final DiagnosisCacheProperties cacheProperties;

    private final DiagnosisRedisKeyPrefixProperties keyPrefixProperties;

    public DiagnosisSessionCreateResponse createSession(DiagnosisSessionCreateRequest request) {
        String queryHash = queryHashService.buildQueryHash(request);
        DiagnosisOverviewSnapshotRow overview = snapshotMapper.selectLatestOverviewByQuery(TENANT_ID, queryHash);
        String sessionId = "S-" + UUID.randomUUID().toString().replace("-", "");
        Long triggeredJobId = null;
        String source = "SNAPSHOT_HIT";

        boolean triggerIfMissing = request.getTriggerIfMissing() == null || Boolean.TRUE.equals(request.getTriggerIfMissing());
        int waitSeconds = request.getWaitSeconds() == null ? 0 : Math.max(0, request.getWaitSeconds());

        if (overview == null && triggerIfMissing) {
            PrecomputeJobCreateRequest jobRequest = new PrecomputeJobCreateRequest();
            jobRequest.setModule("DIAGNOSIS");
            jobRequest.setReadRangeType("CUSTOM");
            jobRequest.setReadStart(request.getPeriodStart());
            jobRequest.setReadEnd(request.getPeriodEnd());
            jobRequest.setCompareStart(request.getCompareStart());
            jobRequest.setCompareEnd(request.getCompareEnd());
            jobRequest.setWindowTypes(request.getCompareStart() == null ? "MONTH" : "YOY");
            jobRequest.setForceRebuild(Boolean.FALSE);
            jobRequest.setPriority(5);
            jobRequest.setRequestJson(request.getExtraFilterJson());
            triggeredJobId = precomputeJobService.createJob(jobRequest).getJobId();
            source = "TRIGGERED";

            for (int i = 0; i < waitSeconds; i++) {
                sleepOneSecond();
                overview = snapshotMapper.selectLatestOverviewByQuery(TENANT_ID, queryHash);
                if (overview != null) {
                    source = "WAIT_READY";
                    break;
                }
            }
        }

        DiagnosisSessionCacheModel cacheModel = new DiagnosisSessionCacheModel();
        cacheModel.setSessionId(sessionId);
        cacheModel.setQueryHash(queryHash);
        cacheModel.setDataVersion(overview == null ? null : overview.getDataVersion());
        RedisUtils.setCacheObject(sessionKey(sessionId), JsonUtils.toJsonString(cacheModel),
            Duration.ofMinutes(Math.max(1, cacheProperties.getResultTtlMinutes())));

        DiagnosisSessionCreateResponse response = new DiagnosisSessionCreateResponse();
        response.setSessionId(sessionId);
        response.setQueryHash(queryHash);
        response.setDataVersion(overview == null ? null : overview.getDataVersion());
        response.setCacheHit(overview != null);
        response.setReady(overview != null);
        response.setTriggeredJobId(triggeredJobId);
        response.setSource(source);
        return response;
    }

    public DiagnosisOverviewResponse getOverview(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        DiagnosisOverviewSnapshotRow overview = snapshotMapper.selectLatestOverviewByQuery(TENANT_ID, session.getQueryHash());
        if (overview == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "会话对应结果不存在，请先触发预计算");
        }
        DiagnosisOverviewResponse response = new DiagnosisOverviewResponse();
        response.setSessionId(sessionId);
        response.setDataVersion(overview.getDataVersion());
        response.setCacheHit(Boolean.TRUE);
        response.setOverview(overview);
        return response;
    }

    public DiagnosisTrendsResponse getTrends(String sessionId, String metricCode) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        DiagnosisOverviewSnapshotRow overview = snapshotMapper.selectLatestOverviewByQuery(TENANT_ID, session.getQueryHash());
        List<DiagnosisTrendSnapshotRow> trends = snapshotMapper.selectLatestTrendsByQuery(TENANT_ID, session.getQueryHash(), metricCode);
        DiagnosisTrendsResponse response = new DiagnosisTrendsResponse();
        response.setSessionId(sessionId);
        response.setMetricCode(metricCode);
        response.setDataVersion(overview == null ? session.getDataVersion() : overview.getDataVersion());
        response.setCacheHit(Boolean.TRUE);
        response.setTrends(trends);
        return response;
    }

    private DiagnosisSessionCacheModel getSession(String sessionId) {
        String text = RedisUtils.getCacheObject(sessionKey(sessionId));
        if (text == null || text.isBlank()) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "会话不存在或已过期");
        }
        DiagnosisSessionCacheModel model = JsonUtils.parseObject(text, DiagnosisSessionCacheModel.class);
        if (model == null || model.getQueryHash() == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "会话数据损坏");
        }
        return model;
    }

    private void sleepOneSecond() {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String sessionKey(String sessionId) {
        return keyPrefixProperties.getResultQuery() + ":session:" + sessionId;
    }
}


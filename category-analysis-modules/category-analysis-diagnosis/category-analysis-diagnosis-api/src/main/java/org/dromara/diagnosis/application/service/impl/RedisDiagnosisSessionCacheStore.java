package org.dromara.diagnosis.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.diagnosis.application.config.DiagnosisRedisKeyPrefixProperties;
import org.dromara.diagnosis.application.model.DiagnosisSessionCacheModel;
import org.dromara.diagnosis.application.service.DiagnosisSessionCacheStore;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisDiagnosisSessionCacheStore implements DiagnosisSessionCacheStore {

    private final DiagnosisRedisKeyPrefixProperties keyPrefixProperties;

    @Override
    public void save(String sessionId, DiagnosisSessionCacheModel session, Duration ttl) {
        RedisUtils.setCacheObject(sessionKey(sessionId), JsonUtils.toJsonString(session), ttl);
    }

    @Override
    public DiagnosisSessionCacheModel get(String sessionId) {
        String text = RedisUtils.getCacheObject(sessionKey(sessionId));
        if (text == null || text.isBlank()) {
            return null;
        }
        return JsonUtils.parseObject(text, DiagnosisSessionCacheModel.class);
    }

    private String sessionKey(String sessionId) {
        return keyPrefixProperties.getResultQuery() + ":session:" + sessionId;
    }
}


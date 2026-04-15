package org.dromara.diagnosis.application.service;

import org.dromara.diagnosis.application.model.DiagnosisSessionCacheModel;

import java.time.Duration;

/**
 * Session cache store abstraction for diagnosis query flow.
 */
public interface DiagnosisSessionCacheStore {

    void save(String sessionId, DiagnosisSessionCacheModel session, Duration ttl);

    DiagnosisSessionCacheModel get(String sessionId);

    DiagnosisSessionCacheModel getByQueryHash(String queryHash);
}

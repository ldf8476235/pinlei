package org.dromara.diagnosis.cache.constant;

/**
 * Redis key patterns used by diagnosis module.
 */
public interface DiagnosisRedisKeyConstants {

    String JOB_PROGRESS = "diag:job:progress:%s";
    String RESULT_QUERY = "diag:result:query:%s";
    String WINDOW_LOCK = "diag:lock:window:%s";

    static String jobProgressKey(String jobId) {
        return JOB_PROGRESS.formatted(jobId);
    }

    static String resultQueryKey(String hash) {
        return RESULT_QUERY.formatted(hash);
    }

    static String windowLockKey(String bizKey) {
        return WINDOW_LOCK.formatted(bizKey);
    }
}

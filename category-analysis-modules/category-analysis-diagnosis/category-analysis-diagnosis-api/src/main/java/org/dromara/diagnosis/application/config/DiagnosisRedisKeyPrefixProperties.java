package org.dromara.diagnosis.application.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 品类诊断 Redis key 前缀配置.
 */
@Data
@Component
@ConfigurationProperties(prefix = "diagnosis.redis.keyPrefix")
public class DiagnosisRedisKeyPrefixProperties {

    private String jobProgress = "diag:job:progress";

    private String resultQuery = "diag:result:query";

    private String windowLock = "diag:lock:window";
}

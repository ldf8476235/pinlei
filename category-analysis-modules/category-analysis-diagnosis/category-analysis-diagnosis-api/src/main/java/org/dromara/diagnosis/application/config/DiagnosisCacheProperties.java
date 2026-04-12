package org.dromara.diagnosis.application.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 品类诊断缓存配置.
 */
@Data
@Component
@ConfigurationProperties(prefix = "diagnosis.cache")
public class DiagnosisCacheProperties {

    private int resultTtlMinutes = 15;

    private int emptyResultTtlSeconds = 60;

    private int progressTtlMinutes = 120;
}


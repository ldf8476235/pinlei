package org.dromara.diagnosis.cache.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cache TTL options for diagnosis query and progress data.
 */
@Component
@ConfigurationProperties(prefix = "diagnosis.cache")
public class DiagnosisCacheProperties {

    private int resultTtlMinutes = 15;
    private int emptyResultTtlSeconds = 60;
    private int progressTtlMinutes = 120;

    public int getResultTtlMinutes() {
        return resultTtlMinutes;
    }

    public void setResultTtlMinutes(int resultTtlMinutes) {
        this.resultTtlMinutes = resultTtlMinutes;
    }

    public int getEmptyResultTtlSeconds() {
        return emptyResultTtlSeconds;
    }

    public void setEmptyResultTtlSeconds(int emptyResultTtlSeconds) {
        this.emptyResultTtlSeconds = emptyResultTtlSeconds;
    }

    public int getProgressTtlMinutes() {
        return progressTtlMinutes;
    }

    public void setProgressTtlMinutes(int progressTtlMinutes) {
        this.progressTtlMinutes = progressTtlMinutes;
    }
}

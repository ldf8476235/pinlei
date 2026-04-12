package org.dromara.diagnosis.job.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Batch tuning options for diagnosis precompute jobs.
 */
@Component
@ConfigurationProperties(prefix = "diagnosis.batch")
public class DiagnosisBatchProperties {

    private int fetchSize = 4000;
    private int chunkSize = 2000;
    private int parallelShards = 8;

    public int getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getParallelShards() {
        return parallelShards;
    }

    public void setParallelShards(int parallelShards) {
        this.parallelShards = parallelShards;
    }
}

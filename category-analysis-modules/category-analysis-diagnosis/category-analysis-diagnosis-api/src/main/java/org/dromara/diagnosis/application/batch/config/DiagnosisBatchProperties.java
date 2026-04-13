package org.dromara.diagnosis.application.batch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 诊断批处理配置.
 */
@Data
@Component
@ConfigurationProperties(prefix = "diagnosis.batch")
public class DiagnosisBatchProperties {

    private int fetchSize = 4000;

    private int chunkSize = 2000;

    private int parallelShards = 8;
}

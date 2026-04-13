package org.dromara.diagnosis.application.batch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 诊断批处理线程池配置.
 */
@Configuration
public class DiagnosisBatchExecutorConfig {

    @Bean(name = "diagnosisBatchTaskExecutor")
    public TaskExecutor diagnosisBatchTaskExecutor(DiagnosisBatchProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int poolSize = Math.max(2, properties.getParallelShards());
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setQueueCapacity(poolSize * 2);
        executor.setThreadNamePrefix("diag-batch-");
        executor.initialize();
        return executor;
    }
}

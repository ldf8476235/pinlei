package org.dromara.diagnosis.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class DiagnosisAsyncExecutorConfig {

    @Bean(name = "diagnosisOrchestratorExecutor")
    public Executor diagnosisOrchestratorExecutor() {
        return new ThreadPoolExecutor(
            4,
            8,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(200),
            r -> {
                Thread t = new Thread(r);
                t.setName("diag-orch-" + t.getId());
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    @Bean(name = "diagnosisComputeExecutor")
    public Executor diagnosisComputeExecutor() {
        return new ThreadPoolExecutor(
            8,
            16,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(500),
            r -> {
                Thread t = new Thread(r);
                t.setName("diag-compute-" + t.getId());
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}

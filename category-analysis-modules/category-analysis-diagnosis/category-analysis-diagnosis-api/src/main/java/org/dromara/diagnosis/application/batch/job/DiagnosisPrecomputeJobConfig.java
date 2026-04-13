package org.dromara.diagnosis.application.batch.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 诊断预计算批处理作业配置.
 */
@Configuration
@RequiredArgsConstructor
public class DiagnosisPrecomputeJobConfig {

    private final JobRepository jobRepository;

    private final PlatformTransactionManager transactionManager;

    private final Partitioner diagnosisIdRangePartitioner;

    private final DiagnosisPrecomputeJobListener diagnosisPrecomputeJobListener;

    @Bean
    public Job diagnosisSingleMonthPrecomputeJob(Step diagnosisPartitionManagerStep,
                                                 Step diagnosisFinalizeStep) {
        return new JobBuilder(DiagnosisBatchJobNames.SINGLE_MONTH_PRECOMPUTE_JOB, jobRepository)
            .listener(diagnosisPrecomputeJobListener)
            .start(diagnosisPartitionManagerStep)
            .next(diagnosisFinalizeStep)
            .build();
    }

    @Bean
    public Step diagnosisPartitionManagerStep(Step diagnosisShardWorkerStep,
                                              TaskExecutor diagnosisBatchTaskExecutor) {
        return new StepBuilder("diagnosisPartitionManagerStep", jobRepository)
            .partitioner("diagnosisShardWorkerStep", diagnosisIdRangePartitioner)
            .step(diagnosisShardWorkerStep)
            .gridSize(8)
            .taskExecutor(diagnosisBatchTaskExecutor)
            .build();
    }

    @Bean
    public Step diagnosisShardWorkerStep(DiagnosisShardWorkerTasklet diagnosisShardWorkerTasklet) {
        return new StepBuilder("diagnosisShardWorkerStep", jobRepository)
            .tasklet(diagnosisShardWorkerTasklet, transactionManager)
            .build();
    }

    @Bean
    public Step diagnosisFinalizeStep(DiagnosisFinalizeTasklet diagnosisFinalizeTasklet) {
        return new StepBuilder("diagnosisFinalizeStep", jobRepository)
            .tasklet(diagnosisFinalizeTasklet, transactionManager)
            .build();
    }
}

package org.dromara.diagnosis.application.batch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 预计算批处理启动器.
 */
@Service
@RequiredArgsConstructor
public class DiagnosisPrecomputeBatchRunner {

    private final JobLauncher jobLauncher;

    private final Job diagnosisSingleMonthPrecomputeJob;

    public void launch(Long jobId, Long windowId, LocalDate periodStart, LocalDate periodEnd, String dataVersion) {
        try {
            JobParameters parameters = new JobParametersBuilder()
                .addLong("jobId", jobId)
                .addLong("windowId", windowId)
                .addString("periodStart", periodStart.toString())
                .addString("periodEnd", periodEnd.toString())
                .addString("dataVersion", dataVersion)
                .addLong("runTs", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(diagnosisSingleMonthPrecomputeJob, parameters);
        } catch (Exception e) {
            throw new IllegalStateException("启动预计算任务失败", e);
        }
    }
}

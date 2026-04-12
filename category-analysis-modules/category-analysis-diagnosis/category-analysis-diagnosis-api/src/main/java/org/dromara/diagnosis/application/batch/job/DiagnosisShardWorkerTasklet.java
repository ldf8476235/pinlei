package org.dromara.diagnosis.application.batch.job;

import lombok.RequiredArgsConstructor;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.diagnosis.api.response.PrecomputeJobProgressResponse;
import org.dromara.diagnosis.application.service.DiagnosisProgressCacheService;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisBatchSourceMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeEventRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceShardParam;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 分片工作任务：按范围读取并统计读取量.
 */
@Component
@RequiredArgsConstructor
public class DiagnosisShardWorkerTasklet implements Tasklet {

    private final DiagnosisBatchSourceMapper batchSourceMapper;

    private final DiagnosisPrecomputeMapper precomputeMapper;

    private final DiagnosisProgressCacheService progressCacheService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        Long jobId = chunkContext.getStepContext().getStepExecution().getJobParameters().getLong("jobId");
        Long windowId = chunkContext.getStepContext().getStepExecution().getJobParameters().getLong("windowId");
        DiagnosisPrecomputeJobRow jobRow = precomputeMapper.selectJobById("000000", jobId);
        if (jobRow != null && "STOPPED".equalsIgnoreCase(jobRow.getStatusCode())) {
            DiagnosisPrecomputeEventRow stopEvent = new DiagnosisPrecomputeEventRow();
            stopEvent.setTenantId("000000");
            stopEvent.setJobId(jobId);
            stopEvent.setWindowId(windowId);
            stopEvent.setEventLevel("WARN");
            stopEvent.setEventStage("STOPPED");
            stopEvent.setEventMessage("任务已停止，分片读取跳过");
            precomputeMapper.insertEvent(stopEvent);
            return RepeatStatus.FINISHED;
        }

        String periodStartText = chunkContext.getStepContext().getStepExecution().getJobParameters().getString("periodStart");
        String periodEndText = chunkContext.getStepContext().getStepExecution().getJobParameters().getString("periodEnd");
        LocalDate periodStart = LocalDate.parse(periodStartText);
        LocalDate periodEnd = LocalDate.parse(periodEndText);

        Long startId = chunkContext.getStepContext().getStepExecutionContext().getLong("startId");
        Long endId = chunkContext.getStepContext().getStepExecutionContext().getLong("endId");

        DiagnosisSourceShardParam param = new DiagnosisSourceShardParam();
        param.setPeriodStart(periodStart);
        param.setPeriodEnd(periodEnd);
        if (startId != null && endId != null && startId > 0 && endId > 0) {
            param.setStartId(startId);
            param.setEndId(endId);
        }

        Long rows = batchSourceMapper.countRows(param);

        DiagnosisPrecomputeEventRow event = new DiagnosisPrecomputeEventRow();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("startId", startId);
        payload.put("endId", endId);
        payload.put("rows", rows);
        payload.put("periodStart", periodStart);
        payload.put("periodEnd", periodEnd);
        event.setTenantId("000000");
        event.setJobId(jobId);
        event.setWindowId(windowId);
        event.setEventLevel("INFO");
        event.setEventStage("READ");
        event.setEventMessage("读取分片完成: startId=" + startId + ", endId=" + endId + ", rows=" + rows);
        event.setPayloadJson(JsonUtils.toJsonString(payload));
        precomputeMapper.insertEvent(event);

        long delta = rows == null ? 0L : rows;
        PrecomputeJobProgressResponse progress = progressCacheService.get(jobId);
        if (progress == null) {
            DiagnosisPrecomputeJobRow dbRow = precomputeMapper.selectJobById("000000", jobId);
            if (dbRow != null) {
                progressCacheService.saveFromJobRow(dbRow);
                progress = progressCacheService.get(jobId);
            }
        }
        if (progress != null) {
            progress.setStatus("RUNNING");
            progress.setCurrentStage("READ");
            progress.setRowsRead((progress.getRowsRead() == null ? 0L : progress.getRowsRead()) + delta);
            if (progress.getProgressPercent() == null || progress.getProgressPercent().compareTo(new BigDecimal("5")) < 0) {
                progress.setProgressPercent(new BigDecimal("5"));
            }
            progressCacheService.save(progress);
        }

        return RepeatStatus.FINISHED;
    }
}

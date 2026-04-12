package org.dromara.diagnosis.application.batch.job;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.application.batch.config.DiagnosisBatchProperties;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisBatchSourceMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceIdRangeRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceShardParam;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 按主键范围切片的分区器.
 */
@Component
@RequiredArgsConstructor
public class DiagnosisIdRangePartitioner implements Partitioner {

    private final DiagnosisBatchSourceMapper batchSourceMapper;

    private final DiagnosisBatchProperties batchProperties;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        if (StepSynchronizationManager.getContext() == null || StepSynchronizationManager.getContext().getStepExecution() == null) {
            throw new IllegalStateException("未获取到批处理上下文，无法构建分片");
        }
        String periodStartText = StepSynchronizationManager.getContext()
            .getStepExecution()
            .getJobParameters()
            .getString("periodStart");
        String periodEndText = StepSynchronizationManager.getContext()
            .getStepExecution()
            .getJobParameters()
            .getString("periodEnd");

        LocalDate periodStart = LocalDate.parse(periodStartText);
        LocalDate periodEnd = LocalDate.parse(periodEndText);

        DiagnosisSourceShardParam rangeParam = new DiagnosisSourceShardParam();
        rangeParam.setPeriodStart(periodStart);
        rangeParam.setPeriodEnd(periodEnd);

        DiagnosisSourceIdRangeRow idRange = batchSourceMapper.selectIdRange(rangeParam);
        long minId = idRange == null || idRange.getMinId() == null ? 0L : idRange.getMinId();
        long maxId = idRange == null || idRange.getMaxId() == null ? 0L : idRange.getMaxId();

        int shards = Math.max(1, Math.min(batchProperties.getParallelShards(), gridSize));
        Map<String, ExecutionContext> result = new HashMap<>(shards);

        if (minId <= 0L || maxId < minId) {
            ExecutionContext context = new ExecutionContext();
            context.putLong("startId", 0L);
            context.putLong("endId", 0L);
            result.put("shard0", context);
            return result;
        }

        long total = maxId - minId + 1;
        long step = Math.max(1L, (long) Math.ceil((double) total / shards));
        long current = minId;
        int shardIndex = 0;
        while (current <= maxId) {
            long end = Math.min(maxId, current + step - 1);
            ExecutionContext context = new ExecutionContext();
            context.putLong("startId", current);
            context.putLong("endId", end);
            result.put("shard" + shardIndex, context);
            current = end + 1;
            shardIndex++;
        }
        return result;
    }
}

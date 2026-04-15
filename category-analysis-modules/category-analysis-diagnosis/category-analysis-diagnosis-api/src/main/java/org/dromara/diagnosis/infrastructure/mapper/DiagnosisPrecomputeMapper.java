package org.dromara.diagnosis.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeEventRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeWindowRow;

import java.util.List;

/**
 * 预计算任务 Mapper.
 */
@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface DiagnosisPrecomputeMapper {

    int insertJob(@Param("row") DiagnosisPrecomputeJobRow row);

    int updateJobProgress(@Param("row") DiagnosisPrecomputeJobRow row);

    int updateJobStatus(@Param("row") DiagnosisPrecomputeJobRow row);

    DiagnosisPrecomputeJobRow selectJobById(@Param("tenantId") String tenantId,
                                            @Param("jobId") Long jobId);

    DiagnosisPrecomputeJobRow selectJobByCode(@Param("tenantId") String tenantId,
                                              @Param("jobCode") String jobCode);

    DiagnosisPrecomputeJobRow selectLatestActiveJobByRequestHash(@Param("tenantId") String tenantId,
                                                                 @Param("requestHash") String requestHash);

    DiagnosisPrecomputeJobRow selectLatestJobByRequestHash(@Param("tenantId") String tenantId,
                                                           @Param("requestHash") String requestHash);

    int insertWindow(@Param("row") DiagnosisPrecomputeWindowRow row);

    int updateWindowStatus(@Param("row") DiagnosisPrecomputeWindowRow row);

    List<DiagnosisPrecomputeWindowRow> selectWindowsByJobId(@Param("tenantId") String tenantId,
                                                            @Param("jobId") Long jobId);

    DiagnosisPrecomputeWindowRow selectWindowById(@Param("tenantId") String tenantId,
                                                  @Param("windowId") Long windowId);

    DiagnosisPrecomputeWindowRow selectNextPendingWindow(@Param("tenantId") String tenantId,
                                                          @Param("jobId") Long jobId);

    int insertEvent(@Param("row") DiagnosisPrecomputeEventRow row);

    List<DiagnosisPrecomputeEventRow> selectRecentEventsByJobId(@Param("tenantId") String tenantId,
                                                                @Param("jobId") Long jobId,
                                                                @Param("limit") Integer limit);
}

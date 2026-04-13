package org.dromara.diagnosis.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.diagnosis.infrastructure.model.DiagnosisCategoryPerformanceTrendRow;

import java.util.List;

/**
 * 品类业绩趋势结果 Mapper.
 */
@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface DiagnosisCategoryPerformanceTrendMapper {

    int deleteByVersion(@Param("tenantId") String tenantId,
                        @Param("queryHash") String queryHash,
                        @Param("dataVersion") String dataVersion);

    int batchInsert(@Param("rows") List<DiagnosisCategoryPerformanceTrendRow> rows);

    List<DiagnosisCategoryPerformanceTrendRow> selectByVersion(@Param("tenantId") String tenantId,
                                                               @Param("queryHash") String queryHash,
                                                               @Param("dataVersion") String dataVersion);
}

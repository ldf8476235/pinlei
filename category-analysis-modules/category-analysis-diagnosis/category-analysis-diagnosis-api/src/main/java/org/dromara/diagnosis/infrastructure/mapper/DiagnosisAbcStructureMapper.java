package org.dromara.diagnosis.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.diagnosis.infrastructure.model.DiagnosisAbcBucketRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisAbcMatrixRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisAbcParamConfigRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisAbcParamSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisAbcSkuRow;

import java.util.List;

@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface DiagnosisAbcStructureMapper {

    List<DiagnosisAbcParamConfigRow> selectParamConfig(@Param("tenantId") String tenantId);

    int upsertParamConfig(@Param("row") DiagnosisAbcParamConfigRow row);

    int deleteByVersion(@Param("tenantId") String tenantId,
                        @Param("queryHash") String queryHash,
                        @Param("dataVersion") String dataVersion);

    int batchInsertParamSnapshot(@Param("rows") List<DiagnosisAbcParamSnapshotRow> rows);

    int batchInsertBucket(@Param("rows") List<DiagnosisAbcBucketRow> rows);

    int insertMatrix(@Param("row") DiagnosisAbcMatrixRow row);

    int batchInsertSku(@Param("rows") List<DiagnosisAbcSkuRow> rows);

    List<DiagnosisAbcParamSnapshotRow> selectParamSnapshot(@Param("tenantId") String tenantId,
                                                           @Param("queryHash") String queryHash,
                                                           @Param("dataVersion") String dataVersion);

    List<DiagnosisAbcBucketRow> selectBucket(@Param("tenantId") String tenantId,
                                             @Param("queryHash") String queryHash,
                                             @Param("dataVersion") String dataVersion,
                                             @Param("abcType") String abcType);

    DiagnosisAbcMatrixRow selectMatrix(@Param("tenantId") String tenantId,
                                       @Param("queryHash") String queryHash,
                                       @Param("dataVersion") String dataVersion,
                                       @Param("abcType") String abcType);

    Long countSku(@Param("tenantId") String tenantId,
                  @Param("queryHash") String queryHash,
                  @Param("dataVersion") String dataVersion,
                  @Param("abcType") String abcType,
                  @Param("currentAbc") String currentAbc,
                  @Param("compareAbc") String compareAbc,
                  @Param("promotion") String promotion,
                  @Param("statusList") List<String> statusList);

    List<DiagnosisAbcSkuRow> selectSkuPage(@Param("tenantId") String tenantId,
                                           @Param("queryHash") String queryHash,
                                           @Param("dataVersion") String dataVersion,
                                           @Param("abcType") String abcType,
                                           @Param("currentAbc") String currentAbc,
                                           @Param("compareAbc") String compareAbc,
                                           @Param("promotion") String promotion,
                                           @Param("statusList") List<String> statusList,
                                           @Param("orderBy") String orderBy,
                                           @Param("orderType") String orderType,
                                           @Param("offset") Integer offset,
                                           @Param("pageSize") Integer pageSize);
}

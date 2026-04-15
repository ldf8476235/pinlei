package org.dromara.diagnosis.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPriceBandLineRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPriceBandPointRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPriceBandRangeRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPriceBandSkuRow;

import java.util.List;

@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface DiagnosisPriceBandMapper {

    int deleteByVersion(@Param("tenantId") String tenantId,
                        @Param("queryHash") String queryHash,
                        @Param("dataVersion") String dataVersion);

    int batchInsertRange(@Param("rows") List<DiagnosisPriceBandRangeRow> rows);

    int batchInsertLine(@Param("rows") List<DiagnosisPriceBandLineRow> rows);

    int batchInsertPoint(@Param("rows") List<DiagnosisPriceBandPointRow> rows);

    int batchInsertSku(@Param("rows") List<DiagnosisPriceBandSkuRow> rows);

    List<DiagnosisPriceBandRangeRow> selectRangeByVersion(@Param("tenantId") String tenantId,
                                                          @Param("queryHash") String queryHash,
                                                          @Param("dataVersion") String dataVersion,
                                                          @Param("orderBy") String orderBy,
                                                          @Param("orderType") String orderType);

    List<DiagnosisPriceBandRangeRow> selectRangeByBandLevel(@Param("tenantId") String tenantId,
                                                            @Param("queryHash") String queryHash,
                                                            @Param("dataVersion") String dataVersion,
                                                            @Param("bandLevel") String bandLevel);

    List<DiagnosisPriceBandLineRow> selectLineByVersion(@Param("tenantId") String tenantId,
                                                        @Param("queryHash") String queryHash,
                                                        @Param("dataVersion") String dataVersion);

    List<DiagnosisPriceBandPointRow> selectPointByVersion(@Param("tenantId") String tenantId,
                                                          @Param("queryHash") String queryHash,
                                                          @Param("dataVersion") String dataVersion);

    List<DiagnosisPriceBandSkuRow> selectSkuPageByVersion(@Param("tenantId") String tenantId,
                                                          @Param("queryHash") String queryHash,
                                                          @Param("dataVersion") String dataVersion,
                                                          @Param("promotionFlag") String promotionFlag,
                                                          @Param("statusList") List<String> statusList,
                                                          @Param("priceBandLabels") List<String> priceBandLabels,
                                                          @Param("orderBy") String orderBy,
                                                          @Param("orderType") String orderType,
                                                          @Param("offset") Integer offset,
                                                          @Param("pageSize") Integer pageSize);

    Long countSkuByVersion(@Param("tenantId") String tenantId,
                           @Param("queryHash") String queryHash,
                           @Param("dataVersion") String dataVersion,
                           @Param("promotionFlag") String promotionFlag,
                           @Param("statusList") List<String> statusList,
                           @Param("priceBandLabels") List<String> priceBandLabels);
}

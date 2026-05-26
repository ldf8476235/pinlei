package org.dromara.diagnosis.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.diagnosis.infrastructure.model.DiagnosisCategorySalesSkuRow;

import java.util.List;

@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface DiagnosisCategorySalesListMapper {

    int deleteByVersion(@Param("tenantId") String tenantId,
                        @Param("queryHash") String queryHash,
                        @Param("dataVersion") String dataVersion);

    int batchInsert(@Param("rows") List<DiagnosisCategorySalesSkuRow> rows);

    Long countSku(@Param("tenantId") String tenantId,
                  @Param("queryHash") String queryHash,
                  @Param("dataVersion") String dataVersion,
                  @Param("promotionFlag") String promotionFlag,
                  @Param("statusList") List<String> statusList,
                  @Param("brandList") List<String> brandList);

    List<DiagnosisCategorySalesSkuRow> selectSkuPage(@Param("tenantId") String tenantId,
                                                     @Param("queryHash") String queryHash,
                                                     @Param("dataVersion") String dataVersion,
                                                     @Param("promotionFlag") String promotionFlag,
                                                     @Param("statusList") List<String> statusList,
                                                     @Param("brandList") List<String> brandList,
                                                     @Param("orderBy") String orderBy,
                                                     @Param("orderType") String orderType,
                                                     @Param("offset") int offset,
                                                     @Param("size") int size);
}

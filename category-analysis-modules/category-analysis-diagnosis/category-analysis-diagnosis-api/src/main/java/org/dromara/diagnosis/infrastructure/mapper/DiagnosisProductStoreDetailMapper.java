package org.dromara.diagnosis.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.diagnosis.infrastructure.model.DiagnosisProductStoreDetailRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisSourceShardParam;

import java.time.LocalDate;
import java.util.List;

@Mapper
@DS("source")
@InterceptorIgnore(tenantLine = "true")
public interface DiagnosisProductStoreDetailMapper {

    Long countStoreDetails(@Param("param") DiagnosisSourceShardParam param,
                           @Param("productNo") String productNo,
                           @Param("stockDate") LocalDate stockDate,
                           @Param("statusList") List<String> statusList,
                           @Param("storeKeyword") String storeKeyword);

    List<DiagnosisProductStoreDetailRow> selectStoreDetailPage(@Param("param") DiagnosisSourceShardParam param,
                                                               @Param("productNo") String productNo,
                                                               @Param("stockDate") LocalDate stockDate,
                                                               @Param("statusList") List<String> statusList,
                                                               @Param("storeKeyword") String storeKeyword,
                                                               @Param("orderBy") String orderBy,
                                                               @Param("orderType") String orderType,
                                                               @Param("offset") Integer offset,
                                                               @Param("size") Integer size);
}

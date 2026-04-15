package org.dromara.diagnosis.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPriceBandConfigRow;

import java.util.List;

@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface DiagnosisPriceBandConfigMapper {

    List<DiagnosisPriceBandConfigRow> selectActiveConfig(@Param("tenantId") String tenantId,
                                                         @Param("queryHash") String queryHash);

    int deleteActiveConfig(@Param("tenantId") String tenantId,
                           @Param("queryHash") String queryHash);

    int batchInsertConfig(@Param("rows") List<DiagnosisPriceBandConfigRow> rows);
}

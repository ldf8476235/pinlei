package org.dromara.diagnosis.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.diagnosis.infrastructure.model.DiagnosisResultPublishVersionRow;

@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface DiagnosisResultPublishVersionMapper {

    int upsert(@Param("row") DiagnosisResultPublishVersionRow row);

    DiagnosisResultPublishVersionRow selectLatestPublishedByQueryHash(@Param("tenantId") String tenantId,
                                                                      @Param("queryHash") String queryHash);
}

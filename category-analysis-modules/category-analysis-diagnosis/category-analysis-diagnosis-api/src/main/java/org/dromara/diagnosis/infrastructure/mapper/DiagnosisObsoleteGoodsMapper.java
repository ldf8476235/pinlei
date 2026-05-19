package org.dromara.diagnosis.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.diagnosis.infrastructure.model.DiagnosisObsoleteGoodsRow;

import java.util.List;

@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface DiagnosisObsoleteGoodsMapper {

    Long countObsoleteGoods(@Param("tenantId") String tenantId,
                            @Param("queryHash") String queryHash,
                            @Param("dataVersion") String dataVersion,
                            @Param("abcType") String abcType,
                            @Param("currentAbc") String currentAbc,
                            @Param("compareAbc") String compareAbc,
                            @Param("currentGrossRole") String currentGrossRole,
                            @Param("compareGrossRole") String compareGrossRole,
                            @Param("currentGmroiRole") String currentGmroiRole,
                            @Param("compareGmroiRole") String compareGmroiRole,
                            @Param("useAbc") boolean useAbc,
                            @Param("useGross") boolean useGross,
                            @Param("useGmroi") boolean useGmroi,
                            @Param("statusList") List<String> statusList,
                            @Param("firstSaleDate") String firstSaleDate,
                            @Param("priceBandList") List<String> priceBandList,
                            @Param("brandList") List<String> brandList,
                            @Param("specList") List<String> specList,
                            @Param("tagType") String tagType,
                            @Param("tagList") List<String> tagList);

    List<DiagnosisObsoleteGoodsRow> selectObsoleteGoodsPage(@Param("tenantId") String tenantId,
                                                            @Param("queryHash") String queryHash,
                                                            @Param("dataVersion") String dataVersion,
                                                            @Param("abcType") String abcType,
                                                            @Param("currentAbc") String currentAbc,
                                                            @Param("compareAbc") String compareAbc,
                                                            @Param("currentGrossRole") String currentGrossRole,
                                                            @Param("compareGrossRole") String compareGrossRole,
                                                            @Param("currentGmroiRole") String currentGmroiRole,
                                                            @Param("compareGmroiRole") String compareGmroiRole,
                                                            @Param("useAbc") boolean useAbc,
                                                            @Param("useGross") boolean useGross,
                                                            @Param("useGmroi") boolean useGmroi,
                                                            @Param("statusList") List<String> statusList,
                                                            @Param("firstSaleDate") String firstSaleDate,
                                                            @Param("priceBandList") List<String> priceBandList,
                                                            @Param("brandList") List<String> brandList,
                                                            @Param("specList") List<String> specList,
                                                            @Param("tagType") String tagType,
                                                            @Param("tagList") List<String> tagList,
                                                            @Param("orderBy") String orderBy,
                                                            @Param("orderType") String orderType,
                                                            @Param("offset") Integer offset,
                                                            @Param("pageSize") Integer pageSize);
}

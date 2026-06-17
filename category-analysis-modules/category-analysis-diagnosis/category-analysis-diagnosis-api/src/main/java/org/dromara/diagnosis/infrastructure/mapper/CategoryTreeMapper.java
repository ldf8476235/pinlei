package org.dromara.diagnosis.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.diagnosis.infrastructure.model.CategoryHierarchyRow;
import org.dromara.diagnosis.infrastructure.model.CategorySaleSkuRow;
import org.dromara.diagnosis.infrastructure.model.CategorySkuMetricRow;
import org.dromara.diagnosis.infrastructure.model.CategoryStoreConfigRow;
import org.dromara.diagnosis.infrastructure.model.CategoryTreeQueryParam;
import org.dromara.diagnosis.infrastructure.model.DiagnosisDictRow;
import org.dromara.diagnosis.infrastructure.model.StoreBaseRow;

import java.util.List;

/**
 * 品类树数据查询.
 */
@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface CategoryTreeMapper {

    @DS("source")
    List<CategoryHierarchyRow> selectClassHierarchy();

    List<CategorySkuMetricRow> selectCategorySkuMetrics(@Param("param") CategoryTreeQueryParam param);

    @DS("source")
    List<CategorySaleSkuRow> selectCategorySaleSku(@Param("param") CategoryTreeQueryParam param);

    @DS("source")
    Integer countClassByClassNo(@Param("classNo") String classNo);

    @DS("source")
    Integer countStoreByStoreNo(@Param("storeNo") String storeNo);

    @DS("source")
    String selectClassNameByClassNo(@Param("classNo") String classNo);

    @DS("source")
    CategoryStoreConfigRow selectStoreConfigByStoreNo(@Param("storeNo") String storeNo);

    int upsertCategoryNodeConfig(@Param("storeNo") String storeNo,
                                 @Param("classNo") String classNo,
                                 @Param("className") String className,
                                 @Param("store") CategoryStoreConfigRow store,
                                 @Param("roleNo") String roleNo,
                                 @Param("suggestSaleSku") Integer suggestSaleSku,
                                 @Param("sysSuggestSaleSku") Integer sysSuggestSaleSku);

    CategorySkuMetricRow selectCategoryNodeConfig(@Param("storeNo") String storeNo,
                                                  @Param("classNo") String classNo);

    List<DiagnosisDictRow> selectDictRowsByType(@Param("dictType") String dictType);

    @DS("source")
    List<StoreBaseRow> selectStores(@Param("param") CategoryTreeQueryParam param);
}

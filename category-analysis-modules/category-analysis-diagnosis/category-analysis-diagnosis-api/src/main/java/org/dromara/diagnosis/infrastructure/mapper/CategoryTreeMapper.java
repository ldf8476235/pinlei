package org.dromara.diagnosis.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.diagnosis.infrastructure.model.CategoryHierarchyRow;
import org.dromara.diagnosis.infrastructure.model.CategorySaleSkuRow;
import org.dromara.diagnosis.infrastructure.model.CategorySkuMetricRow;
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

    List<CategoryHierarchyRow> selectClassHierarchy();

    List<CategorySkuMetricRow> selectCategorySkuMetrics(@Param("param") CategoryTreeQueryParam param);

    List<CategorySaleSkuRow> selectCategorySaleSku(@Param("param") CategoryTreeQueryParam param);

    Integer countClassByClassNo(@Param("classNo") String classNo);

    Integer countStoreByStoreNo(@Param("storeNo") String storeNo);

    int upsertCategoryNodeConfig(@Param("storeNo") String storeNo,
                                 @Param("classNo") String classNo,
                                 @Param("roleNo") String roleNo,
                                 @Param("suggestSaleSku") Integer suggestSaleSku,
                                 @Param("sysSuggestSaleSku") Integer sysSuggestSaleSku);

    CategorySkuMetricRow selectCategoryNodeConfig(@Param("storeNo") String storeNo,
                                                  @Param("classNo") String classNo);

    List<DiagnosisDictRow> selectDictRowsByType(@Param("dictType") String dictType);

    List<StoreBaseRow> selectStores(@Param("param") CategoryTreeQueryParam param);
}

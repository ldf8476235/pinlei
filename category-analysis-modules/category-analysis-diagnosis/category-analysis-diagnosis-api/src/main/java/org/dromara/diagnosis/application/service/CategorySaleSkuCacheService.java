package org.dromara.diagnosis.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.diagnosis.application.config.DiagnosisRedisKeyPrefixProperties;
import org.dromara.diagnosis.infrastructure.mapper.CategoryTreeMapper;
import org.dromara.diagnosis.infrastructure.model.CategorySaleSkuRow;
import org.dromara.diagnosis.infrastructure.model.CategoryTreeQueryParam;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Cache expensive actual SKU aggregation by category.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategorySaleSkuCacheService {

    private static final String ALL = "__ALL__";

    private static final int LOCK_WAIT_TIMES = 90;

    private static final long LOCK_WAIT_MILLIS = 500L;

    private final CategoryTreeMapper categoryTreeMapper;

    private final DiagnosisRedisKeyPrefixProperties keyPrefixProperties;

    public List<CategorySaleSkuRow> selectCategorySaleSku(CategoryTreeQueryParam param) {
        String cacheKey = cacheKey(param);
        List<CategorySaleSkuRow> cached = readCache(cacheKey);
        if (cached != null) {
            log.debug("hit category sale sku cache, key={}", cacheKey);
            return cached;
        }

        String lockKey = cacheKey + ":lock";
        boolean locked = tryLock(lockKey);
        if (locked) {
            try {
                List<CategorySaleSkuRow> rows = safeRows(categoryTreeMapper.selectCategorySaleSku(param));
                writeCache(cacheKey, rows);
                return rows;
            } finally {
                releaseLock(lockKey);
            }
        }

        cached = waitCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<CategorySaleSkuRow> rows = safeRows(categoryTreeMapper.selectCategorySaleSku(param));
        writeCache(cacheKey, rows);
        return rows;
    }

    private List<CategorySaleSkuRow> readCache(String cacheKey) {
        try {
            String text = RedisUtils.getCacheObject(cacheKey);
            if (text == null || text.isBlank()) {
                return null;
            }
            return JsonUtils.parseArray(text, CategorySaleSkuRow.class);
        } catch (Exception e) {
            log.warn("read category sale sku cache failed, key={}", cacheKey, e);
            return null;
        }
    }

    private void writeCache(String cacheKey, List<CategorySaleSkuRow> rows) {
        try {
            RedisUtils.setCacheObject(cacheKey,
                JsonUtils.toJsonString(safeRows(rows)));
        } catch (Exception e) {
            log.warn("write category sale sku cache failed, key={}", cacheKey, e);
        }
    }

    private boolean tryLock(String lockKey) {
        try {
            return RedisUtils.setObjectIfAbsent(lockKey, "1", Duration.ofSeconds(60));
        } catch (Exception e) {
            log.warn("acquire category sale sku cache lock failed, key={}", lockKey, e);
            return true;
        }
    }

    private List<CategorySaleSkuRow> waitCache(String cacheKey) {
        for (int i = 0; i < LOCK_WAIT_TIMES; i++) {
            try {
                TimeUnit.MILLISECONDS.sleep(LOCK_WAIT_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
            List<CategorySaleSkuRow> cached = readCache(cacheKey);
            if (cached != null) {
                return cached;
            }
        }
        return null;
    }

    private void releaseLock(String lockKey) {
        try {
            RedisUtils.deleteObject(lockKey);
        } catch (Exception e) {
            log.warn("release category sale sku cache lock failed, key={}", lockKey, e);
        }
    }

    private String cacheKey(CategoryTreeQueryParam param) {
        return keyPrefixProperties.getResultQuery()
            + ":category-sale-sku:v1"
            + ":retailTypeId:" + normalize(param == null ? null : param.getRetailTypeId())
            + ":businessCircleId:" + normalize(param == null ? null : param.getBusinessCircleId())
            + ":deptGroupId:" + normalize(param == null ? null : param.getDeptGroupId())
            + ":storeNo:" + normalize(param == null ? null : param.getStoreNo())
            + ":keyword:" + normalize(param == null ? null : param.getKeyword())
            + ":limit:" + Objects.toString(param == null ? null : param.getLimit(), ALL);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }
        return value.trim();
    }

    private List<CategorySaleSkuRow> safeRows(List<CategorySaleSkuRow> rows) {
        return rows == null ? List.of() : rows;
    }
}

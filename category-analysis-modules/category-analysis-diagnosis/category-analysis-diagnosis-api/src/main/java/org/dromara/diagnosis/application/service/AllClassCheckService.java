package org.dromara.diagnosis.application.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.request.AllClassCheckListRequest;
import org.dromara.diagnosis.api.request.AllClassCheckRequest;
import org.dromara.diagnosis.api.response.AllClassCheckSalesChangeItemResponse;
import org.dromara.diagnosis.api.response.AllClassCheckSalesChangeResponse;
import org.dromara.diagnosis.api.response.AllClassCheckScatterItemResponse;
import org.dromara.diagnosis.api.response.AllClassCheckScatterResponse;
import org.dromara.diagnosis.api.response.AllClassCheckScatterXYResponse;
import org.dromara.diagnosis.api.response.AllClassCheckSkuDifferItemResponse;
import org.dromara.diagnosis.api.response.AllClassCheckSkuDifferResponse;
import org.dromara.diagnosis.api.response.AllClassCheckSkuItemResponse;
import org.dromara.diagnosis.api.response.AllClassCheckSkuResponse;
import org.dromara.diagnosis.api.response.AllClassCheckSkuYDataResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllClassCheckService {

    private static final Logger log = LoggerFactory.getLogger(AllClassCheckService.class);

    private static final int DEFAULT_CLASS_LEVEL = 1;
    private static final BigDecimal SKU_STRUCTURE_DIFF_WARNING_THRESHOLD = BigDecimal.valueOf(3);
    private static final Map<String, String> ROLE_NAME_MAP = Map.of(
        "1", "明星品类",
        "2", "幼童品类",
        "3", "结构品类",
        "4", "金牛品类"
    );

    private final JdbcTemplate jdbcTemplate;
    private final ConcurrentHashMap<String, CompletableFuture<List<ClassAggRow>>> classAggregateFlights = new ConcurrentHashMap<>();

    public AllClassCheckSalesChangeResponse getSalesChange(AllClassCheckListRequest request) {
        int classLevel = normalizeClassLevel(request.getClassLevel());
        List<ClassAggRow> rows = loadClassAggregates(request);
        Map<String, String> roleMap = loadRoleMap(request);
        rows.forEach(row -> row.roleCode = roleMap.getOrDefault(row.classNo, row.roleCode));
        List<ClassAggRow> filtered = rows.stream()
            .filter(row -> matchRoleFilter(row.roleCode, request.getClassRole()))
            .collect(Collectors.toList());
        applyShareMetrics(filtered);
        filtered.sort(resolveComparator(request));

        int page = normalizePage(request.getPage());
        int size = normalizeSize(request.getSize());
        int fromIndex = Math.min((page - 1) * size, filtered.size());
        int toIndex = Math.min(fromIndex + size, filtered.size());
        List<AllClassCheckSalesChangeItemResponse> pageList = filtered.subList(fromIndex, toIndex).stream()
            .map(row -> toSalesChangeItem(row, classLevel))
            .collect(Collectors.toList());

        AllClassCheckSalesChangeResponse response = new AllClassCheckSalesChangeResponse();
        response.setContent(pageList);
        response.setList(pageList);
        response.setTotalElements((long) filtered.size());
        response.setTotal((long) filtered.size());
        return response;
    }

    public AllClassCheckScatterResponse getAllClassCheck(AllClassCheckRequest request) {
        int classLevel = normalizeClassLevel(request.getClassLevel());
        List<ClassAggRow> rows = loadClassAggregates(request);
        Map<String, String> roleMap = loadRoleMap(request);
        rows.forEach(row -> row.roleCode = roleMap.getOrDefault(row.classNo, row.roleCode));
        List<ClassAggRow> filtered = rows.stream()
            .filter(row -> matchRoleFilter(row.roleCode, request.getClassRole()))
            .collect(Collectors.toList());
        applyShareMetrics(filtered);
        filtered.sort(Comparator.comparing((ClassAggRow r) -> nvl(r.contributionRatePer)).reversed()
            .thenComparing(r -> r.classNo));

        BigDecimal avgX = average(filtered.stream().map(r -> nvl(r.contributionRatePer)).toList());
        BigDecimal avgY = average(filtered.stream().map(r -> nvl(r.salesCompareRate)).toList());
        BigDecimal minX = filtered.stream().map(r -> nvl(r.contributionRatePer)).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal maxX = filtered.stream().map(r -> nvl(r.contributionRatePer)).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal minY = filtered.stream().map(r -> nvl(r.salesCompareRate)).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal maxY = filtered.stream().map(r -> nvl(r.salesCompareRate)).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        AllClassCheckScatterXYResponse xyData = new AllClassCheckScatterXYResponse();
        xyData.setAvgPointX(avgX);
        xyData.setAvgPointY(avgY);
        xyData.setXMin(minX);
        xyData.setXMax(maxX);
        xyData.setYMin(minY);
        xyData.setYMax(maxY);
        xyData.setXName("综合贡献率");
        xyData.setYName("销售对比增长率%");

        int warningCount = countRoleWarnings(filtered, avgX, avgY, roleMap);
        log.info("all class role warning calculated, classLevel={}, total={}, warning={}",
            classLevel, filtered.size(), warningCount);

        AllClassCheckScatterResponse response = new AllClassCheckScatterResponse();
        response.setWarn(warningCount);
        response.setScaleY(buildScaleY(minY, maxY));
        response.setXyData(xyData);
        response.setList(filtered.stream().map(row -> toScatterItem(row, classLevel, avgX, avgY, roleMap)).collect(Collectors.toList()));
        return response;
    }

    public AllClassCheckSkuResponse getFindClassSku(AllClassCheckRequest request) {
        int classLevel = normalizeClassLevel(request.getClassLevel());
        List<ClassAggRow> rows = loadClassAggregates(request);
        Map<String, String> roleMap = loadRoleMap(request);
        rows.forEach(row -> row.roleCode = roleMap.getOrDefault(row.classNo, row.roleCode));
        List<ClassSkuRow> skuRows = loadClassSkuConfigRows(request);
        Map<String, Integer> configMap = skuRows.stream().collect(Collectors.toMap(r -> r.classNo, r -> r.classSku, Integer::sum));
        Map<String, Integer> actualSkuMap = loadActualSaleSkuMap(request);

        List<ClassAggRow> filtered = rows.stream()
            .filter(row -> matchRoleFilter(row.roleCode, request.getClassRole()))
            .sorted(Comparator.comparing((ClassAggRow r) -> nvl(r.sales)).reversed()
                .thenComparing(r -> r.classNo))
            .collect(Collectors.toList());
        applyShareMetrics(filtered);

        BigDecimal totalSales = filtered.stream().map(r -> nvl(r.sales)).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalConfigSku = skuRows.stream().map(r -> BigDecimal.valueOf(Math.max(r.classSku, 0))).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<AllClassCheckSkuItemResponse> list = new ArrayList<>();
        for (ClassAggRow row : filtered) {
            int classSku = configMap.getOrDefault(row.classNo, 0);
            int saleSku = actualSkuMap.getOrDefault(row.classNo, 0);
            BigDecimal salesPer = ratePercent(row.sales, totalSales);
            BigDecimal skuPer = ratePercent(BigDecimal.valueOf(classSku), totalConfigSku);
            BigDecimal skuDifference = skuPer.subtract(salesPer);

            AllClassCheckSkuItemResponse item = new AllClassCheckSkuItemResponse();
            item.setClassNo(row.classNo);
            item.setClassName(row.className);
            item.setClassLevel(classLevel);
            item.setSales(row.sales);
            item.setSalesPer(salesPer);
            item.setSkuPer(skuPer);
            item.setSkuDifference(skuDifference);
            item.setClassSku(classSku);
            fillRole(item, row.roleCode);
            list.add(item);
        }

        AllClassCheckSkuYDataResponse yData = new AllClassCheckSkuYDataResponse();
        yData.setYOneSalesPerMin(list.stream().map(i -> nvl(i.getSalesPer())).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        yData.setYOneSalesPerMax(list.stream().map(i -> nvl(i.getSalesPer())).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        yData.setYOneSkuPerMin(list.stream().map(i -> nvl(i.getSkuPer())).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        yData.setYOneSkuPerMax(list.stream().map(i -> nvl(i.getSkuPer())).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        yData.setYTwoSkuDifferenceMin(list.stream().map(i -> nvl(i.getSkuDifference())).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        yData.setYTwoSkuDifferenceMax(list.stream().map(i -> nvl(i.getSkuDifference())).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));

        int warningCount = countSkuStructureWarnings(list);
        log.info("all class sku structure warning calculated, classLevel={}, total={}, warning={}",
            classLevel, list.size(), warningCount);

        AllClassCheckSkuResponse response = new AllClassCheckSkuResponse();
        response.setWarn(warningCount);
        response.setYData(yData);
        response.setList(list);
        return response;
    }

    public AllClassCheckSkuDifferResponse getFindClassSkuDiffer(AllClassCheckRequest request) {
        int classLevel = normalizeClassLevel(request.getClassLevel());
        List<ClassAggRow> rows = loadClassAggregates(request);
        Map<String, String> roleMap = loadRoleMap(request);
        rows.forEach(row -> row.roleCode = roleMap.getOrDefault(row.classNo, row.roleCode));
        List<ClassSkuRow> skuRows = loadClassSkuConfigRows(request);
        Map<String, Integer> configMap = skuRows.stream().collect(Collectors.toMap(r -> r.classNo, r -> r.classSku, Integer::sum));
        Map<String, Integer> actualSkuMap = loadActualSaleSkuMap(request);

        List<ClassAggRow> filtered = rows.stream()
            .filter(row -> matchRoleFilter(row.roleCode, request.getClassRole()))
            .sorted(Comparator.comparing((ClassAggRow r) -> nvl(r.sales)).reversed()
                .thenComparing(r -> r.classNo))
            .collect(Collectors.toList());
        applyShareMetrics(filtered);

        BigDecimal totalSales = filtered.stream().map(r -> nvl(r.sales)).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalConfigSku = skuRows.stream().map(r -> BigDecimal.valueOf(Math.max(r.classSku, 0))).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<AllClassCheckSkuDifferItemResponse> list = new ArrayList<>();
        for (ClassAggRow row : filtered) {
            int suggestSaleSku = rateCount(row.sales, totalSales, totalConfigSku);
            int saleSku = actualSkuMap.getOrDefault(row.classNo, 0);

            AllClassCheckSkuDifferItemResponse item = new AllClassCheckSkuDifferItemResponse();
            item.setClassNo(row.classNo);
            item.setClassName(row.className);
            item.setClassLevel(classLevel);
            item.setSuggestSaleSku(suggestSaleSku);
            item.setSaleSku(saleSku);
            item.setSkuDiffer(saleSku - suggestSaleSku);
            fillRole(item, row.roleCode);
            list.add(item);
        }

        int warningCount = countSkuPresetWarnings(list);
        log.info("all class sku preset warning calculated, classLevel={}, total={}, warning={}",
            classLevel, list.size(), warningCount);

        AllClassCheckSkuDifferResponse response = new AllClassCheckSkuDifferResponse();
        response.setWarn(warningCount);
        response.setList(list);
        return response;
    }

    private List<ClassAggRow> loadClassAggregates(AllClassCheckRequest request) {
        String cacheKey = classAggregateKey(request);
        CompletableFuture<List<ClassAggRow>> newFuture = new CompletableFuture<>();
        CompletableFuture<List<ClassAggRow>> future = classAggregateFlights.putIfAbsent(cacheKey, newFuture);
        if (future == null) {
            try {
                List<ClassAggRow> rows = queryClassAggregates(request);
                newFuture.complete(rows);
                return copyClassAggRows(rows);
            } catch (Exception ex) {
                newFuture.completeExceptionally(ex);
                throw ex;
            } finally {
                classAggregateFlights.remove(cacheKey, newFuture);
            }
        }
        try {
            log.info("reuse in-flight all class aggregate, key={}", cacheKey);
            return copyClassAggRows(future.join());
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw ex;
        }
    }

    private List<ClassAggRow> queryClassAggregates(AllClassCheckRequest request) {
        int classLevel = normalizeClassLevel(request.getClassLevel());
        ClassLevelColumns columns = resolveClassLevelColumns(classLevel);
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            WITH class_dim AS (
                SELECT DISTINCT
                    %s AS class_no,
                    %s AS class_name
                FROM base_class
                WHERE %s IS NOT NULL AND LTRIM(RTRIM(%s)) <> ''
            )
            SELECT
                dim.class_no AS class_no,
                dim.class_name AS class_name,
                COALESCE(SUM(CASE WHEN fs.sale_date BETWEEN ? AND ? THEN CAST(fs.sales AS DECIMAL(20, 6)) ELSE 0 END), 0) AS current_sales,
                COALESCE(SUM(CASE WHEN fs.sale_date BETWEEN ? AND ? THEN CAST(fs.gross AS DECIMAL(20, 6)) ELSE 0 END), 0) AS current_gross,
                COALESCE(SUM(CASE WHEN fs.sale_date BETWEEN ? AND ? THEN CAST(fs.sale_quantity AS DECIMAL(20, 6)) ELSE 0 END), 0) AS current_qty,
                COALESCE(SUM(CASE WHEN fs.sale_date BETWEEN ? AND ? THEN CAST(fs.sales AS DECIMAL(20, 6)) ELSE 0 END), 0) AS compare_sales,
                COALESCE(SUM(CASE WHEN fs.sale_date BETWEEN ? AND ? THEN CAST(fs.gross AS DECIMAL(20, 6)) ELSE 0 END), 0) AS compare_gross,
                COALESCE(SUM(CASE WHEN fs.sale_date BETWEEN ? AND ? THEN CAST(fs.sale_quantity AS DECIMAL(20, 6)) ELSE 0 END), 0) AS compare_qty
            FROM class_dim dim
            LEFT JOIN fact_sales_day fs
              ON fs.%s = dim.class_no
             AND (
                fs.sale_date BETWEEN ? AND ?
                OR fs.sale_date BETWEEN ? AND ?
             )
            WHERE 1 = 1
            """.formatted(
            columns.codeColumn,
            columns.nameColumn,
            columns.codeColumn,
            columns.codeColumn,
            columns.codeColumn
        ));

        addDateParams(params, request.getCurrentStartDate(), request.getCurrentEndDate());
        addDateParams(params, request.getCurrentStartDate(), request.getCurrentEndDate());
        addDateParams(params, request.getCurrentStartDate(), request.getCurrentEndDate());
        addDateParams(params, request.getCompareStartDate(), request.getCompareEndDate());
        addDateParams(params, request.getCompareStartDate(), request.getCompareEndDate());
        addDateParams(params, request.getCompareStartDate(), request.getCompareEndDate());
        addDateParams(params, request.getCurrentStartDate(), request.getCurrentEndDate());
        addDateParams(params, request.getCompareStartDate(), request.getCompareEndDate());

        applyStoreScope(sql, params, request);
        appendClassFilter(sql, params, request.getClassNo(), "dim.class_no");
        sql.append(" GROUP BY dim.class_no, dim.class_name ORDER BY dim.class_no");

        return jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) -> {
            ClassAggRow row = new ClassAggRow();
            row.classNo = rs.getString("class_no");
            row.className = rs.getString("class_name");
            row.currentSales = rs.getBigDecimal("current_sales");
            row.currentGross = rs.getBigDecimal("current_gross");
            row.currentQty = rs.getBigDecimal("current_qty");
            row.compareSales = rs.getBigDecimal("compare_sales");
            row.compareGross = rs.getBigDecimal("compare_gross");
            row.compareQty = rs.getBigDecimal("compare_qty");
            row.sales = nvl(row.currentSales);
            row.salesCompare = nvl(row.compareSales);
            row.gross = nvl(row.currentGross);
            row.saleQuantity = nvl(row.currentQty);
            row.salesCompareRate = growthRate(row.sales, row.salesCompare);
            row.contributionRatePer = BigDecimal.ZERO;
            row.salesPer = BigDecimal.ZERO;
            row.roleCode = inferRoleCode(row.classNo, row.className);
            return row;
        }).stream().peek(this::enrichDerivedMetrics).toList();
    }

    private String classAggregateKey(AllClassCheckRequest request) {
        StringJoiner joiner = new StringJoiner("|");
        joiner.add(String.valueOf(normalizeClassLevel(request.getClassLevel())));
        joiner.add(normalizeKeyPart(request.getStoreNo()));
        joiner.add(normalizeKeyPart(request.getDeptId()));
        joiner.add(normalizeKeyPart(request.getRetailTypeId()));
        joiner.add(normalizeKeyPart(request.getBusinessCircleId()));
        joiner.add(normalizeKeyPart(request.getDeptGroupId()));
        joiner.add(normalizeKeyPart(request.getCurrentStartDate()));
        joiner.add(normalizeKeyPart(request.getCurrentEndDate()));
        joiner.add(normalizeKeyPart(request.getCompareStartDate()));
        joiner.add(normalizeKeyPart(request.getCompareEndDate()));
        joiner.add(String.join(",", normalizeList(request.getClassNo())));
        return joiner.toString();
    }

    private String normalizeKeyPart(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private List<ClassAggRow> copyClassAggRows(List<ClassAggRow> rows) {
        return rows.stream().map(this::copyClassAggRow).collect(Collectors.toList());
    }

    private ClassAggRow copyClassAggRow(ClassAggRow source) {
        ClassAggRow row = new ClassAggRow();
        row.classNo = source.classNo;
        row.className = source.className;
        row.currentSales = source.currentSales;
        row.currentGross = source.currentGross;
        row.currentQty = source.currentQty;
        row.compareSales = source.compareSales;
        row.compareGross = source.compareGross;
        row.compareQty = source.compareQty;
        row.sales = source.sales;
        row.salesCompare = source.salesCompare;
        row.gross = source.gross;
        row.saleQuantity = source.saleQuantity;
        row.salesCompareRate = source.salesCompareRate;
        row.contributionRatePer = source.contributionRatePer;
        row.salesPer = source.salesPer;
        row.roleCode = source.roleCode;
        return row;
    }

    private void enrichDerivedMetrics(ClassAggRow row) {
        row.contributionRatePer = nvl(row.contributionRatePer);
        row.salesPer = nvl(row.salesPer);
    }

    private void applyShareMetrics(List<ClassAggRow> rows) {
        BigDecimal totalSales = rows.stream().map(r -> nvl(r.sales)).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalGross = rows.stream().map(r -> nvl(r.gross)).reduce(BigDecimal.ZERO, BigDecimal::add);
        for (ClassAggRow row : rows) {
            row.salesPer = ratePercent(row.sales, totalSales);
            row.contributionRatePer = ratePercent(row.gross, totalGross);
        }
    }

    private List<ClassSkuRow> loadClassSkuConfigRows(AllClassCheckRequest request) {
        ClassLevelColumns columns = resolveClassLevelColumns(normalizeClassLevel(request.getClassLevel()));
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            WITH class_dim AS (
                SELECT DISTINCT
                    %s AS class_no
                FROM base_class
                WHERE %s IS NOT NULL AND LTRIM(RTRIM(%s)) <> ''
            )
            SELECT
                bcs.class_no AS class_no,
                COALESCE(SUM(COALESCE(TRY_CONVERT(INT, NULLIF(LTRIM(RTRIM(bcs.class_sku)), '')), 0)), 0) AS class_sku
            FROM base_class_sku bcs
            INNER JOIN class_dim dim
              ON dim.class_no = bcs.class_no
            WHERE bcs.class_no IS NOT NULL AND LTRIM(RTRIM(bcs.class_no)) <> ''
            """.formatted(columns.codeColumn, columns.codeColumn, columns.codeColumn));
        applyStoreScopeToClassSku(sql, params, request);
        appendClassFilter(sql, params, request.getClassNo(), "bcs.class_no");
        sql.append(" GROUP BY bcs.class_no");

        return jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) -> {
            ClassSkuRow row = new ClassSkuRow();
            row.classNo = rs.getString("class_no");
            row.classSku = rs.getInt("class_sku");
            return row;
        });
    }

    private Map<String, String> loadRoleMap(AllClassCheckRequest request) {
        ClassLevelColumns columns = resolveClassLevelColumns(normalizeClassLevel(request.getClassLevel()));
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            WITH class_dim AS (
                SELECT DISTINCT
                    %s AS class_no
                FROM base_class
                WHERE %s IS NOT NULL AND LTRIM(RTRIM(%s)) <> ''
            )
            SELECT
                bcs.class_no AS class_no,
                MAX(LTRIM(RTRIM(COALESCE(bcs.class_role, '')))) AS role_code
            FROM base_class_sku bcs
            INNER JOIN class_dim dim
              ON dim.class_no = bcs.class_no
            WHERE bcs.class_no IS NOT NULL AND LTRIM(RTRIM(bcs.class_no)) <> ''
            """.formatted(columns.codeColumn, columns.codeColumn, columns.codeColumn));
        applyStoreScopeToClassSku(sql, params, request);
        appendClassFilter(sql, params, request.getClassNo(), "bcs.class_no");
        sql.append(" GROUP BY bcs.class_no");

        return jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) -> Map.entry(
            rs.getString("class_no"),
            normalizeRoleCode(rs.getString("role_code"))
        )).stream()
            .filter(entry -> entry.getKey() != null && entry.getValue() != null && !entry.getValue().isBlank())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a));
    }

    private Map<String, Integer> loadActualSaleSkuMap(AllClassCheckRequest request) {
        ClassLevelColumns columns = resolveClassLevelColumns(normalizeClassLevel(request.getClassLevel()));
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT
                fs.%s AS class_no,
                COUNT(DISTINCT fs.product_no) AS sale_sku
            FROM fact_sales_day fs
            WHERE fs.%s IS NOT NULL AND LTRIM(RTRIM(fs.%s)) <> ''
              AND fs.sale_date BETWEEN ? AND ?
            """.formatted(columns.codeColumn, columns.codeColumn, columns.codeColumn));
        addDateParams(params, request.getCurrentStartDate(), request.getCurrentEndDate());
        applyStoreScope(sql, params, request);
        appendClassFilter(sql, params, request.getClassNo(), "fs." + columns.codeColumn);
        sql.append(" GROUP BY fs.").append(columns.codeColumn);

        return jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) -> Map.entry(
            rs.getString("class_no"),
            rs.getInt("sale_sku")
        )).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum));
    }

    private void applyStoreScope(StringBuilder sql, List<Object> params, AllClassCheckRequest request) {
        if (hasText(request.getStoreNo()) && !isAll(request.getStoreNo())) {
            sql.append(" AND fs.store_no = ?");
            params.add(request.getStoreNo());
            return;
        }
        if (hasText(request.getDeptId()) && !isAll(request.getDeptId())) {
            sql.append(" AND fs.store_no IN (SELECT store_no FROM base_department WHERE preorgcode = ? AND store_type_no = '1' AND status_no = 1)");
            params.add(request.getDeptId());
        }
        if (hasText(request.getRetailTypeId()) && !isAll(request.getRetailTypeId())) {
            sql.append(" AND fs.store_no IN (SELECT store_no FROM base_department WHERE store_format_no = ? AND store_type_no = '1' AND status_no = 1)");
            params.add(request.getRetailTypeId());
        }
        if (hasText(request.getBusinessCircleId()) && !isAll(request.getBusinessCircleId())) {
            sql.append(" AND fs.store_no IN (SELECT store_no FROM base_department WHERE business_circle_no = ? AND store_type_no = '1' AND status_no = 1)");
            params.add(request.getBusinessCircleId());
        }
        if (hasText(request.getDeptGroupId()) && !isAll(request.getDeptGroupId())) {
            sql.append(" AND fs.store_no IN (SELECT store_no FROM base_department WHERE store_group_no = ? AND store_type_no = '1' AND status_no = 1)");
            params.add(request.getDeptGroupId());
        }
    }

    private void applyStoreScopeToClassSku(StringBuilder sql, List<Object> params, AllClassCheckRequest request) {
        if (hasText(request.getStoreNo()) && !isAll(request.getStoreNo())) {
            sql.append(" AND bcs.store_no = ?");
            params.add(request.getStoreNo());
            return;
        }
        if (hasText(request.getDeptId()) && !isAll(request.getDeptId())) {
            sql.append(" AND bcs.store_no IN (SELECT store_no FROM base_department WHERE preorgcode = ? AND store_type_no = '1' AND status_no = 1)");
            params.add(request.getDeptId());
        }
        if (hasText(request.getRetailTypeId()) && !isAll(request.getRetailTypeId())) {
            sql.append(" AND bcs.store_no IN (SELECT store_no FROM base_department WHERE store_format_no = ? AND store_type_no = '1' AND status_no = 1)");
            params.add(request.getRetailTypeId());
        }
        if (hasText(request.getBusinessCircleId()) && !isAll(request.getBusinessCircleId())) {
            sql.append(" AND bcs.store_no IN (SELECT store_no FROM base_department WHERE business_circle_no = ? AND store_type_no = '1' AND status_no = 1)");
            params.add(request.getBusinessCircleId());
        }
        if (hasText(request.getDeptGroupId()) && !isAll(request.getDeptGroupId())) {
            sql.append(" AND bcs.store_no IN (SELECT store_no FROM base_department WHERE store_group_no = ? AND store_type_no = '1' AND status_no = 1)");
            params.add(request.getDeptGroupId());
        }
    }

    private void appendClassFilter(StringBuilder sql, List<Object> params, List<String> classNos, String alias) {
        List<String> filtered = normalizeList(classNos);
        if (filtered.isEmpty()) {
            return;
        }
        sql.append(" AND ");
        sql.append(alias).append(" IN (");
        sql.append(String.join(",", filtered.stream().map(v -> "?").toList()));
        sql.append(")");
        params.addAll(filtered);
    }

    private List<String> normalizeList(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(v -> !v.isBlank() && !"all".equalsIgnoreCase(v) && !"0".equals(v))
            .collect(Collectors.toList());
    }

    private void addDateParams(List<Object> params, LocalDate start, LocalDate end) {
        params.add(start == null ? null : Date.valueOf(start));
        params.add(end == null ? null : Date.valueOf(end));
    }

    private Comparator<ClassAggRow> resolveComparator(AllClassCheckListRequest request) {
        String order = request.getOrder() == null ? "" : request.getOrder().trim().toLowerCase(Locale.ROOT);
        String orderType = request.getOrderType() == null ? "desc" : request.getOrderType().trim().toLowerCase(Locale.ROOT);
        Comparator<ClassAggRow> comparator;
        if ("salescompare".equals(order)) {
            comparator = Comparator.comparing((ClassAggRow r) -> nvl(r.salesCompare));
        } else if ("salescomparerate".equals(order)) {
            comparator = Comparator.comparing((ClassAggRow r) -> nvl(r.salesCompareRate));
        } else if ("contributionrateper".equals(order)) {
            comparator = Comparator.comparing((ClassAggRow r) -> nvl(r.contributionRatePer));
        } else if ("salesper".equals(order)) {
            comparator = Comparator.comparing((ClassAggRow r) -> nvl(r.salesPer));
        } else {
            comparator = Comparator.comparing((ClassAggRow r) -> nvl(r.sales));
        }
        if (!"asc".equals(orderType)) {
            comparator = comparator.reversed();
        }
        return comparator.thenComparing(r -> r.classNo, Comparator.nullsLast(String::compareTo));
    }

    private AllClassCheckSalesChangeItemResponse toSalesChangeItem(ClassAggRow row, int classLevel) {
        AllClassCheckSalesChangeItemResponse item = new AllClassCheckSalesChangeItemResponse();
        item.setClassNo(row.classNo);
        item.setClassName(row.className);
        item.setClassLevel(classLevel);
        item.setSales(row.sales);
        item.setSalesCompare(row.salesCompare);
//        item.setGross(row.gross);
//        item.setSaleQuantity(row.saleQuantity);
        item.setSalesCompareRate(row.salesCompareRate);
        item.setContributionRatePer(row.contributionRatePer);
        item.setSalesPer(row.salesPer);
        fillRole(item, row.roleCode);
        return item;
    }

    private AllClassCheckScatterItemResponse toScatterItem(ClassAggRow row, int classLevel, BigDecimal splitX, BigDecimal splitY, Map<String, String> presetRoleMap) {
        AllClassCheckScatterItemResponse item = new AllClassCheckScatterItemResponse();
        item.setClassNo(row.classNo);
        item.setClassName(row.className);
        item.setClassLevel(classLevel);
        item.setSales(row.sales);
        item.setSalesCompare(row.salesCompare);
        item.setGross(row.gross);
        item.setSaleQuantity(row.saleQuantity);
        item.setSalesCompareRate(row.salesCompareRate);
        item.setContributionRatePer(row.contributionRatePer);
        item.setSalesPer(row.salesPer);
        fillRole(item, row.roleCode);
        String presetRole = normalizeRoleCode(presetRoleMap.get(row.classNo));
        String evaluatedRole = resolveMetricRoleCode(row, splitX, splitY);
        item.setPresetRole(presetRole);
        item.setPresetRoleName(roleName(presetRole));
        item.setEvaluatedRole(evaluatedRole);
        item.setEvaluatedRoleName(roleName(evaluatedRole));
        item.setRoleWarning(hasText(presetRole) && hasText(evaluatedRole) && !presetRole.equals(evaluatedRole));
        return item;
    }

    private void fillRole(AllClassCheckSalesChangeItemResponse item, String roleCode) {
        item.setClassRole(roleCode);
        item.setClassRoleName(roleName(roleCode));
        item.setClassRoleType(resolveRoleType(roleCode));
        item.setClassRoleTypeDescribe(null);
    }

    private void fillRole(AllClassCheckScatterItemResponse item, String roleCode) {
        item.setClassRole(roleCode);
        item.setClassRoleName(roleName(roleCode));
        item.setClassRoleType(resolveRoleType(roleCode));
        item.setClassRoleTypeDescribe(null);
    }

    private void fillRole(AllClassCheckSkuItemResponse item, String roleCode) {
        item.setClassRole(roleCode);
        item.setClassRoleName(roleName(roleCode));
        item.setClassRoleType(resolveRoleType(roleCode));
        item.setClassRoleTypeDescribe(null);
    }

    private void fillRole(AllClassCheckSkuDifferItemResponse item, String roleCode) {
        item.setClassRole(roleCode);
        item.setClassRoleName(roleName(roleCode));
        item.setClassRoleType(resolveRoleType(roleCode));
        item.setClassRoleTypeDescribe(null);
    }

    private String roleName(String roleCode) {
        return hasText(roleCode) ? ROLE_NAME_MAP.get(roleCode) : null;
    }

    private String resolveRoleType(String roleCode) {
        if (roleCode == null) {
            return null;
        }
        return switch (roleCode) {
            case "1" -> "明星品类";
            case "2" -> "幼童品类";
            case "3" -> "结构品类";
            case "4" -> "金牛品类";
            default -> null;
        };
    }

    private int countRoleWarnings(List<ClassAggRow> rows, BigDecimal splitX, BigDecimal splitY, Map<String, String> presetRoleMap) {
        int count = 0;
        for (ClassAggRow row : rows) {
            String presetRole = normalizeRoleCode(presetRoleMap.get(row.classNo));
            String evaluatedRole = resolveMetricRoleCode(row, splitX, splitY);
            if (hasText(presetRole) && hasText(evaluatedRole) && !presetRole.equals(evaluatedRole)) {
                count++;
            }
        }
        return count;
    }

    private String resolveMetricRoleCode(ClassAggRow row, BigDecimal splitX, BigDecimal splitY) {
        BigDecimal contributionRate = nvl(row.contributionRatePer);
        BigDecimal growth = nvl(row.salesCompareRate);
        if (contributionRate.compareTo(BigDecimal.ZERO) == 0 && growth.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        boolean highContribution = contributionRate.compareTo(nvl(splitX)) >= 0;
        boolean highGrowth = growth.compareTo(nvl(splitY)) >= 0;
        if (highContribution && highGrowth) {
            return "1";
        }
        if (!highContribution && highGrowth) {
            return "2";
        }
        if (!highContribution) {
            return "3";
        }
        return "4";
    }

    private int countSkuStructureWarnings(List<AllClassCheckSkuItemResponse> list) {
        int count = 0;
        for (AllClassCheckSkuItemResponse item : list) {
            if (nvl(item.getSkuDifference()).abs().compareTo(SKU_STRUCTURE_DIFF_WARNING_THRESHOLD) > 0) {
                count++;
            }
        }
        return count;
    }

    private int countSkuPresetWarnings(List<AllClassCheckSkuDifferItemResponse> list) {
        int count = 0;
        for (AllClassCheckSkuDifferItemResponse item : list) {
            if (item.getSkuDiffer() != null && item.getSkuDiffer() != 0) {
                count++;
            }
        }
        return count;
    }

    private List<Integer> buildScaleY(BigDecimal min, BigDecimal max) {
        int start = min == null ? 0 : min.setScale(0, RoundingMode.FLOOR).intValue();
        int end = max == null ? 0 : max.setScale(0, RoundingMode.CEILING).intValue();
        if (end <= start) {
            end = start + 1;
        }
        int mid = Math.max(start + 1, (start + end) / 2);
        return List.of(start, mid, end);
    }

    private boolean matchRoleFilter(String roleCode, List<String> requestedRoles) {
        List<String> normalized = normalizeList(requestedRoles);
        if (normalized.isEmpty()) {
            return true;
        }
        return normalized.contains(normalizeRoleCode(roleCode));
    }

    private String inferRoleCode(String classNo, String className) {
        String code = classNo == null ? "" : classNo.trim();
        if ("001".equals(code)) {
            return "1";
        }
        if ("010".equals(code) || "011".equals(code)) {
            return "4";
        }
        String name = className == null ? "" : className;
        if (containsAny(name, "低温", "鲜奶", "酸奶", "预制菜", "儿童牙膏", "沐浴露", "护肤", "功能性饮品", "咖啡", "茶", "坚果", "健康零食")) {
            return "1";
        }
        if (containsAny(name, "益智", "早教", "玩具", "维生素", "蛋白粉", "益生菌", "有机", "零添加", "儿童橄榄油", "自热锅", "速食面")) {
            return "2";
        }
        if (containsAny(name, "米", "面", "油", "饮用水", "碳酸饮料", "洗衣液", "洗洁精", "卫生纸", "卫生巾", "方便面")) {
            return "4";
        }
        return "3";
    }

    private String normalizeRoleCode(String roleCode) {
        if (roleCode == null) {
            return null;
        }
        return switch (roleCode.trim()) {
            case "STAR" -> "1";
            case "NURTURE" -> "2";
            case "STRUCTURAL" -> "3";
            case "CASH_COW" -> "4";
            default -> roleCode.trim();
        };
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private BigDecimal growthRate(BigDecimal current, BigDecimal compare) {
        BigDecimal c = nvl(current);
        BigDecimal p = nvl(compare);
        if (p.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return c.subtract(p).multiply(BigDecimal.valueOf(100)).divide(p, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal ratePercent(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return nvl(numerator).multiply(BigDecimal.valueOf(100)).divide(denominator, 4, RoundingMode.HALF_UP);
    }

    private int rateCount(BigDecimal sales, BigDecimal totalSales, BigDecimal totalCount) {
        if (totalSales == null || totalSales.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        return nvl(sales).multiply(totalCount).divide(totalSales, 0, RoundingMode.HALF_UP).intValue();
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            sum = sum.add(nvl(value));
        }
        return sum.divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int normalizeSize(Integer size) {
        return size == null || size < 1 ? 10 : Math.min(size, 200);
    }

    private int normalizeClassLevel(Integer classLevel) {
        if (classLevel == null || classLevel < 1 || classLevel > 5) {
            return DEFAULT_CLASS_LEVEL;
        }
        return classLevel;
    }

    private ClassLevelColumns resolveClassLevelColumns(int classLevel) {
        return switch (classLevel) {
            case 2 -> new ClassLevelColumns("two_class_no", "two_class_name");
            case 3 -> new ClassLevelColumns("three_class_no", "three_class_name");
            case 4 -> new ClassLevelColumns("four_class_no", "four_class_name");
            case 5 -> new ClassLevelColumns("five_class_no", "five_class_name");
            default -> new ClassLevelColumns("one_class_no", "one_class_name");
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean isAll(String value) {
        return value == null || value.isBlank() || "0".equals(value) || "all".equalsIgnoreCase(value);
    }

    private static class ClassAggRow {
        private String classNo;
        private String className;
        private BigDecimal currentSales;
        private BigDecimal currentGross;
        private BigDecimal currentQty;
        private BigDecimal compareSales;
        private BigDecimal compareGross;
        private BigDecimal compareQty;
        private BigDecimal sales;
        private BigDecimal salesCompare;
        private BigDecimal gross;
        private BigDecimal saleQuantity;
        private BigDecimal salesCompareRate;
        private BigDecimal contributionRatePer;
        private BigDecimal salesPer;
        private String roleCode;
    }

    private static class ClassSkuRow {
        private String classNo;
        private int classSku;
    }

    private static class ClassLevelColumns {
        private final String codeColumn;
        private final String nameColumn;

        private ClassLevelColumns(String codeColumn, String nameColumn) {
            this.codeColumn = codeColumn;
            this.nameColumn = nameColumn;
        }
    }
}

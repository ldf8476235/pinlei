package org.dromara.diagnosis.application.service.impl;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.request.CategoryNodeConfigUpdateRequest;
import org.dromara.diagnosis.api.request.CategoryTreeQueryRequest;
import org.dromara.diagnosis.api.response.CategoryClassTreeNodeResponse;
import org.dromara.diagnosis.api.response.CategoryFilterOptionsResponse;
import org.dromara.diagnosis.api.response.CategoryNodeConfigUpdateResponse;
import org.dromara.diagnosis.api.response.DictDetailResponse;
import org.dromara.diagnosis.api.response.DictOptionResponse;
import org.dromara.diagnosis.api.response.CategoryTreeNodeResponse;
import org.dromara.diagnosis.application.service.CategoryTreeService;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.dromara.diagnosis.infrastructure.mapper.CategoryTreeMapper;
import org.dromara.diagnosis.infrastructure.model.CategoryHierarchyRow;
import org.dromara.diagnosis.infrastructure.model.CategorySaleSkuRow;
import org.dromara.diagnosis.infrastructure.model.CategorySkuMetricRow;
import org.dromara.diagnosis.infrastructure.model.CategoryTreeQueryParam;
import org.dromara.diagnosis.infrastructure.model.DiagnosisDictRow;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Category tree service implementation.
 */
@Service
@RequiredArgsConstructor
public class CategoryTreeServiceImpl implements CategoryTreeService {

    private static final String ROOT_CLASS_NO = "0";

    private static final String ROOT_PARENT_CLASS_NO = "-1";

    private static final String DICT_TYPE_CATEGORY_ROLE = "class_role_type";

    private static final String DICT_TYPE_CLASS_SALES_STATUS_NO = "class_sales_status_no";

    private static final int SUGGEST_SKU_MAX = 999999;

    private final CategoryTreeMapper categoryTreeMapper;

    @Override
    public List<CategoryTreeNodeResponse> queryTree(CategoryTreeQueryRequest request) {
        CategoryTreeQueryRequest query = request == null ? new CategoryTreeQueryRequest() : request;
        CategoryTreeQueryParam param = toQueryParam(query);

        List<CategoryHierarchyRow> hierarchyRows = categoryTreeMapper.selectClassHierarchy();
        List<CategorySkuMetricRow> skuMetricRows = categoryTreeMapper.selectCategorySkuMetrics(param);
        List<CategorySaleSkuRow> saleSkuRows = categoryTreeMapper.selectCategorySaleSku(param);

        Map<String, CategorySkuMetricRow> skuMetricMap = skuMetricRows.stream()
            .filter(Objects::nonNull)
            .filter(it -> notBlank(it.getClassNo()))
            .collect(Collectors.toMap(CategorySkuMetricRow::getClassNo, it -> it, (a, b) -> a));
        Map<String, Integer> saleSkuMap = saleSkuRows.stream()
            .filter(Objects::nonNull)
            .filter(it -> notBlank(it.getClassNo()))
            .collect(Collectors.toMap(CategorySaleSkuRow::getClassNo, it -> nvl(it.getSaleSku()), Integer::sum));

        MutableNode root = buildTree(hierarchyRows);
        fillDirectMetrics(root, skuMetricMap, saleSkuMap);
        recalculateFromChildren(root);

        MutableNode filtered = applyFilters(root, query);
        if (filtered == null) {
            return List.of();
        }
        recalculateFromChildren(filtered);
        normalizeLeafSubClass(filtered);
        return List.of(toResponse(filtered));
    }

    @Override
    public List<CategoryClassTreeNodeResponse> queryClassTree(Integer level) {
        int maxLevel = normalizeLegacyLevel(level);

        List<CategoryHierarchyRow> hierarchyRows = categoryTreeMapper.selectClassHierarchy();
        CategoryTreeQueryParam allParam = new CategoryTreeQueryParam();
        List<CategorySaleSkuRow> saleSkuRows = categoryTreeMapper.selectCategorySaleSku(allParam);

        Map<String, Integer> saleSkuMap = saleSkuRows.stream()
            .filter(Objects::nonNull)
            .filter(it -> notBlank(it.getClassNo()))
            .collect(Collectors.toMap(CategorySaleSkuRow::getClassNo, it -> nvl(it.getSaleSku()), Integer::sum));

        MutableNode root = buildTree(hierarchyRows);
        walk(root, node -> {
            if (ROOT_CLASS_NO.equals(node.getClassNo())) {
                return;
            }
            node.setSaleSku(nvl(saleSkuMap.get(node.getClassNo())));
        });
        recalculateSaleSkuOnly(root);

        return List.of(toLegacyResponse(root, maxLevel));
    }

    @Override
    public CategoryFilterOptionsResponse queryFilterOptions() {
        List<DiagnosisDictRow> roleRows = categoryTreeMapper.selectDictRowsByType(DICT_TYPE_CATEGORY_ROLE);
        List<DiagnosisDictRow> salesStatusRows = categoryTreeMapper.selectDictRowsByType(DICT_TYPE_CLASS_SALES_STATUS_NO);

        CategoryFilterOptionsResponse response = new CategoryFilterOptionsResponse();
        response.setCategoryLevels(buildFixedCategoryLevels());
        response.setCategoryRoles(toDictOptions(roleRows));
        response.setSkuAbnormal(buildSkuAbnormalOptions());
        response.setClassSalesStatusNo(toDictDetails(salesStatusRows));
        return response;
    }
    @Override
    public CategoryNodeConfigUpdateResponse updateNodeConfig(CategoryNodeConfigUpdateRequest request) {
        if (request == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "request is required");
        }

        String storeNo = trim(request.getStoreNo());
        String classNo = trim(request.getClassNo());
        String roleNo = trim(request.getRoleNo());
        Integer suggestSaleSku = request.getSuggestSaleSku();

        if (!notBlank(storeNo)) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "storeNo is required");
        }
        if (!notBlank(classNo)) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "classNo is required");
        }
        if (!notBlank(roleNo)) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "roleNo is required");
        }
        if (suggestSaleSku == null || suggestSaleSku < 0 || suggestSaleSku > SUGGEST_SKU_MAX) {
            throw new DiagnosisBizException(
                DiagnosisErrorCode.INVALID_ARGUMENT,
                "suggestSaleSku must be an integer between 0 and " + SUGGEST_SKU_MAX
            );
        }

        Integer classCnt = categoryTreeMapper.countClassByClassNo(classNo);
        if (classCnt == null || classCnt <= 0) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "classNo does not exist");
        }

        List<DiagnosisDictRow> roleRows = categoryTreeMapper.selectDictRowsByType(DICT_TYPE_CATEGORY_ROLE);
        Map<String, String> roleMap = roleRows.stream()
            .filter(Objects::nonNull)
            .filter(it -> notBlank(it.getDictValue()))
            .collect(Collectors.toMap(it -> trim(it.getDictValue()), it -> trim(it.getDictLabel()), (a, b) -> a));
        if (!roleMap.containsKey(roleNo)) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "roleNo is not in dictionary");
        }

        categoryTreeMapper.upsertCategoryNodeConfig(storeNo, classNo, roleNo, suggestSaleSku);

        CategorySkuMetricRow row = categoryTreeMapper.selectCategoryNodeConfig(storeNo, classNo);
        CategoryNodeConfigUpdateResponse response = new CategoryNodeConfigUpdateResponse();
        response.setStoreNo(storeNo);
        response.setClassNo(classNo);
        response.setRoleNo(row == null || !notBlank(row.getRoleNo()) ? roleNo : trim(row.getRoleNo()));
        response.setSuggestSaleSku(row == null ? suggestSaleSku : nvl(row.getSuggestSaleSku()));
        response.setRoleType(roleMap.getOrDefault(response.getRoleNo(), toRoleType(response.getRoleNo())));
        return response;
    }

    private List<DictOptionResponse> buildFixedCategoryLevels() {
        return List.of(
            dictOption("\u4e00\u7ea7\u54c1\u7c7b", "1"),
            dictOption("\u4e8c\u7ea7\u54c1\u7c7b", "2"),
            dictOption("\u4e09\u7ea7\u54c1\u7c7b", "3"),
            dictOption("\u56db\u7ea7\u54c1\u7c7b", "4")
        );
    }

    private DictOptionResponse dictOption(String label, String value) {
        DictOptionResponse option = new DictOptionResponse();
        option.setLabel(label);
        option.setValue(value);
        return option;
    }

    private List<DictOptionResponse> buildSkuAbnormalOptions() {
        return List.of(
            dictOption("\u5168\u90e8", "0"),
            dictOption("\u5b9e\u9645\u6570\u6bd4\u7cfb\u7edf\u5efa\u8bae\u5c11", "1"),
            dictOption("\u5b9e\u9645\u6570\u6bd4\u7cfb\u7edf\u5efa\u8bae\u591a", "2"),
            dictOption("\u5b9e\u9645\u6570\u4e0e\u9884\u8bbe\u6807\u51c6\u4e0d\u4e00\u81f4", "3")
        );
    }

    private CategoryTreeQueryParam toQueryParam(CategoryTreeQueryRequest request) {
        CategoryTreeQueryParam param = new CategoryTreeQueryParam();
        param.setRetailTypeId(normalizeFilterValue(request.getRetailTypeId()));
        param.setBusinessCircleId(normalizeFilterValue(request.getBusinessCircleId()));
        param.setDeptGroupId(normalizeFilterValue(request.getDeptGroupId()));
        param.setStoreNo(normalizeFilterValue(request.getStoreNo()));
        return param;
    }

    private MutableNode buildTree(List<CategoryHierarchyRow> rows) {
        Map<String, MutableNode> nodeMap = new HashMap<>();
        MutableNode root = new MutableNode();
        root.setClassNo(ROOT_CLASS_NO);
        root.setClassName("\u5168\u90e8");
        root.setParentClassNo(ROOT_PARENT_CLASS_NO);
        root.setClassLevel(0);
        nodeMap.put(ROOT_CLASS_NO, root);

        for (CategoryHierarchyRow row : rows) {
            if (row == null || !notBlank(row.getClassNo())) {
                continue;
            }
            MutableNode node = nodeMap.computeIfAbsent(row.getClassNo(), key -> new MutableNode());
            node.setClassNo(trim(row.getClassNo()));
            node.setClassName(trim(row.getClassName()));
            node.setParentClassNo(notBlank(row.getParentClassNo()) ? trim(row.getParentClassNo()) : ROOT_CLASS_NO);
            node.setClassLevel(nvl(row.getClassLevel()));
        }

        for (MutableNode node : nodeMap.values()) {
            if (ROOT_CLASS_NO.equals(node.getClassNo())) {
                continue;
            }
            String parentClassNo = notBlank(node.getParentClassNo()) ? node.getParentClassNo() : ROOT_CLASS_NO;
            MutableNode parent = nodeMap.get(parentClassNo);
            if (parent == null) {
                parent = root;
            }
            parent.getSubClass().add(node);
        }
        sortTree(root);
        return root;
    }

    private void sortTree(MutableNode node) {
        node.getSubClass().sort(Comparator.comparing(MutableNode::getClassNo, Comparator.nullsLast(String::compareTo)));
        for (MutableNode child : node.getSubClass()) {
            sortTree(child);
        }
    }

    private void fillDirectMetrics(MutableNode root, Map<String, CategorySkuMetricRow> skuMetricMap, Map<String, Integer> saleSkuMap) {
        walk(root, node -> {
            if (ROOT_CLASS_NO.equals(node.getClassNo())) {
                return;
            }
            CategorySkuMetricRow metric = skuMetricMap.get(node.getClassNo());
            if (metric != null) {
                node.setSuggestSaleSku(nvl(metric.getSuggestSaleSku()));
                node.setSysSuggestSaleSku(nvl(metric.getSysSuggestSaleSku()));
                node.setRoleNo(trim(metric.getRoleNo()));
            }
            node.setSaleSku(nvl(saleSkuMap.get(node.getClassNo())));
            if (node.getSysSuggestSaleSku() == null) {
                node.setSysSuggestSaleSku(nvl(node.getSuggestSaleSku()));
            }
            node.setRoleType(toRoleType(node.getRoleNo()));
            node.setSkuDiffer(calcSkuDiffer(node));
        });
    }

    private MutableNode applyFilters(MutableNode root, CategoryTreeQueryRequest query) {
        int level = normalizeClassLevel(query.getClassLevel());
        Set<String> classNoSet = toClassNoSet(query.getClassNo(), level);
        // If incoming classNo cannot match current level (e.g. 1/2/3 vs 001/002/003),
        // degrade to "no classNo filter" to avoid returning only root node.
        if (!classNoSet.isEmpty() && !classNoSet.contains(ROOT_CLASS_NO) && !hasClassNoMatch(root, classNoSet, level)) {
            classNoSet.clear();
        }
        Set<String> roleSet = toNormalizedSet(query.getClassRole());
        Set<String> abnormalSet = toNormalizedSet(query.getSkuAbnormal());
        MutableNode filtered = filterNode(root, false, level, classNoSet, roleSet, abnormalSet);
        if (filtered == null) {
            return root;
        }
        if (filtered.getSubClass() == null || filtered.getSubClass().isEmpty()) {
            // Keep tree available for UI when filter combination is over-strict.
            return root;
        }
        return filtered;
    }

    private MutableNode filterNode(MutableNode source, boolean ancestorSelected, int targetLevel,
                                   Set<String> classNoSet, Set<String> roleSet, Set<String> abnormalSet) {
        boolean currentSelected = ancestorSelected
            || (source.getClassLevel() != null && source.getClassLevel() == targetLevel && classNoSet.contains(source.getClassNo()));

        List<MutableNode> children = new ArrayList<>();
        for (MutableNode child : source.getSubClass()) {
            MutableNode filteredChild = filterNode(child, currentSelected, targetLevel, classNoSet, roleSet, abnormalSet);
            if (filteredChild != null) {
                children.add(filteredChild);
            }
        }

        boolean classNoPass = matchClassNo(source, targetLevel, classNoSet, ancestorSelected, currentSelected);
        boolean rolePass = matchRole(source, roleSet);
        boolean abnormalPass = matchSkuAbnormal(source, abnormalSet);

        if (!classNoSet.isEmpty() && !ROOT_CLASS_NO.equals(source.getClassNo()) && nvl(source.getClassLevel()) < targetLevel && children.isEmpty()) {
            classNoPass = false;
        }

        boolean keepSelf = classNoPass && rolePass && abnormalPass;
        boolean keepByChildren = !children.isEmpty();
        boolean keep = keepSelf || keepByChildren || ROOT_CLASS_NO.equals(source.getClassNo());
        if (!keep) {
            return null;
        }

        MutableNode copy = shallowCopy(source);
        copy.setSubClass(children);
        return copy;
    }

    private boolean matchClassNo(MutableNode node, int targetLevel, Set<String> classNoSet, boolean ancestorSelected, boolean currentSelected) {
        if (classNoSet.isEmpty() || classNoSet.contains(ROOT_CLASS_NO)) {
            return true;
        }
        int level = nvl(node.getClassLevel());
        if (level < targetLevel) {
            return true;
        }
        if (level == targetLevel) {
            return classNoSet.contains(node.getClassNo());
        }
        return ancestorSelected || currentSelected;
    }

    private boolean matchRole(MutableNode node, Set<String> roleSet) {
        if (ROOT_CLASS_NO.equals(node.getClassNo())) {
            return true;
        }
        if (roleSet.isEmpty() || roleSet.contains("0")) {
            return true;
        }
        return roleSet.contains(trim(node.getRoleNo()));
    }

    private boolean matchSkuAbnormal(MutableNode node, Set<String> abnormalSet) {
        if (ROOT_CLASS_NO.equals(node.getClassNo())) {
            return true;
        }
        if (abnormalSet.isEmpty() || abnormalSet.contains("0")) {
            return true;
        }
        Integer differ = node.getSkuDiffer();
        if (differ == null) {
            return false;
        }
        if (abnormalSet.contains("1") && differ < 0) {
            return true;
        }
        if (abnormalSet.contains("2") && differ > 0) {
            return true;
        }
        return abnormalSet.contains("3") && differ != 0;
    }

    private boolean hasClassNoMatch(MutableNode root, Set<String> classNoSet, int level) {
        if (classNoSet.isEmpty()) {
            return true;
        }
        List<MutableNode> stack = new ArrayList<>();
        stack.add(root);
        while (!stack.isEmpty()) {
            MutableNode node = stack.remove(stack.size() - 1);
            if (nvl(node.getClassLevel()) == level && classNoSet.contains(node.getClassNo())) {
                return true;
            }
            if (node.getSubClass() != null && !node.getSubClass().isEmpty()) {
                stack.addAll(node.getSubClass());
            }
        }
        return false;
    }

    private void recalculateFromChildren(MutableNode node) {
        for (MutableNode child : node.getSubClass()) {
            recalculateFromChildren(child);
        }
        if (!node.getSubClass().isEmpty()) {
            if (ROOT_CLASS_NO.equals(node.getClassNo()) || node.getSuggestSaleSku() == null) {
                node.setSuggestSaleSku(sum(node.getSubClass(), MutableNode::getSuggestSaleSku));
            }
            node.setSaleSku(sum(node.getSubClass(), MutableNode::getSaleSku));
            if (ROOT_CLASS_NO.equals(node.getClassNo()) || node.getSysSuggestSaleSku() == null) {
                node.setSysSuggestSaleSku(sum(node.getSubClass(), MutableNode::getSysSuggestSaleSku));
            }
            if (!notBlank(node.getRoleNo())) {
                node.setRoleNo(node.getSubClass().stream().map(MutableNode::getRoleNo).filter(this::notBlank).findFirst().orElse(null));
            }
            node.setRoleType(toRoleType(node.getRoleNo()));
            node.setSkuDiffer(calcSkuDiffer(node));
        } else {
            if (node.getSuggestSaleSku() == null) {
                node.setSuggestSaleSku(0);
            }
            if (node.getSaleSku() == null) {
                node.setSaleSku(0);
            }
            if (node.getSysSuggestSaleSku() == null) {
                node.setSysSuggestSaleSku(node.getSuggestSaleSku());
            }
            node.setRoleType(toRoleType(node.getRoleNo()));
            node.setSkuDiffer(calcSkuDiffer(node));
        }
    }

    private void normalizeLeafSubClass(MutableNode node) {
        if (node.getSubClass().isEmpty()) {
            node.setSubClass(null);
            return;
        }
        for (MutableNode child : node.getSubClass()) {
            normalizeLeafSubClass(child);
        }
    }

    private CategoryTreeNodeResponse toResponse(MutableNode node) {
        CategoryTreeNodeResponse response = new CategoryTreeNodeResponse();
        response.setClassNo(node.getClassNo());
        response.setClassName(node.getClassName());
        response.setParentClassNo(node.getParentClassNo());
        response.setClassLevel(node.getClassLevel());
        response.setSuggestSaleSku(nvl(node.getSuggestSaleSku()));
        response.setSaleSku(nvl(node.getSaleSku()));
        response.setRoleNo(node.getRoleNo());
        response.setRoleType(node.getRoleType());
        response.setSkuDiffer(node.getSkuDiffer());
        response.setSysSuggestSaleSku(nvl(node.getSysSuggestSaleSku()));
        if (node.getSubClass() != null) {
            response.setSubClass(node.getSubClass().stream().map(this::toResponse).toList());
        } else {
            response.setSubClass(null);
        }
        return response;
    }

    private CategoryClassTreeNodeResponse toLegacyResponse(MutableNode node, int maxLevel) {
        CategoryClassTreeNodeResponse response = new CategoryClassTreeNodeResponse();
        String classNo = node.getClassNo();
        String className = trim(node.getClassName());
        Integer classLevel = nvl(node.getClassLevel());

        response.setLevel(classNo);
        response.setFlevel(notBlank(node.getParentClassNo()) ? node.getParentClassNo() : ROOT_PARENT_CLASS_NO);
        response.setLevelFlag(classLevel);
        response.setClassName(ROOT_CLASS_NO.equals(classNo) ? "\u5168\u90e8" : classNo + nvlString(className));
        response.setLabelName(ROOT_CLASS_NO.equals(classNo) ? "\u5168\u90e8" : nvlString(className));
        response.setStateFlag(0);
        response.setSku(ROOT_CLASS_NO.equals(classNo) ? null : node.getSaleSku());
        response.setUpdateFlag(null);
        response.setUpdateTime(null);
        response.setCanChoose(true);
        response.setId(classNo);
        response.setLabel(response.getClassName());

        if (classLevel >= maxLevel || node.getSubClass() == null || node.getSubClass().isEmpty()) {
            response.setChildren(null);
            return response;
        }

        List<CategoryClassTreeNodeResponse> children = node.getSubClass().stream()
            .filter(Objects::nonNull)
            .filter(child -> nvl(child.getClassLevel()) <= maxLevel)
            .map(child -> toLegacyResponse(child, maxLevel))
            .toList();
        response.setChildren(children.isEmpty() ? null : children);
        return response;
    }

    private List<DictOptionResponse> toDictOptions(List<DiagnosisDictRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        return rows.stream().filter(Objects::nonNull).map(row -> {
            DictOptionResponse item = new DictOptionResponse();
            item.setLabel(trim(row.getDictLabel()));
            item.setValue(trim(row.getDictValue()));
            return item;
        }).filter(item -> notBlank(item.getLabel()) && notBlank(item.getValue())).toList();
    }

    private List<DictDetailResponse> toDictDetails(List<DiagnosisDictRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        return rows.stream().filter(Objects::nonNull).map(row -> {
            DictDetailResponse item = new DictDetailResponse();
            item.setCreateTime(trim(row.getCreateTime()));
            item.setDictId(row.getDictId());
            item.setId(row.getDictCode());
            item.setLabel(trim(row.getDictLabel()));
            item.setSort(row.getDictSort() == null ? null : String.valueOf(row.getDictSort()));
            item.setValue(trim(row.getDictValue()));
            return item;
        }).toList();
    }

    private void recalculateSaleSkuOnly(MutableNode node) {
        for (MutableNode child : node.getSubClass()) {
            recalculateSaleSkuOnly(child);
        }
        if (!node.getSubClass().isEmpty()) {
            node.setSaleSku(sum(node.getSubClass(), MutableNode::getSaleSku));
        } else if (node.getSaleSku() == null) {
            node.setSaleSku(0);
        }
    }

    private MutableNode shallowCopy(MutableNode source) {
        MutableNode node = new MutableNode();
        node.setClassNo(source.getClassNo());
        node.setClassName(source.getClassName());
        node.setParentClassNo(source.getParentClassNo());
        node.setClassLevel(source.getClassLevel());
        node.setSuggestSaleSku(source.getSuggestSaleSku());
        node.setSaleSku(source.getSaleSku());
        node.setRoleNo(source.getRoleNo());
        node.setRoleType(source.getRoleType());
        node.setSkuDiffer(source.getSkuDiffer());
        node.setSysSuggestSaleSku(source.getSysSuggestSaleSku());
        return node;
    }

    private void walk(MutableNode node, java.util.function.Consumer<MutableNode> consumer) {
        consumer.accept(node);
        for (MutableNode child : node.getSubClass()) {
            walk(child, consumer);
        }
    }

    private Integer sum(List<MutableNode> nodes, java.util.function.Function<MutableNode, Integer> getter) {
        int total = 0;
        for (MutableNode node : nodes) {
            total += nvl(getter.apply(node));
        }
        return total;
    }

    private Integer calcSkuDiffer(MutableNode node) {
        if (node.getSaleSku() == null || node.getSuggestSaleSku() == null) {
            return null;
        }
        return node.getSaleSku() - node.getSuggestSaleSku();
    }

    private Set<String> toNormalizedSet(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return new HashSet<>();
        }
        return values.stream().filter(this::notBlank).map(this::trim).collect(Collectors.toSet());
    }

    private Set<String> toClassNoSet(Collection<String> values, int level) {
        Set<String> result = new HashSet<>();
        if (values == null || values.isEmpty()) {
            return result;
        }
        int expectedLength = classNoLengthByLevel(level);
        for (String value : values) {
            if (!notBlank(value)) {
                continue;
            }
            String v = trim(value);
            if ("0".equals(v)) {
                result.add(v);
                continue;
            }
            if (isDigits(v) && expectedLength > 0 && v.length() < expectedLength) {
                result.add(leftPadZero(v, expectedLength));
                continue;
            }
            result.add(v);
        }
        return result;
    }

    private int normalizeClassLevel(Integer classLevel) {
        if (classLevel == null || classLevel < 1 || classLevel > 5) {
            return 1;
        }
        return classLevel;
    }

    private int normalizeLegacyLevel(Integer level) {
        if (level == null || level < 1 || level > 5) {
            return 4;
        }
        return level;
    }

    private int classNoLengthByLevel(int level) {
        return switch (level) {
            case 1 -> 3;
            case 2 -> 5;
            case 3 -> 7;
            case 4 -> 9;
            case 5 -> 11;
            default -> 0;
        };
    }

    private boolean isDigits(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return !value.isEmpty();
    }

    private String leftPadZero(String value, int length) {
        if (value.length() >= length) {
            return value;
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = value.length(); i < length; i++) {
            sb.append('0');
        }
        sb.append(value);
        return sb.toString();
    }

    private String toRoleType(String roleNo) {
        if (!notBlank(roleNo)) {
            return "";
        }
        return switch (roleNo.trim()) {
            case "0" -> "\u5168\u90e8";
            case "1" -> "\u660e\u661f\u54c1\u7c7b";
            case "2" -> "\u95ee\u9898\u54c1\u7c7b";
            case "3" -> "\u7ed3\u6784\u54c1\u7c7b";
            case "4" -> "\u91d1\u725b\u54c1\u7c7b";
            case "5" -> "\u6218\u7565\u54c1\u7c7b";
            default -> "";
        };
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String nvlString(String value) {
        return value == null ? "" : value;
    }

    private int nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private String normalizeFilterValue(String value) {
        String v = trim(value);
        if (!notBlank(v) || "0".equals(v) || "all".equalsIgnoreCase(v)) {
            return null;
        }
        return v;
    }

    @Data
    private static class MutableNode {
        private String classNo;
        private String className;
        private String parentClassNo;
        private Integer classLevel;
        private Integer suggestSaleSku;
        private Integer saleSku;
        private String roleNo;
        private String roleType;
        private Integer skuDiffer;
        private Integer sysSuggestSaleSku;
        private List<MutableNode> subClass = new ArrayList<>();
    }
}

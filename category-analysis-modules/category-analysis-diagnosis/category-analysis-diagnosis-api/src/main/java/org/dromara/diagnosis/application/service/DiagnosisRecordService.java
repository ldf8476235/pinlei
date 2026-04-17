package org.dromara.diagnosis.application.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.request.DiagnosisRecordQueryRequest;
import org.dromara.diagnosis.api.response.DiagnosisRecordItemResponse;
import org.dromara.diagnosis.api.response.DiagnosisRecordPageResponse;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisRecordQueryRow;
import org.dromara.common.core.constant.TenantConstants;
import org.dromara.common.tenant.helper.TenantHelper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiagnosisRecordService {

    private final DiagnosisPrecomputeMapper precomputeMapper;

    public DiagnosisRecordPageResponse queryRecords(DiagnosisRecordQueryRequest request) {
        DiagnosisRecordQueryRequest actual = request == null ? new DiagnosisRecordQueryRequest() : request;
        String tenantId = resolveTenantId();
        int pageNum = actual.getPageNum() == null || actual.getPageNum() < 1 ? 1 : actual.getPageNum();
        int pageSize = actual.getPageSize() == null || actual.getPageSize() < 1 ? 20 : Math.min(actual.getPageSize(), 200);
        int offset = (pageNum - 1) * pageSize;

        Long total = precomputeMapper.countDiagnosisRecordRows(tenantId, actual);
        List<DiagnosisRecordQueryRow> rows = total == null || total <= 0
            ? new ArrayList<>()
            : precomputeMapper.selectDiagnosisRecordRows(tenantId, actual, offset, pageSize);
        if ((rows == null || rows.isEmpty()) && total != null && total > 0) {
            rows = precomputeMapper.selectDiagnosisRecordRowsFallback(tenantId, actual, offset, pageSize);
        }

        DiagnosisRecordPageResponse response = new DiagnosisRecordPageResponse();
        response.setRows(rows.stream().map(this::toItem).toList());
        response.setTotal(total == null ? 0L : total);
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        return response;
    }

    private String resolveTenantId() {
        String tenantId = TenantHelper.getTenantId();
        return (tenantId == null || tenantId.isBlank()) ? TenantConstants.DEFAULT_TENANT_ID : tenantId;
    }

    private DiagnosisRecordItemResponse toItem(DiagnosisRecordQueryRow row) {
        DiagnosisRecordItemResponse item = new DiagnosisRecordItemResponse();
        item.setRecordId("JOB-" + row.getJobId());
        item.setJobId(row.getJobId());
        item.setRequestHash(row.getRequestHash());
        item.setDataVersion(row.getDataVersion());
        item.setClassLevel(row.getClassLevel());
        item.setClassNo(row.getClassNo());
        item.setClassName(row.getClassName());
        item.setDeptId(row.getDeptId());
        item.setDeptName(resolveScopeName(row.getDeptName(), row.getDeptId(), "\u5168\u90e8"));
        item.setRetailTypeId(row.getRetailTypeId());
        item.setRetailTypeName(resolveScopeName(row.getRetailTypeName(), row.getRetailTypeId(), "\u5168\u90e8"));
        item.setBusinessCircleId(row.getBusinessCircleId());
        item.setBusinessCircleName(resolveScopeName(row.getBusinessCircleName(), row.getBusinessCircleId(), "\u5168\u90e8"));
        item.setDeptGroupId(row.getDeptGroupId());
        item.setDeptGroupName(resolveScopeName(row.getDeptGroupName(), row.getDeptGroupId(), "\u5168\u90e8"));
        item.setStoreNo(row.getStoreNo());
        item.setStoreRangeName(resolveStoreRangeName(row));
        item.setPeriodStart(row.getPeriodStart());
        item.setPeriodEnd(row.getPeriodEnd());
        item.setCompareStart(row.getCompareStart());
        item.setCompareEnd(row.getCompareEnd());
        item.setCreateBy(row.getSubmittedBy());
        item.setCreateByName(resolveCreatorName(row));
        item.setCreateTime(row.getSubmittedTime());
        item.setProgressPercent(row.getProgressPercent());
        item.setCurrentStage(row.getCurrentStage());

        boolean canViewReport = row.getSnapshotId() != null;
        item.setCanViewReport(canViewReport);
        item.setReady(canViewReport);
        item.setStatus(canViewReport ? "SUCCESS" : normalizeStatus(row.getStatusCode()));
        item.setStatusLabel(canViewReport ? "\u5df2\u751f\u6210" : toStatusLabel(row.getStatusCode()));
        return item;
    }

    private String resolveScopeName(String name, String id, String defaultValue) {
        if (hasText(name)) {
            return name.trim();
        }
        if (hasText(id)) {
            return id.trim();
        }
        return defaultValue;
    }

    private String resolveStoreRangeName(DiagnosisRecordQueryRow row) {
        if (!hasText(row.getStoreNo())) {
            return "\u5168\u5e97";
        }
        if (hasText(row.getStoreName())) {
            return row.getStoreNo().trim() + " " + row.getStoreName().trim();
        }
        return row.getStoreNo().trim();
    }

    private String resolveCreatorName(DiagnosisRecordQueryRow row) {
        if (hasText(row.getCreateByName())) {
            return row.getCreateByName().trim();
        }
        if (row.getSubmittedBy() != null) {
            return String.valueOf(row.getSubmittedBy());
        }
        return "\u7cfb\u7edf";
    }

    private String normalizeStatus(String status) {
        return hasText(status) ? status : "PENDING";
    }

    private String toStatusLabel(String status) {
        if (!hasText(status)) {
            return "\u5f85\u8ba1\u7b97";
        }
        String upper = status.toUpperCase();
        switch (upper) {
            case "RUNNING":
            case "RETRYING":
                return "\u8ba1\u7b97\u4e2d";
            case "SUCCESS":
                return "\u5df2\u751f\u6210";
            case "FAILED":
                return "\u751f\u6210\u5931\u8d25";
            case "STOPPED":
                return "\u5df2\u505c\u6b62";
            default:
                return "\u5f85\u8ba1\u7b97";
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

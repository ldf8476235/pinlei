package org.dromara.diagnosis.application.service;

import org.dromara.diagnosis.api.request.DiagnosisSessionCreateRequest;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.Locale;

/**
 * 诊断查询哈希服务.
 */
@Service
public class DiagnosisQueryHashService {

    public String buildQueryHash(DiagnosisSessionCreateRequest request) {
        try {
            String classLevel = normalizeClassLevel(request.getClassLevel());
            String classNo = normalizeCommon(request.getClassNo());
            String deptId = normalizeCommon(request.getDeptId());
            String retailTypeId = normalizeCommon(request.getRetailTypeId());
            String businessCircleId = normalizeCommon(request.getBusinessCircleId());
            String deptGroupId = normalizeCommon(request.getDeptGroupId());
            String storeNo = normalizeStoreNo(request.getStoreNo());
            String extraFilterJson = normalizeCommon(request.getExtraFilterJson());
            String periodStart = normalizeDate(request.getPeriodStart());
            String periodEnd = normalizeDate(request.getPeriodEnd());
            String compareStart = normalizeDate(request.getCompareStart());
            String compareEnd = normalizeDate(request.getCompareEnd());

            String text = "000000|"
                + nvl(classLevel) + "|"
                + nvl(classNo) + "|"
                + nvl(deptId) + "|"
                + nvl(retailTypeId) + "|"
                + nvl(businessCircleId) + "|"
                + nvl(deptGroupId) + "|"
                + nvl(storeNo) + "|"
                + nvl(periodStart) + "|"
                + nvl(periodEnd) + "|"
                + nvl(compareStart) + "|"
                + nvl(compareEnd) + "|"
                + nvl(extraFilterJson);

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format(Locale.ROOT, "%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("构建查询哈希失败", ex);
        }
    }

    private String normalizeCommon(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        if (text.isEmpty() || "0".equals(text)) {
            return null;
        }
        return text;
    }

    private String normalizeStoreNo(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        if (text.isEmpty() || "0".equals(text) || "all".equalsIgnoreCase(text)) {
            return null;
        }
        return text;
    }

    private String normalizeClassLevel(Integer value) {
        if (value == null || value <= 0) {
            return null;
        }
        return String.valueOf(value);
    }

    private String normalizeDate(LocalDate value) {
        return value == null ? null : value.toString();
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }
}

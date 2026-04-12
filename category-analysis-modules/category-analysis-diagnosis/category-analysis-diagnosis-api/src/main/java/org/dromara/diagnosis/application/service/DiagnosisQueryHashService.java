package org.dromara.diagnosis.application.service;

import org.dromara.diagnosis.api.request.DiagnosisSessionCreateRequest;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

/**
 * 诊断查询哈希服务.
 */
@Service
public class DiagnosisQueryHashService {

    public String buildQueryHash(DiagnosisSessionCreateRequest request) {
        try {
            // 与批处理落快照的 queryHash 保持一致，避免会话查不到结果。
            String text = "000000|"
                + request.getPeriodStart() + "|"
                + request.getPeriodEnd() + "|"
                + request.getCompareStart() + "|"
                + request.getCompareEnd();
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
}


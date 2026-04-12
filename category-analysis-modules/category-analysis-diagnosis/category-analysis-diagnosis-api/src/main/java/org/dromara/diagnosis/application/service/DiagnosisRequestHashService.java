package org.dromara.diagnosis.application.service;

import org.dromara.common.json.utils.JsonUtils;
import org.dromara.diagnosis.api.request.PrecomputeJobCreateRequest;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 任务请求幂等哈希服务.
 */
@Service
public class DiagnosisRequestHashService {

    public String buildHash(PrecomputeJobCreateRequest request) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("module", request.getModule());
            payload.put("readRangeType", request.getReadRangeType());
            payload.put("readStart", request.getReadStart());
            payload.put("readEnd", request.getReadEnd());
            payload.put("compareStart", request.getCompareStart());
            payload.put("compareEnd", request.getCompareEnd());
            payload.put("windowTypes", request.getWindowTypes());
            payload.put("maxRetry", request.getMaxRetry());
            payload.put("requestJson", request.getRequestJson());
            String text = JsonUtils.toJsonString(payload);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format(Locale.ROOT, "%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("构建请求哈希失败", ex);
        }
    }
}

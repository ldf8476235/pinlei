package org.dromara.diagnosis.application.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.request.PriceBandConfigUpdateRequest;
import org.dromara.diagnosis.api.response.PriceBandConfigUpdateResponse;
import org.dromara.diagnosis.application.batch.model.DiagnosisFinalizeContext;
import org.dromara.diagnosis.application.batch.service.DiagnosisFinalizeSupport;
import org.dromara.diagnosis.application.batch.service.DiagnosisPriceBandFinalizeService;
import org.dromara.diagnosis.application.config.DiagnosisCacheProperties;
import org.dromara.diagnosis.application.model.DiagnosisSessionCacheModel;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPriceBandConfigMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisOverviewSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeWindowRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPriceBandConfigRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceBandConfigService {

    private static final String TENANT_ID = "000000";
    private static final String SYSTEM_USER = "system";

    private final DiagnosisSessionCacheStore sessionCacheStore;
    private final DiagnosisCacheProperties cacheProperties;
    private final DiagnosisSnapshotMapper snapshotMapper;
    private final DiagnosisPrecomputeMapper precomputeMapper;
    private final DiagnosisPriceBandConfigMapper priceBandConfigMapper;
    private final DiagnosisFinalizeSupport finalizeSupport;
    private final DiagnosisPriceBandFinalizeService diagnosisPriceBandFinalizeService;

    @Transactional(rollbackFor = Exception.class)
    public PriceBandConfigUpdateResponse updateConfig(PriceBandConfigUpdateRequest request) {
        DiagnosisSessionCacheModel session = getSession(request.getSessionId());
        DiagnosisOverviewSnapshotRow overview = resolveOverviewSnapshot(request.getSessionId(), session);
        List<DiagnosisPriceBandConfigRow> rows = buildConfigRows(session.getQueryHash(), request);

        priceBandConfigMapper.deleteActiveConfig(TENANT_ID, session.getQueryHash());
        if (!rows.isEmpty()) {
            priceBandConfigMapper.batchInsertConfig(rows);
        }

        String status = "CONFIG_SAVED";
        if (session.getJobId() != null) {
            DiagnosisPrecomputeJobRow job = precomputeMapper.selectJobById(TENANT_ID, session.getJobId());
            List<DiagnosisPrecomputeWindowRow> windows = precomputeMapper.selectWindowsByJobId(TENANT_ID, session.getJobId());
            DiagnosisPrecomputeWindowRow window = pickWindowByVersion(windows, overview == null ? session.getDataVersion() : overview.getDataVersion());
            if (job != null && window != null && hasText(job.getRequestJson()) && hasText(window.getDataVersion())) {
                DiagnosisFinalizeContext context = DiagnosisFinalizeContext.builder()
                    .jobId(job.getJobId())
                    .windowId(window.getWindowId())
                    .dataVersion(window.getDataVersion())
                    .requestJson(job.getRequestJson())
                    .periodStart(window.getPeriodStart())
                    .periodEnd(window.getPeriodEnd())
                    .periodDays(calcDays(window.getPeriodStart(), window.getPeriodEnd()))
                    .compareStart(window.getCompareStart())
                    .compareEnd(window.getCompareEnd())
                    .compareDays(calcDays(window.getCompareStart(), window.getCompareEnd()))
                    .param(finalizeSupport.buildParam(window.getPeriodStart(), window.getPeriodEnd(), job.getRequestJson()))
                    .compareParam(window.getCompareStart() == null || window.getCompareEnd() == null
                        ? null
                        : finalizeSupport.buildParam(window.getCompareStart(), window.getCompareEnd(), job.getRequestJson()))
                    .hashRequest(finalizeSupport.buildHashRequest(
                        window.getPeriodStart(), window.getPeriodEnd(), window.getCompareStart(), window.getCompareEnd(), job.getRequestJson()))
                    .queryHash(session.getQueryHash())
                    .build();
                diagnosisPriceBandFinalizeService.finalizePriceBand(context);
                status = "SUCCESS";
            }
        }

        PriceBandConfigUpdateResponse response = new PriceBandConfigUpdateResponse();
        response.setSessionId(request.getSessionId());
        response.setQueryHash(session.getQueryHash());
        response.setBoundJobId(session.getJobId());
        response.setStatus(status);
        return response;
    }

    private DiagnosisSessionCacheModel getSession(String sessionId) {
        DiagnosisSessionCacheModel model = sessionCacheStore.get(sessionId);
        if (model == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "session not found or expired");
        }
        if (model.getQueryHash() == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "session payload corrupted");
        }
        return model;
    }

    private DiagnosisOverviewSnapshotRow resolveOverviewSnapshot(String sessionId, DiagnosisSessionCacheModel session) {
        if (hasText(session.getDataVersion())) {
            DiagnosisOverviewSnapshotRow overview = snapshotMapper.selectOverviewByQueryAndVersion(
                TENANT_ID, session.getQueryHash(), session.getDataVersion());
            if (overview != null) {
                return overview;
            }
        }
        DiagnosisOverviewSnapshotRow latest = snapshotMapper.selectLatestOverviewByQuery(TENANT_ID, session.getQueryHash());
        if (latest != null && hasText(latest.getDataVersion())) {
            session.setDataVersion(latest.getDataVersion());
            sessionCacheStore.save(sessionId, session, Duration.ofMinutes(Math.max(1, cacheProperties.getResultTtlMinutes())));
        }
        return latest;
    }

    private List<DiagnosisPriceBandConfigRow> buildConfigRows(String queryHash, PriceBandConfigUpdateRequest request) {
        if (request.getPriceRange() == null || request.getPriceRange().isEmpty()) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "priceRange is required");
        }
        List<PriceBandConfigUpdateRequest.PriceRangeItem> items = new ArrayList<>(request.getPriceRange());
        items.sort(Comparator.comparing(PriceBandConfigUpdateRequest.PriceRangeItem::getPriceBandMin,
            Comparator.nullsFirst(BigDecimal::compareTo)));

        LocalDateTime now = LocalDateTime.now();
        List<DiagnosisPriceBandConfigRow> rows = new ArrayList<>();
        BigDecimal previousMax = null;
        for (int i = 0; i < items.size(); i++) {
            PriceBandConfigUpdateRequest.PriceRangeItem item = items.get(i);
            BigDecimal min = scale2(item.getPriceBandMin());
            BigDecimal max = scale2(item.getPriceBandMax());
            boolean isLast = i == items.size() - 1;
            if (min == null) {
                throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "priceBandMin is required");
            }
            if (!isLast && max == null) {
                throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "only the last price range can be open ended");
            }
            if (max != null && max.compareTo(min) <= 0) {
                throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "priceBandMax must be greater than priceBandMin");
            }
            if (previousMax != null && min.compareTo(previousMax) < 0) {
                throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "price ranges cannot overlap");
            }

            DiagnosisPriceBandConfigRow row = new DiagnosisPriceBandConfigRow();
            row.setTenantId(TENANT_ID);
            row.setQueryHash(queryHash);
            row.setSortNo(i + 1);
            row.setPriceBandMin(min);
            row.setPriceBandMax(max);
            row.setIsOpenEnded(isLast && max == null ? "1" : "0");
            row.setIsActive("1");
            row.setCreatedBy(SYSTEM_USER);
            row.setCreatedTime(now);
            row.setUpdatedBy(SYSTEM_USER);
            row.setUpdatedTime(now);
            rows.add(row);
            previousMax = max;
        }
        return rows;
    }

    private DiagnosisPrecomputeWindowRow pickWindowByVersion(List<DiagnosisPrecomputeWindowRow> windows, String dataVersion) {
        if (windows == null || windows.isEmpty()) {
            return null;
        }
        if (hasText(dataVersion)) {
            for (DiagnosisPrecomputeWindowRow window : windows) {
                if (window != null && dataVersion.equals(window.getDataVersion())) {
                    return window;
                }
            }
        }
        return windows.get(windows.size() - 1);
    }

    private long calcDays(java.time.LocalDate start, java.time.LocalDate end) {
        if (start == null || end == null) {
            return 0L;
        }
        return ChronoUnit.DAYS.between(start, end) + 1;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private BigDecimal scale2(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }
}

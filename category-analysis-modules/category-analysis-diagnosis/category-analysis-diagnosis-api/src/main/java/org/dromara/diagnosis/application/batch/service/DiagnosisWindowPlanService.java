package org.dromara.diagnosis.application.batch.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.application.batch.model.DiagnosisWindowType;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeWindowRow;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 预计算窗口计划服务.
 */
@Service
@RequiredArgsConstructor
public class DiagnosisWindowPlanService {

    private static final String DEFAULT_TENANT_ID = "000000";

    private final DiagnosisPrecomputeMapper precomputeMapper;

    public List<DiagnosisPrecomputeWindowRow> createWindows(DiagnosisPrecomputeJobRow jobRow,
                                                            LocalDate compareStartInput,
                                                            LocalDate compareEndInput) {
        LocalDate start = jobRow.getReadStart();
        LocalDate end = jobRow.getReadEnd();
        if (start == null || end == null) {
            LocalDate now = LocalDate.now();
            start = now.withDayOfMonth(1);
            end = now;
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("readEnd 不能早于 readStart");
        }

        LocalDate compareStart = compareStartInput;
        LocalDate compareEnd = compareEndInput;
        if ((compareStart == null) ^ (compareEnd == null)) {
            throw new IllegalArgumentException("compareStart 与 compareEnd 需要同时传入");
        }

        Set<DiagnosisWindowType> types = parseTypes(jobRow.getWindowTypes());
        List<DiagnosisPrecomputeWindowRow> windows = new ArrayList<>();
        for (DiagnosisWindowType type : types) {
            switch (type) {
                case YOY -> {
                    LocalDate yoyStart = compareStart == null ? start.minusYears(1) : compareStart;
                    LocalDate yoyEnd = compareEnd == null ? end.minusYears(1) : compareEnd;
                    if (yoyEnd.isBefore(yoyStart)) {
                        throw new IllegalArgumentException("compareEnd 不能早于 compareStart");
                    }
                    windows.add(insertWindow(jobRow.getJobId(), type.name(), start, end, yoyStart, yoyEnd));
                }
                case MONTH -> windows.add(insertWindow(jobRow.getJobId(), type.name(), start, end, null, null));
                default -> windows.add(insertWindow(jobRow.getJobId(), DiagnosisWindowType.MONTH.name(), start, end, null, null));
            }
        }
        return windows;
    }

    private DiagnosisPrecomputeWindowRow insertWindow(Long jobId,
                                                      String type,
                                                      LocalDate periodStart,
                                                      LocalDate periodEnd,
                                                      LocalDate compareStart,
                                                      LocalDate compareEnd) {
        DiagnosisPrecomputeWindowRow window = new DiagnosisPrecomputeWindowRow();
        window.setTenantId(DEFAULT_TENANT_ID);
        window.setJobId(jobId);
        window.setWindowType(type);
        window.setWindowKey(type + ":" + periodStart + "_" + periodEnd
            + ":" + (compareStart == null ? "-" : compareStart)
            + "_" + (compareEnd == null ? "-" : compareEnd));
        window.setPeriodStart(periodStart);
        window.setPeriodEnd(periodEnd);
        window.setCompareStart(compareStart);
        window.setCompareEnd(compareEnd);
        window.setStatusCode("PENDING");
        window.setProgressPercent(BigDecimal.ZERO);
        window.setCurrentStage("INIT");
        window.setRetryCount(0);
        window.setRowsRead(0L);
        window.setRowsWritten(0L);
        precomputeMapper.insertWindow(window);
        return window;
    }

    private Set<DiagnosisWindowType> parseTypes(String windowTypes) {
        LinkedHashSet<DiagnosisWindowType> result = new LinkedHashSet<>();
        if (!StringUtils.hasText(windowTypes)) {
            result.add(DiagnosisWindowType.MONTH);
            return result;
        }
        String[] values = windowTypes.split(",");
        for (String value : values) {
            result.add(DiagnosisWindowType.parse(value));
        }
        if (result.isEmpty()) {
            result.add(DiagnosisWindowType.MONTH);
        }
        return result;
    }
}

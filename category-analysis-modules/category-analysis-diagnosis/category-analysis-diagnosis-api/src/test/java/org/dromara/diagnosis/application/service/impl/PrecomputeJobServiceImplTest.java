package org.dromara.diagnosis.application.service.impl;

import org.dromara.diagnosis.api.request.PrecomputeJobCreateRequest;
import org.dromara.diagnosis.api.response.PrecomputeJobResponse;
import org.dromara.diagnosis.application.batch.service.DiagnosisPrecomputeBatchRunner;
import org.dromara.diagnosis.application.batch.service.DiagnosisWindowPlanService;
import org.dromara.diagnosis.application.service.DiagnosisProgressCacheService;
import org.dromara.diagnosis.application.service.DiagnosisRequestHashService;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeWindowRow;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
@ExtendWith(MockitoExtension.class)
class PrecomputeJobServiceImplTest {

    @Mock
    private DiagnosisPrecomputeMapper precomputeMapper;
    @Mock
    private DiagnosisWindowPlanService windowPlanService;
    @Mock
    private DiagnosisPrecomputeBatchRunner batchRunner;
    @Mock
    private DiagnosisProgressCacheService progressCacheService;
    @Mock
    private DiagnosisRequestHashService requestHashService;

    @InjectMocks
    private PrecomputeJobServiceImpl service;

    @Test
    void createJob_shouldCreateAndLaunch_whenNoActiveJob() {
        PrecomputeJobCreateRequest request = buildRequest();
        when(requestHashService.buildHash(request)).thenReturn("hash-1");
        when(precomputeMapper.selectLatestActiveJobByRequestHash("000000", "hash-1")).thenReturn(null);

        doAnswer(invocation -> {
            DiagnosisPrecomputeJobRow row = invocation.getArgument(0);
            row.setJobId(100L);
            return null;
        }).when(precomputeMapper).insertJob(any(DiagnosisPrecomputeJobRow.class));

        DiagnosisPrecomputeWindowRow window = new DiagnosisPrecomputeWindowRow();
        window.setWindowId(200L);
        window.setPeriodStart(LocalDate.of(2025, 4, 1));
        window.setPeriodEnd(LocalDate.of(2025, 4, 30));
        when(windowPlanService.createWindows(any(DiagnosisPrecomputeJobRow.class), eq(request.getCompareStart()), eq(request.getCompareEnd())))
            .thenReturn(List.of(window));

        DiagnosisPrecomputeJobRow saved = new DiagnosisPrecomputeJobRow();
        saved.setJobId(100L);
        saved.setJobCode("JOB-100");
        saved.setStatusCode("RUNNING");
        saved.setPriority(5);
        saved.setModuleCode("DIAGNOSIS");
        saved.setReadRangeType("CUSTOM");
        saved.setReadStart(request.getReadStart());
        saved.setReadEnd(request.getReadEnd());
        saved.setWindowTypes(request.getWindowTypes());
        saved.setMaxRetry(1);
        saved.setRetryCount(0);
        saved.setProgressPercent(BigDecimal.ZERO);
        saved.setCurrentStage("INIT");
        saved.setTotalWindows(1);
        saved.setDoneWindows(0);
        saved.setRowsRead(0L);
        saved.setRowsWritten(0L);
        saved.setSubmittedTime(LocalDateTime.now());
        when(precomputeMapper.selectJobById("000000", 100L)).thenReturn(saved);
        when(precomputeMapper.selectWindowsByJobId("000000", 100L)).thenReturn(List.of(window));

        PrecomputeJobResponse response = service.createJob(request);

        assertThat(response.getJobId()).isEqualTo(100L);
        assertThat(response.getStatus()).isEqualTo("RUNNING");
        assertThat(response.getWindows()).hasSize(1);
        verify(batchRunner).launch(eq(100L), eq(200L), eq(window.getPeriodStart()), eq(window.getPeriodEnd()), anyString(), eq(request.getRequestJson()));
    }

    @Test
    void createJob_shouldReuseActiveJob_whenActiveJobExists() {
        PrecomputeJobCreateRequest request = buildRequest();
        request.setForceRebuild(Boolean.FALSE);
        when(requestHashService.buildHash(request)).thenReturn("hash-2");

        DiagnosisPrecomputeJobRow active = new DiagnosisPrecomputeJobRow();
        active.setJobId(88L);
        when(precomputeMapper.selectLatestActiveJobByRequestHash("000000", "hash-2")).thenReturn(active);

        DiagnosisPrecomputeJobRow saved = new DiagnosisPrecomputeJobRow();
        saved.setJobId(88L);
        saved.setStatusCode("RUNNING");
        when(precomputeMapper.selectJobById("000000", 88L)).thenReturn(saved);
        when(precomputeMapper.selectWindowsByJobId("000000", 88L)).thenReturn(List.of());

        PrecomputeJobResponse response = service.createJob(request);

        assertThat(response.getJobId()).isEqualTo(88L);
        verify(precomputeMapper, never()).insertJob(any(DiagnosisPrecomputeJobRow.class));
        verify(batchRunner, never()).launch(any(), any(), any(), any(), anyString(), anyString());
    }

    @Test
    void retryJob_shouldThrow_whenRetryCountReachedMax() {
        DiagnosisPrecomputeJobRow row = new DiagnosisPrecomputeJobRow();
        row.setJobId(66L);
        row.setRetryCount(1);
        row.setMaxRetry(1);
        when(precomputeMapper.selectJobById("000000", 66L)).thenReturn(row);

        assertThatThrownBy(() -> service.retryJob(66L))
            .isInstanceOf(DiagnosisBizException.class);
    }

    private PrecomputeJobCreateRequest buildRequest() {
        PrecomputeJobCreateRequest request = new PrecomputeJobCreateRequest();
        request.setModule("DIAGNOSIS");
        request.setReadRangeType("CUSTOM");
        request.setReadStart(LocalDate.of(2025, 4, 1));
        request.setReadEnd(LocalDate.of(2025, 4, 30));
        request.setCompareStart(LocalDate.of(2024, 4, 1));
        request.setCompareEnd(LocalDate.of(2024, 4, 30));
        request.setWindowTypes("YOY");
        request.setForceRebuild(Boolean.FALSE);
        request.setPriority(5);
        request.setRequestJson("{\"storeNo\":\"0000\",\"classLevel\":1,\"classNo\":\"001\"}");
        return request;
    }
}


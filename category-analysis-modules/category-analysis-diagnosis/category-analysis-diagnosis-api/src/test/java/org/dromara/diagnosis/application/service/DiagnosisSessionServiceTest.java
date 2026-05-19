package org.dromara.diagnosis.application.service;

import org.dromara.diagnosis.api.request.DiagnosisSessionCreateRequest;
import org.dromara.diagnosis.api.response.DiagnosisCategoryPerformanceTrendResponse;
import org.dromara.diagnosis.api.response.DiagnosisOverviewResponse;
import org.dromara.diagnosis.api.response.DiagnosisSessionCreateResponse;
import org.dromara.diagnosis.api.response.PrecomputeJobResponse;
import org.dromara.diagnosis.application.config.DiagnosisCacheProperties;
import org.dromara.diagnosis.application.model.DiagnosisSessionCacheModel;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisCategoryPerformanceTrendMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisCategoryPerformanceTrendRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisOverviewSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
@ExtendWith(MockitoExtension.class)
class DiagnosisSessionServiceTest {

    @Mock
    private DiagnosisSnapshotMapper snapshotMapper;
    @Mock
    private DiagnosisCategoryPerformanceTrendMapper categoryPerformanceTrendMapper;
    @Mock
    private DiagnosisQueryHashService queryHashService;
    @Mock
    private PrecomputeJobService precomputeJobService;
    @Mock
    private DiagnosisPrecomputeMapper precomputeMapper;
    @Mock
    private DiagnosisCacheProperties cacheProperties;
    @Mock
    private DiagnosisSessionCacheStore sessionCacheStore;

    @InjectMocks
    private DiagnosisSessionService service;

    @Test
    void createSession_shouldTriggerPrecompute_whenSnapshotMissing() {
        DiagnosisSessionCreateRequest req = new DiagnosisSessionCreateRequest();
        req.setStoreNo("0000");
        req.setClassLevel(1);
        req.setClassNo("001");
        req.setPeriodStart(LocalDate.of(2025, 4, 1));
        req.setPeriodEnd(LocalDate.of(2025, 4, 30));
        req.setCompareStart(LocalDate.of(2024, 4, 1));
        req.setCompareEnd(LocalDate.of(2024, 4, 30));
        req.setTriggerIfMissing(Boolean.TRUE);
        req.setWaitSeconds(0);

        when(queryHashService.buildQueryHash(req)).thenReturn("qh-1");
        when(snapshotMapper.selectLatestOverviewByQuery("000000", "qh-1")).thenReturn(null);
        when(cacheProperties.getSessionTtlMinutes()).thenReturn(120);
        PrecomputeJobResponse job = new PrecomputeJobResponse();
        job.setJobId(300L);
        when(precomputeJobService.createJob(any())).thenReturn(job);

        DiagnosisSessionCreateResponse response = service.createSession(req);

        assertThat(response.getQueryHash()).isEqualTo("qh-1");
        assertThat(response.getReady()).isFalse();
        assertThat(response.getTriggeredJobId()).isEqualTo(300L);
        assertThat(response.getSource()).isEqualTo("TRIGGERED");
        verify(sessionCacheStore).save(eq(response.getSessionId()), any(DiagnosisSessionCacheModel.class), eq(Duration.ofMinutes(120)));
    }

    @Test
    void createSession_shouldTriggerNewPrecompute_whenCachedJobIsZombieActive() {
        DiagnosisSessionCreateRequest req = new DiagnosisSessionCreateRequest();
        req.setStoreNo("");
        req.setClassLevel(1);
        req.setClassNo("003");
        req.setClassName("食品杂货");
        req.setPeriodStart(LocalDate.of(2025, 4, 1));
        req.setPeriodEnd(LocalDate.of(2025, 4, 30));
        req.setCompareStart(LocalDate.of(2024, 4, 1));
        req.setCompareEnd(LocalDate.of(2024, 4, 30));
        req.setTriggerIfMissing(Boolean.TRUE);
        req.setWaitSeconds(0);

        DiagnosisSessionCacheModel cached = new DiagnosisSessionCacheModel();
        cached.setSessionId("S-old");
        cached.setQueryHash("qh-zombie");
        cached.setJobId(26L);

        DiagnosisPrecomputeJobRow zombieJob = new DiagnosisPrecomputeJobRow();
        zombieJob.setJobId(26L);
        zombieJob.setStatusCode("RUNNING");
        zombieJob.setStartedTime(LocalDateTime.now().minusMinutes(5));

        PrecomputeJobResponse newJob = new PrecomputeJobResponse();
        newJob.setJobId(301L);
        newJob.setStatus("RUNNING");
        newJob.setOrchestratorStatus("RUNNING");

        when(queryHashService.buildQueryHash(req)).thenReturn("qh-zombie");
        when(snapshotMapper.selectLatestOverviewByQuery("000000", "qh-zombie")).thenReturn(null);
        when(sessionCacheStore.getByQueryHash("qh-zombie")).thenReturn(cached);
        when(precomputeMapper.selectJobById("000000", 26L)).thenReturn(zombieJob);
        when(precomputeMapper.countBatchExecutionsByJobId(26L)).thenReturn(0L);
        when(precomputeJobService.createJob(any())).thenReturn(newJob);
        when(cacheProperties.getSessionTtlMinutes()).thenReturn(120);

        DiagnosisSessionCreateResponse response = service.createSession(req);

        assertThat(response.getSource()).isEqualTo("TRIGGERED");
        assertThat(response.getTriggeredJobId()).isEqualTo(301L);
    }

    @Test
    void getOverview_shouldCalculateDerivedMetrics() {
        DiagnosisSessionCacheModel session = new DiagnosisSessionCacheModel();
        session.setSessionId("S-1");
        session.setQueryHash("qh-2");
        session.setDataVersion("v1");

        DiagnosisOverviewSnapshotRow overview = new DiagnosisOverviewSnapshotRow();
        overview.setClassNo("001");
        overview.setClassName("cls");
        overview.setMetricTotalSku(10);
        overview.setMetricCompareTotalSku(5);
        overview.setMetricTotalSales(new BigDecimal("120"));
        overview.setMetricCompareSales(new BigDecimal("100"));
        overview.setMetricTotalProfit(new BigDecimal("30"));
        overview.setMetricCompareGross(new BigDecimal("20"));
        overview.setMetricProfitMargin(new BigDecimal("25"));
        overview.setMetricSaleQuantity(new BigDecimal("60"));
        overview.setMetricCompareSaleQuantity(new BigDecimal("50"));
        overview.setMetricSalesCost(new BigDecimal("90"));
        overview.setMetricCompareSalesCost(new BigDecimal("80"));
        overview.setMetricCustomerCount(new BigDecimal("30"));
        overview.setMetricCompareCustomerCount(new BigDecimal("20"));
        overview.setMetricCustomerCountTotal(new BigDecimal("40"));
        overview.setMetricCompareCustomerCountTotal(new BigDecimal("35"));
        overview.setMetricCustomerPrice(new BigDecimal("4"));
        overview.setMetricCustomerAvgQuantity(new BigDecimal("2"));
        overview.setMetricPieceAvgPrice(new BigDecimal("2"));
        overview.setMetricAvgInventory(new BigDecimal("50"));
        overview.setMetricCompareAvgInventory(new BigDecimal("40"));
        overview.setMetricInventorySalesRatio(new BigDecimal("2.4"));
        overview.setMetricCompareInventorySalesRatio(new BigDecimal("2.0"));
        overview.setMetricInventoryTurnoverDays(new BigDecimal("15"));
        overview.setMetricCompareInventoryTurnoverDays(new BigDecimal("12"));
        overview.setMetricPenetrateRate(new BigDecimal("0.50"));
        overview.setMetricComparePenetrateRate(new BigDecimal("0.40"));
        overview.setMetricSalesRate(new BigDecimal("0.80"));
        overview.setMetricCompareSalesRate(new BigDecimal("0.70"));

        when(sessionCacheStore.get("S-1")).thenReturn(session);
        when(snapshotMapper.selectOverviewByQueryAndVersion("000000", "qh-2", "v1")).thenReturn(overview);

        DiagnosisOverviewResponse response = service.getOverview("S-1");

        assertThat(response.getCurrentSales()).isEqualByComparingTo("120");
        assertThat(response.getCompareSales()).isEqualByComparingTo("100");
        assertThat(response.getComparativeSales()).isEqualByComparingTo("20.0000");
        assertThat(response.getCompareCustomerPrice()).isEqualByComparingTo("5.000000");
        assertThat(response.getCompareGrossRate()).isEqualByComparingTo("20.0000");
    }

    @Test
    void getTrendChanges_shouldSplitCurrentAndCompareSeries() {
        DiagnosisSessionCacheModel session = new DiagnosisSessionCacheModel();
        session.setSessionId("S-2");
        session.setQueryHash("qh-3");
        session.setDataVersion("v2");

        DiagnosisOverviewSnapshotRow overview = new DiagnosisOverviewSnapshotRow();
        overview.setDataVersion("v2");

        DiagnosisCategoryPerformanceTrendRow r1 = new DiagnosisCategoryPerformanceTrendRow();
        r1.setPeriodFlag("1");
        r1.setPointIndex(1);
        r1.setPointDate(LocalDate.of(2025, 4, 1));
        r1.setSales(new BigDecimal("100"));
        DiagnosisCategoryPerformanceTrendRow r2 = new DiagnosisCategoryPerformanceTrendRow();
        r2.setPeriodFlag("2");
        r2.setPointIndex(1);
        r2.setPointDate(LocalDate.of(2024, 4, 1));
        r2.setSales(new BigDecimal("80"));

        when(sessionCacheStore.get("S-2")).thenReturn(session);
        when(snapshotMapper.selectOverviewByQueryAndVersion("000000", "qh-3", "v2")).thenReturn(overview);
        when(categoryPerformanceTrendMapper.selectByVersion("000000", "qh-3", "v2")).thenReturn(List.of(r1, r2));

        DiagnosisCategoryPerformanceTrendResponse response = service.getTrendChanges("S-2");

        assertThat(response.getXdata()).containsExactly("2025-04-01");
        assertThat(response.getXdataDB()).containsExactly("2024-04-01");
        assertThat(response.getLineDate()).hasSize(2);
    }

    @Test
    void getOverview_shouldThrow_whenSessionNotFound() {
        when(sessionCacheStore.get("not-exist")).thenReturn(null);

        assertThatThrownBy(() -> service.getOverview("not-exist"))
            .isInstanceOf(DiagnosisBizException.class);
    }
}

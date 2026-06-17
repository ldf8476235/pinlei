package org.dromara.diagnosis.application.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.dromara.diagnosis.application.model.DiagnosisAsyncOrchestratorRequest;
import org.dromara.diagnosis.application.model.OrchestratorResult;
import org.dromara.diagnosis.application.service.DiagnosisAsyncOrchestratorService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
@Slf4j
public class DiagnosisAsyncOrchestratorServiceImpl implements DiagnosisAsyncOrchestratorService {

    private static final int MODULE_RETRY_TIMES = 1;

    private final Executor orchestratorExecutor;

    private final Executor computeExecutor;

    private final long moduleTimeoutSeconds;

    public DiagnosisAsyncOrchestratorServiceImpl(
        @Qualifier("diagnosisOrchestratorExecutor") Executor orchestratorExecutor,
        @Qualifier("diagnosisComputeExecutor") Executor computeExecutor,
        @Value("${diagnosis.orchestrator.module-timeout-seconds:1800}") long moduleTimeoutSeconds
    ) {
        this.orchestratorExecutor = orchestratorExecutor;
        this.computeExecutor = computeExecutor;
        this.moduleTimeoutSeconds = moduleTimeoutSeconds;
    }

    @Override
    public CompletableFuture<OrchestratorResult> orchestrate(DiagnosisAsyncOrchestratorRequest request,
                                                             Map<String, Supplier<Long>> moduleSuppliers,
                                                             Consumer<Map<String, Object>> moduleProgressConsumer,
                                                             Supplier<Boolean> stopSignal) {
        return CompletableFuture.supplyAsync(() -> run(request, moduleSuppliers, moduleProgressConsumer, stopSignal), orchestratorExecutor);
    }

    private OrchestratorResult run(DiagnosisAsyncOrchestratorRequest request,
                                   Map<String, Supplier<Long>> moduleSuppliers,
                                   Consumer<Map<String, Object>> moduleProgressConsumer,
                                   Supplier<Boolean> stopSignal) {
        Map<String, Long> moduleRows = new ConcurrentHashMap<>();
        Map<String, String> moduleStatus = new ConcurrentHashMap<>();
        Map<String, String> moduleErrors = new ConcurrentHashMap<>();
        Map<String, CompletableFuture<Void>> futures = new LinkedHashMap<>();
        long orchestratorStart = System.currentTimeMillis();
        log.info(
            "diagnosis orchestrator started, jobId={}, dataVersion={}, period={}~{}, compare={}~{}, modules={}",
            request == null ? null : request.getJobId(),
            request == null ? null : request.getDataVersion(),
            request == null ? null : request.getPeriodStart(),
            request == null ? null : request.getPeriodEnd(),
            request == null ? null : request.getCompareStart(),
            request == null ? null : request.getCompareEnd(),
            moduleSuppliers.keySet()
        );

        for (Map.Entry<String, Supplier<Long>> entry : moduleSuppliers.entrySet()) {
            String module = entry.getKey();
            Supplier<Long> supplier = entry.getValue();
            if (stopSignal != null && Boolean.TRUE.equals(stopSignal.get())) {
                moduleStatus.put(module, "STOPPED");
                moduleErrors.put(module, "job stopped");
                log.warn("diagnosis module skipped because job stopped, jobId={}, module={}",
                    request == null ? null : request.getJobId(), module);
                notifyProgress(moduleProgressConsumer, moduleStatus, moduleErrors);
                continue;
            }
            moduleStatus.put(module, "RUNNING");
            notifyProgress(moduleProgressConsumer, moduleStatus, moduleErrors);
            log.info("diagnosis module started, jobId={}, module={}",
                request == null ? null : request.getJobId(), module);
            long moduleStart = System.currentTimeMillis();
            CompletableFuture<Void> future = CompletableFuture.supplyAsync(
                    () -> executeWithRetry(request, module, supplier, stopSignal),
                    computeExecutor)
                .orTimeout(moduleTimeoutSeconds, TimeUnit.SECONDS)
                .handle((rows, ex) -> {
                    long elapsedMs = System.currentTimeMillis() - moduleStart;
                    if (ex != null) {
                        Throwable cause = ex instanceof CompletionException && ex.getCause() != null ? ex.getCause() : ex;
                        String errorMessage = resolveErrorMessage(module, cause);
                        if (cause instanceof CancellationException) {
                            moduleStatus.put(module, "STOPPED");
                        } else if (cause instanceof TimeoutException) {
                            moduleStatus.put(module, "TIMEOUT");
                        } else {
                            moduleStatus.put(module, "FAILED");
                        }
                        moduleErrors.put(module, errorMessage);
                        log.error(
                            "diagnosis module failed, jobId={}, module={}, status={}, elapsedMs={}, error={}",
                            request == null ? null : request.getJobId(),
                            module,
                            moduleStatus.get(module),
                            elapsedMs,
                            errorMessage,
                            cause
                        );
                        notifyProgress(moduleProgressConsumer, moduleStatus, moduleErrors);
                        throw new CompletionException(cause);
                    }
                    moduleRows.put(module, rows == null ? 0L : rows);
                    moduleStatus.put(module, "SUCCESS");
                    log.info("diagnosis module completed, jobId={}, module={}, rows={}, elapsedMs={}",
                        request == null ? null : request.getJobId(), module, rows == null ? 0L : rows, elapsedMs);
                    notifyProgress(moduleProgressConsumer, moduleStatus, moduleErrors);
                    return null;
                });
            futures.put(module, future);
        }

        CompletableFuture<?>[] all = futures.values().toArray(new CompletableFuture[0]);
        boolean success;
        try {
            CompletableFuture.allOf(all).join();
            success = true;
        } catch (CompletionException ex) {
            success = false;
        }

        log.info(
            "diagnosis orchestrator finished, jobId={}, success={}, elapsedMs={}, moduleStatus={}, moduleErrors={}, moduleRows={}",
            request == null ? null : request.getJobId(),
            success,
            System.currentTimeMillis() - orchestratorStart,
            new LinkedHashMap<>(moduleStatus),
            new LinkedHashMap<>(moduleErrors),
            new LinkedHashMap<>(moduleRows)
        );

        return OrchestratorResult.builder()
            .success(success)
            .moduleRows(moduleRows)
            .moduleStatus(new LinkedHashMap<>(moduleStatus))
            .moduleErrors(new LinkedHashMap<>(moduleErrors))
            .build();
    }

    private Long executeWithRetry(DiagnosisAsyncOrchestratorRequest request,
                                  String module,
                                  Supplier<Long> supplier,
                                  Supplier<Boolean> stopSignal) {
        int attempts = 0;
        RuntimeException lastEx = null;
        while (attempts <= MODULE_RETRY_TIMES) {
            if (stopSignal != null && Boolean.TRUE.equals(stopSignal.get())) {
                throw new CancellationException(module + " cancelled because job stopped");
            }
            int attemptNo = attempts + 1;
            try {
                log.info("diagnosis module attempt started, jobId={}, module={}, attempt={}/{}",
                    request == null ? null : request.getJobId(), module, attemptNo, MODULE_RETRY_TIMES + 1);
                return supplier.get();
            } catch (CancellationException e) {
                throw e;
            } catch (RuntimeException e) {
                lastEx = e;
                attempts++;
                log.warn(
                    "diagnosis module attempt failed, jobId={}, module={}, attempt={}/{}, willRetry={}, error={}",
                    request == null ? null : request.getJobId(),
                    module,
                    attemptNo,
                    MODULE_RETRY_TIMES + 1,
                    attempts <= MODULE_RETRY_TIMES,
                    e.getMessage(),
                    e
                );
                if (attempts > MODULE_RETRY_TIMES) {
                    break;
                }
            }
        }
        throw lastEx == null ? new IllegalStateException(module + " failed with unknown error") : lastEx;
    }

    private String resolveErrorMessage(String module, Throwable cause) {
        if (cause instanceof TimeoutException) {
            return module + " timed out after " + moduleTimeoutSeconds + " seconds";
        }
        String message = cause == null ? null : cause.getMessage();
        if (message == null || message.isBlank()) {
            return module + " failed with " + (cause == null ? "unknown error" : cause.getClass().getSimpleName());
        }
        return message;
    }

    private void notifyProgress(Consumer<Map<String, Object>> moduleProgressConsumer,
                                Map<String, String> moduleStatus,
                                Map<String, String> moduleErrors) {
        if (moduleProgressConsumer == null) {
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", new LinkedHashMap<>(moduleStatus));
        payload.put("errors", new LinkedHashMap<>(moduleErrors));
        moduleProgressConsumer.accept(payload);
    }
}

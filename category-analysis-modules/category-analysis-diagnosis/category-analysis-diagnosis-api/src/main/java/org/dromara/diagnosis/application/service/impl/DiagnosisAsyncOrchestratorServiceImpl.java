package org.dromara.diagnosis.application.service.impl;

import org.dromara.diagnosis.application.model.DiagnosisAsyncOrchestratorRequest;
import org.dromara.diagnosis.application.model.OrchestratorResult;
import org.dromara.diagnosis.application.service.DiagnosisAsyncOrchestratorService;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class DiagnosisAsyncOrchestratorServiceImpl implements DiagnosisAsyncOrchestratorService {

    private static final long MODULE_TIMEOUT_SECONDS = 300L;

    private static final int MODULE_RETRY_TIMES = 1;

    private final Executor orchestratorExecutor;

    private final Executor computeExecutor;

    public DiagnosisAsyncOrchestratorServiceImpl(
        @Qualifier("diagnosisOrchestratorExecutor") Executor orchestratorExecutor,
        @Qualifier("diagnosisComputeExecutor") Executor computeExecutor
    ) {
        this.orchestratorExecutor = orchestratorExecutor;
        this.computeExecutor = computeExecutor;
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

        for (Map.Entry<String, Supplier<Long>> entry : moduleSuppliers.entrySet()) {
            String module = entry.getKey();
            Supplier<Long> supplier = entry.getValue();
            if (stopSignal != null && Boolean.TRUE.equals(stopSignal.get())) {
                moduleStatus.put(module, "STOPPED");
                moduleErrors.put(module, "job stopped");
                notifyProgress(moduleProgressConsumer, moduleStatus, moduleErrors);
                continue;
            }
            moduleStatus.put(module, "RUNNING");
            notifyProgress(moduleProgressConsumer, moduleStatus, moduleErrors);
            CompletableFuture<Void> future = CompletableFuture.supplyAsync(
                    () -> executeWithRetry(module, supplier, stopSignal),
                    computeExecutor)
                .orTimeout(MODULE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .handle((rows, ex) -> {
                    if (ex != null) {
                        Throwable cause = ex instanceof CompletionException && ex.getCause() != null ? ex.getCause() : ex;
                        if (cause instanceof CancellationException) {
                            moduleStatus.put(module, "STOPPED");
                        } else if (cause instanceof TimeoutException) {
                            moduleStatus.put(module, "TIMEOUT");
                        } else {
                            moduleStatus.put(module, "FAILED");
                        }
                        moduleErrors.put(module, cause.getMessage());
                        notifyProgress(moduleProgressConsumer, moduleStatus, moduleErrors);
                        throw new CompletionException(cause);
                    }
                    moduleRows.put(module, rows == null ? 0L : rows);
                    moduleStatus.put(module, "SUCCESS");
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

        return OrchestratorResult.builder()
            .success(success)
            .moduleRows(moduleRows)
            .moduleStatus(new LinkedHashMap<>(moduleStatus))
            .moduleErrors(new LinkedHashMap<>(moduleErrors))
            .build();
    }

    private Long executeWithRetry(String module, Supplier<Long> supplier, Supplier<Boolean> stopSignal) {
        int attempts = 0;
        RuntimeException lastEx = null;
        while (attempts <= MODULE_RETRY_TIMES) {
            if (stopSignal != null && Boolean.TRUE.equals(stopSignal.get())) {
                throw new CancellationException(module + " cancelled because job stopped");
            }
            try {
                return supplier.get();
            } catch (CancellationException e) {
                throw e;
            } catch (RuntimeException e) {
                lastEx = e;
                attempts++;
                if (attempts > MODULE_RETRY_TIMES) {
                    break;
                }
            }
        }
        throw lastEx == null ? new IllegalStateException(module + " failed with unknown error") : lastEx;
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

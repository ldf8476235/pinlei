package org.dromara.diagnosis.application.service;

import org.dromara.diagnosis.application.model.DiagnosisAsyncOrchestratorRequest;
import org.dromara.diagnosis.application.model.OrchestratorResult;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface DiagnosisAsyncOrchestratorService {

    CompletableFuture<OrchestratorResult> orchestrate(DiagnosisAsyncOrchestratorRequest request,
                                                       Map<String, Supplier<Long>> moduleSuppliers,
                                                       Consumer<Map<String, Object>> moduleProgressConsumer,
                                                       Supplier<Boolean> stopSignal);
}

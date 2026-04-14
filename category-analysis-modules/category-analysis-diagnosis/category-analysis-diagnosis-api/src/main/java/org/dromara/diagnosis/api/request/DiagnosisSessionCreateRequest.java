package org.dromara.diagnosis.api.request;

import lombok.Data;

import java.time.LocalDate;

/**
 * Request payload for creating a diagnosis session.
 */
@Data
public class DiagnosisSessionCreateRequest {

    private Integer classLevel;
    private String classNo;
    private String className;

    /**
     * Organization id, compatible with legacy field `deptId`.
     */
    private String deptId;

    /**
     * Retail type id, compatible with legacy field `retailTypeId`.
     */
    private String retailTypeId;

    /**
     * Business circle id, compatible with legacy field `businessCircleId`.
     */
    private String businessCircleId;

    /**
     * Department group id, compatible with legacy field `deptGroupId`.
     */
    private String deptGroupId;

    /**
     * Store number, compatible with legacy field `storeNo`. Empty means all stores.
     */
    private String storeNo;

    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate compareStart;
    private LocalDate compareEnd;
    private String extraFilterJson;

    /**
     * Whether to trigger precompute automatically when snapshot is missing.
     */
    private Boolean triggerIfMissing;

    /**
     * Wait seconds after triggering precompute. Zero means no waiting.
     */
    private Integer waitSeconds;
}

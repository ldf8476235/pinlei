package org.dromara.diagnosis.api.request;

import lombok.Data;

import java.time.LocalDate;

/**
 * 诊断会话创建请求.
 */
@Data
public class DiagnosisSessionCreateRequest {

    private Integer classLevel;

    private String classNo;

    private String className;

    /**
     * 组织ID（兼容旧接口 deptId）.
     */
    private String deptId;

    /**
     * 业态ID（兼容旧接口 retailTypeId）.
     */
    private String retailTypeId;

    /**
     * 商圈ID（兼容旧接口 businessCircleId）.
     */
    private String businessCircleId;

    /**
     * 店组ID（兼容旧接口 deptGroupId）.
     */
    private String deptGroupId;

    /**
     * 门店编码（兼容旧接口 storeNo；0 表示全部）.
     */
    private String storeNo;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private LocalDate compareStart;

    private LocalDate compareEnd;

    private String extraFilterJson;

    /**
     * 结果缺失时是否自动触发预计算，默认 true.
     */
    private Boolean triggerIfMissing;

    /**
     * 触发预计算后等待秒数，默认 0（不等待）.
     */
    private Integer waitSeconds;
}

package org.dromara.diagnosis.api.request;

import lombok.Data;

/**
 * 品类树(兼容旧接口结构)请求.
 */
@Data
public class CategoryClassTreeRequest {

    /**
     * 返回层级(1~5).
     */
    private Integer level;
}


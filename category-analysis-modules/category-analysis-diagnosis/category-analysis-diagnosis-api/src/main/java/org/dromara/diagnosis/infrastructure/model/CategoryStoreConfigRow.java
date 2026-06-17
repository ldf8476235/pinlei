package org.dromara.diagnosis.infrastructure.model;

import lombok.Data;

/**
 * 门店配置写入所需的客户源门店字段.
 */
@Data
public class CategoryStoreConfigRow {

    private String storeNo;

    private String storeName;

    private String storeOrgNo;

    private String storeOrgName;

    private String storeFormatNo;

    private String storeFormatName;

    private String businessCircleNo;

    private String businessCircleName;

    private String storeGroupNo;

    private String storeGroupName;
}

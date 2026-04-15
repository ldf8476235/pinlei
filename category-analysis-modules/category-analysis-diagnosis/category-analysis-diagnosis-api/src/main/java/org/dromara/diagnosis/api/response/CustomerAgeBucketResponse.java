package org.dromara.diagnosis.api.response;

import lombok.Data;

@Data
public class CustomerAgeBucketResponse {

    private String ageCode;
    private String ageName;
    private Integer ageStart;
    private Integer ageEnd;
    private Integer orderNumber;
}

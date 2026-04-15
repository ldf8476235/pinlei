package org.dromara.diagnosis.api.response;

import lombok.Data;

@Data
public class PriceBandConfigUpdateResponse {

    private String sessionId;
    private String queryHash;
    private Long boundJobId;
    private String status;
}

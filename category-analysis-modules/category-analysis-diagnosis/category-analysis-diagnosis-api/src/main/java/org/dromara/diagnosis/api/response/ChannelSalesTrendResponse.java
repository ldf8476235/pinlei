package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.List;

@Data
public class ChannelSalesTrendResponse {

    private List<ChannelSalesTrendPointResponse> lineDate;

    private List<String> xdata;
}

package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DiagnosisRecordPageResponse {

    private List<DiagnosisRecordItemResponse> rows = new ArrayList<>();

    private Long total;

    private Integer pageNum;

    private Integer pageSize;
}

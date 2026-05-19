package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DiagnosisIntroduceDirectionResponse {

    private String sessionId;
    private List<Column> columns;
    private List<Map<String, String>> rows;

    @Data
    public static class Column {
        private String label;
        private String value;

        public Column() {
        }

        public Column(String label, String value) {
            this.label = label;
            this.value = value;
        }
    }
}

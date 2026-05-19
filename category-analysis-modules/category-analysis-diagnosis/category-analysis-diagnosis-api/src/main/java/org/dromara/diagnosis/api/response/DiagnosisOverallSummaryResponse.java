package org.dromara.diagnosis.api.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 品类诊断整体文字总结.
 */
@Data
public class DiagnosisOverallSummaryResponse {

    private String sessionId;

    private String classNo;

    private String className;

    private List<SummarySection> sections = new ArrayList<>();

    @Data
    public static class SummarySection {

        /**
         * NORMAL / ABNORMAL / OTHER.
         */
        private String status;

        private String title;

        private String conclusion;

        private List<String> descriptions = new ArrayList<>();
    }
}

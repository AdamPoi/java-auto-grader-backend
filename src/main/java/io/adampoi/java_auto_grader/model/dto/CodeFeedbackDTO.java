package io.adampoi.java_auto_grader.model.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeFeedbackDTO {
    private List<Issue> issues;
    private String overallAssessment;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Issue {
        private String line;
        private String description;
        private String suggestion;

    }
}

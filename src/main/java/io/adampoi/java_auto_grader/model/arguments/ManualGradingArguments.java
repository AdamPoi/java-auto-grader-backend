package io.adampoi.java_auto_grader.model.arguments;

import io.adampoi.java_auto_grader.model.type.GradeArguments;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ManualGradingArguments extends GradeArguments {
    private List<GradingCriteria> criteria;
    private Boolean requireComments = false;
    private String submissionFormat = "zip"; // "zip", "single_file", "multiple_files"
    private List<String> additionalFiles;
    private String gradingRubricUrl;
    private String instructions;
    private Boolean allowResubmission = true;
    private Integer maxSubmissionAttempts = 3;

    @Override
    public String getType() {
        return "MANUAL_GRADING";
    }

    @Data
    public static class GradingCriteria {
        private String name;
        private Integer maxPoints;
        private String description;
        private List<String> checkpoints;
        private Boolean required = true;
    }
}
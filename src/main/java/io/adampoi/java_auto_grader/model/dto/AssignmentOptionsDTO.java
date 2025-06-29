package io.adampoi.java_auto_grader.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentOptionsDTO {
    private Boolean isTimed;            // Timed or untimed assessment
    private Integer timeLimit;          // Time limit in seconds
    private Integer maxAttempts;        // Max number of student submissions (0 = unlimited)
    private Boolean showTrySubmission;  // Show student their submissions/results (practice mode)
    private Boolean showFeedback;       // Show feedback after submit
    private Boolean showSolution;       // Show solution after submit (if available)
    private Boolean allowUpload;        // Allow student to upload code
}
package io.adampoi.java_auto_grader.domain;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class AssignmentOptions {
    private Boolean isPublished;
    private Boolean isTimed;
    private Integer timeLimit;         // in second
    private Integer maxAttempts;
    private Boolean showTrySubmission;
    private Boolean showFeedback;
    private Boolean showSolution;
    private Boolean allowUpload;

}
package io.adampoi.java_auto_grader.model.dto;


import com.fasterxml.jackson.annotation.JsonBackReference;
import io.adampoi.java_auto_grader.model.request.TestCodeRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TestExecutionDTO extends AuditableDTO {

    private UUID id;

    private String methodName;

    @Size(min = 1, max = 255)
    @Pattern(regexp = "PENDING|RUNNING|PASSED|FAILED|ERROR|TIMEOUT|SKIPPED",
            message = "Status must be one of: PENDING, RUNNING, PASSED, FAILED, ERROR, TIMEOUT, SKIPPED")
    private String status;

    private String output;

    private String error;

    private Long executionTime;

    private UUID rubricGradeId;

    @NotNull(groups = CreateGroup.class)
    private UUID submissionId;

    private UUID assignmentId;

    private TestCodeRequest testCodeRequest;

    @JsonBackReference("rubric-grade-test-executions")
    private RubricGradeDTO rubricGrade;

    @JsonBackReference("submission-test-executions")
    private SubmissionDTO submission;

    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }
}

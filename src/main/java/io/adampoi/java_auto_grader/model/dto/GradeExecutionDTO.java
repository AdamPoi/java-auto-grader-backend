package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GradeExecutionDTO extends AuditableDTO {

    private UUID id;

    private BigDecimal points;

    @Size(min = 1, max = 255)
    @Pattern(regexp = "PENDING|RUNNING|PASSED|FAILED|ERROR|TIMEOUT|SKIPPED",
            message = "Status must be one of: PENDING, RUNNING, PASSED, FAILED, ERROR, TIMEOUT, SKIPPED")
    private String status;

    private String actual;

    private String expected;

    private String error;

    private Long executionTime;

    @NotNull(groups = CreateGroup.class)
    private UUID rubricGradeId;

    @NotNull(groups = CreateGroup.class)
    private UUID submissionId;


    @JsonBackReference("rubric-grade-executions")
    private RubricGradeDTO rubricGrade;

    @JsonBackReference("submission-grade-executions")
    private SubmissionDTO submission;

    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }
}

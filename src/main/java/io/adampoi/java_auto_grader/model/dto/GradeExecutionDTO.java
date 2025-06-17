package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeExecutionDTO {

    private UUID id;

    private BigDecimal pointsAwarded;

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

    @JsonIgnore
    private OffsetDateTime createdAt;

    @JsonIgnore
    private OffsetDateTime updatedAt;

    @JsonBackReference("rubric-grade-executions")
    private RubricGradeDTO rubricGrade;

    @JsonBackReference("submission-grade-executions")
    private SubmissionDTO submission;

    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }
}

package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
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
    private UUID rubricGrade;

    @NotNull(groups = CreateGroup.class)
    private UUID submission;

    @JsonIgnore
    private OffsetDateTime createdAt;

    @JsonIgnore
    private OffsetDateTime updatedAt;

    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }
}

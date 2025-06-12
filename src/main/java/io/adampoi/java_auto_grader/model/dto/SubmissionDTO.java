package io.adampoi.java_auto_grader.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionDTO {

    private UUID id;

    private OffsetDateTime submissionTime;

    @NotNull
    private Integer attemptNumber;

    @Size(min = 3, max = 255)
    private String status;

    private String graderFeedback;

    private OffsetDateTime gradingStartedAt;

    private OffsetDateTime gradingCompletedAt;

    @NotNull(groups = CreateGroup.class)
    private UUID assignment;

    @NotNull(groups = CreateGroup.class)
    private UUID student;

    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }
}

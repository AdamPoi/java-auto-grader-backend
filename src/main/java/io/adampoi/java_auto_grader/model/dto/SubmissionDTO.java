package io.adampoi.java_auto_grader.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;


@Getter
@Setter
public class SubmissionDTO {

    private UUID id;

    private OffsetDateTime submissionTime;

    @NotNull
    private Integer attemptNumber;

    @Size(max = 255)
    private String status;

    private String graderFeedback;

    private OffsetDateTime gradingStartedAt;

    private OffsetDateTime gradingCompletedAt;

    @NotNull
    @Size(max = 512)
    private String submissionBasePath;

    @NotNull
    private UUID assignment;

    @NotNull
    private UUID student;

    @NotNull
    private UUID classroom;

}

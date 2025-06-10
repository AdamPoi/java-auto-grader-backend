package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;


@Getter
@Setter
public class AssignmentDTO {
    private UUID id;


    @NotNull
    @Size(max = 255)
    private String title;

    private String description;

    private OffsetDateTime dueDate;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    @JsonProperty("isPublished")
    private Boolean isPublished;

    @Size(max = 512)
    private String starterCodeBasePath;

    @Size(max = 512)
    private String solutionCodeBasePath;

    private Integer maxAttempts;

    @NotNull
    private UUID course;

    private UUID createdByTeacher;

}

package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
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
public class AssignmentDTO {
    private UUID id;

    @NotNull(groups = CreateGroup.class)
    @NotEmpty(groups = CreateGroup.class)
    @Size(min = 3, max = 255)
    private String title;

    private String description;

    @NotNull(groups = CreateGroup.class)
    private OffsetDateTime dueDate;

    @JsonIgnore
    private OffsetDateTime createdAt;

    @JsonIgnore
    private OffsetDateTime updatedAt;

    @JsonProperty("isPublished")
    private Boolean isPublished;

    @Size(max = 512)
    private String starterCodeBasePath;

    @Size(max = 512)
    private String solutionCodeBasePath;

    private Integer maxAttempts;

    @NotNull(groups = CreateGroup.class)
    private UUID course;

    @NotNull(groups = CreateGroup.class)
    private UUID createdByTeacher;

    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }
}

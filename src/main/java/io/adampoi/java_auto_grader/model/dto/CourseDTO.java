package io.adampoi.java_auto_grader.model.dto;

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
public class CourseDTO {

    private UUID id;

    @NotNull(groups = CreateGroup.class)
    @NotEmpty(groups = CreateGroup.class)
    @Size(min = 3, max = 20)
    private String code;

    @NotNull(groups = CreateGroup.class)
    @NotEmpty(groups = CreateGroup.class)
    @Size(min = 3, max = 255)
    private String name;

    private String description;

    @JsonProperty("isActive")
    private Boolean isActive;

    private OffsetDateTime enrollmentStartDate;

    private OffsetDateTime enrollmentEndDate;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    @NotNull(groups = CreateGroup.class)
    private UUID createdByTeacher;

    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }
}

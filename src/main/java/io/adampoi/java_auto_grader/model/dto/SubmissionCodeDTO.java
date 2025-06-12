package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class SubmissionCodeDTO {

    private UUID id;

    @NotNull(groups = CreateGroup.class)
    @NotEmpty(groups = CreateGroup.class)
    @Size(min = 3, max = 255)
    private String fileName;

    @NotNull(groups = CreateGroup.class)
    @NotEmpty(groups = CreateGroup.class)
    @Size(min = 0, max = 5000)
    private String sourceCode;

    private String packagePath;

    private String className;

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

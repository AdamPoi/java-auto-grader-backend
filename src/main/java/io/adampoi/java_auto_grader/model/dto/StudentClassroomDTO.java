package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class StudentClassroomDTO {

    private UUID id;

    private OffsetDateTime enrollmentDate;

    @JsonProperty("isActive")
    private Boolean isActive;

    @NotNull
    private UUID student;

    @NotNull
    private UUID classroom;

    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }
}

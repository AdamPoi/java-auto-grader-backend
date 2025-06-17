package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomDTO {
    private UUID id;

    @NotNull
    @Size(min = 3, max = 255)
    private String name;

    private Boolean isActive;

    @JsonProperty("teacher")
    @JsonManagedReference("classroom-teacher")
    @JsonIgnoreProperties({"password", "roles", "permissions", "createdAt", "updatedAt"})
    private UserDTO teacher;

    @JsonProperty("students")
    @JsonManagedReference("classroom-students")
    @JsonIgnoreProperties({"password", "roles", "permissions", "createdAt", "updatedAt"})
    private List<UserDTO> enrolledStudents;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    private UUID teacherId;

    private List<UUID> studentIds;


    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }
}
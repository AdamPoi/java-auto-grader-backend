package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;


@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO extends AuditableDTO {

    private UUID id;

    @NotNull(groups = CreateGroup.class)
    @NotEmpty(groups = CreateGroup.class)
    @Size(min = 3, max = 20)
    @Column(nullable = false, unique = true)
    private String code;

    @NotNull(groups = CreateGroup.class)
    @NotEmpty(groups = CreateGroup.class)
    @Size(min = 3, max = 255)
    private String name;

    private String description;

    @JsonProperty("isActive")
    @Builder.Default
    private Boolean isActive = true;


    private UUID teacherId;

    private List<UUID> studentIds;

    private List<UUID> assignmentIds;


    @JsonProperty("teacher")
    @JsonManagedReference("course-teacher")
    @JsonIgnoreProperties({"password", "roles", "permissions", "submissions", "createdAt", "updatedAt"})
    private UserDTO createdByTeacher;

    @JsonProperty("students")
    @JsonManagedReference("course-students")
    @JsonIgnoreProperties({"password", "roles", "permissions", "submissions", "createdAt", "updatedAt"})
    private List<UserDTO> enrolledStudents;

    @JsonProperty("assignments")
    @JsonManagedReference("course-assignments")
    @JsonIgnoreProperties({"password", "description", "starterCode", "solutionCode", "maxAttempts", "timeLimit", "totalPoints", "courseId", "teacherId", "submissions", "rubrics"})
    private List<AssignmentDTO> courseAssignments;


    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }
}

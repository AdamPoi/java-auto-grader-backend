package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.Email;
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
public class UserDTO extends AuditableDTO {

    @JsonView(Views.External.class)
    private UUID id;

    @NotNull(groups = CreateGroup.class)
    @Size(min = 3, max = 100)
    @Email()
    @JsonView(Views.External.class)
    private String email;

    @NotNull(groups = CreateGroup.class)
    @Size(min = 6, max = 255)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JsonView(Views.External.class)
    private String password;

    @NotNull(groups = CreateGroup.class)
    @Size(min = 3, max = 255)
    @JsonView(Views.External.class)
    private String firstName;

    @Size(min = 0, max = 255)
    @JsonView(Views.External.class)
    private String lastName;

    @JsonProperty("isActive")
    @JsonView(Views.External.class)
    @Builder.Default
    private Boolean isActive = true;

    @JsonIgnore
    private List<UUID> userRoles;

    @JsonBackReference("classroom-teacher")
    private ClassroomDTO teacherClassroom;

    @JsonBackReference("classroom-students")
    private ClassroomDTO studentClassroom;

    @JsonBackReference("course-teacher")
    private CourseDTO teacherCourse;

    @JsonBackReference("course-students")
    private CourseDTO studentCourse;

    @JsonManagedReference("student-submissions")
    private List<SubmissionDTO> submissions;

    @NotNull(groups = CreateGroup.class)
    @NotEmpty(groups = CreateGroup.class)
    @JsonView(Views.Internal.class)
    private List<String> roles;

    @JsonView(Views.Internal.class)
    private List<String> permissions;

    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }
}
package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    private UUID id;

    @NotNull(groups = CreateGroup.class)
    @Size(min = 3, max = 100)
    @Email()
    private String email;

    //    @NotNull(groups = CreateGroup.class)
    @Size(min = 3, max = 100)
    private String nim;

    @Size(min = 3, max = 100)
    private String nip;


    @NotNull(groups = CreateGroup.class)
    @Size(min = 6, max = 255)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotNull(groups = CreateGroup.class)
    @Size(min = 3, max = 255)
    private String firstName;

    @Size(min = 0, max = 255)
    private String lastName;

    @JsonProperty("isActive")
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

    //    @JsonManagedReference("student-submissions")
    private List<SubmissionDTO> submissions;

    @NotNull(groups = CreateGroup.class)
    @NotEmpty(groups = CreateGroup.class)
    private List<String> roles;

    private List<String> permissions;

    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }
}
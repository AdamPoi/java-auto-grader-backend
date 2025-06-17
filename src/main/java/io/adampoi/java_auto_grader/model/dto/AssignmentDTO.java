package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
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

    private String starterCode;

    private String solutionCode;

    private String testCode;

    private Integer maxAttempts;

    private Integer timeLimit;

    @Builder.Default
    private BigDecimal totalPoints = BigDecimal.ZERO;

    @JsonProperty("courseId")
    @NotNull(groups = CreateGroup.class)
    private UUID courseId;

    @JsonProperty("teacherId")
    @NotNull(groups = CreateGroup.class)
    private UUID createdByTeacher;

    @JsonBackReference("course-assignments")
    private CourseDTO course;

    @JsonProperty("submissions")
    @JsonManagedReference("assignment-submissions")
//    @JsonIgnoreProperties({"password", "roles", "permissions", "createdAt", "updatedAt"})
    private List<SubmissionDTO> assignmentSubmissions;

    @JsonProperty("rubrics")
    @JsonManagedReference("assignment-rubrics")
//    @JsonIgnoreProperties({"password", "roles", "permissions", "createdAt", "updatedAt"})
    private List<RubricDTO> rubrics;

    @JsonProperty("rubricGrades")
    @JsonManagedReference("assignment-rubric-grades")
//    @JsonIgnoreProperties({"password", "roles", "permissions", "createdAt", "updatedAt"})
    private List<RubricGradeDTO> rubricGrades;

    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }
}

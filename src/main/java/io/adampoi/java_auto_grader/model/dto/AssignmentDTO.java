package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentDTO extends AuditableDTO {
    private UUID id;

    @NotNull(groups = CreateGroup.class)
    @NotEmpty(groups = CreateGroup.class)
    @Size(min = 3, max = 255)
    private String title;

    private String description;
    private String resource;

    @NotNull(groups = CreateGroup.class)
    private OffsetDateTime dueDate;

    @JsonProperty("isPublished")
    private Boolean isPublished;

    private String starterCode;
    private String solutionCode;
    private String testCode;

    private Long timeLimit;      // [optionally deprecated, move to options.timeLimit]
    private int totalPoints;

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
    private List<SubmissionDTO> assignmentSubmissions;

    @JsonProperty("rubrics")
    @JsonManagedReference("assignment-rubrics")
    @JsonIgnoreProperties({"assignmentId", "assignment", "rubricGradeIds", "rubricGrades", "createdAt", "updatedAt"})
    private List<RubricDTO> rubrics;

    @JsonProperty("rubricGrades")
    @JsonManagedReference("assignment-rubric-grades")
    private List<RubricGradeDTO> rubricGrades;

    // 🟢 ADD THIS FIELD:
    @JsonProperty("options")
    private AssignmentOptionsDTO options;

    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }
}
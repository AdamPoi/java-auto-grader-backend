package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
public class SubmissionDTO {

    private UUID id;

    private OffsetDateTime submissionTime;

    @NotNull
    private Integer attemptNumber;

    @Size(min = 3, max = 255)
    private String status;

    private String graderFeedback;

    private OffsetDateTime gradingStartedAt;

    private OffsetDateTime gradingCompletedAt;

    @NotNull(groups = CreateGroup.class)
    private UUID assignmentId;

    @NotNull(groups = CreateGroup.class)
    private UUID studentId;

    private List<SubmissionCodeDTO> submissionCodeIds;

    private List<GradeExecutionDTO> gradeExecutionIds;

    @JsonBackReference("assignment-submissions")
    private AssignmentDTO assignment;

    @JsonProperty("gradeExecutions")
    @JsonManagedReference("submission-grade-executions")
    private List<GradeExecutionDTO> gradeExecutions;

    @JsonProperty("submissionCodes")
    @JsonManagedReference("submission-codes")
    private List<SubmissionCodeDTO> submissionCodes;

    @JsonProperty("student")
    @JsonBackReference("student-submissions")
    @JsonIgnoreProperties({"password", "roles", "permissions", "createdAt", "updatedAt"})
    private UserDTO student;


    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }
}

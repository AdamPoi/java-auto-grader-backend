package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.adampoi.java_auto_grader.model.type.CompilationError;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionDTO extends AuditableDTO {

    private UUID id;

    @Size(min = 3, max = 255)
    private String status;

    private String type;

    private Long executionTime;

    private String feedback;

    private OffsetDateTime startedAt;

    private OffsetDateTime completedAt;

    private String mainClassName = "Main";

    private int totalPoints;

    @NotNull(groups = CreateGroup.class)
    private UUID assignmentId;

    //    @NotNull(groups = CreateGroup.class)
    private UUID studentId;

    private List<SubmissionCodeDTO> submissionCodeIds;

    private List<TestExecutionDTO> gradeExecutionIds;


    @JsonBackReference("assignment-submissions")
    private AssignmentDTO assignment;

    //    @JsonProperty("student")
//    @JsonBackReference("student-submissions")
    @JsonIgnoreProperties({"password", "roles", "permissions", "createdAt", "updatedAt"})
    private UserDTO student;

    @JsonProperty("testExecutions")
    @JsonManagedReference("submission-test-executions")
    private List<TestExecutionDTO> testExecutions;

    @JsonProperty("submissionCodes")
    @JsonManagedReference("submission-codes")
    private List<SubmissionCodeDTO> submissionCodes;

    private List<CompilationError> compilationErrors;



    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }
}

package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.adampoi.java_auto_grader.domain.RubricGrade;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RubricGradeDTO extends AuditableDTO {

    private String id;

    @NotNull(groups = CreateGroup.class)
    @NotEmpty(groups = CreateGroup.class)
    @Size(min = 3, max = 255)
    private String name;

    private String description;

    private Integer displayOrder;

    private Map<String, Object> arguments;

    private RubricGrade.GradeType gradeType;


    private UUID rubricId;

    private UUID assignmentId;

    private Set<UUID> testExecutionIds;


    @JsonBackReference("rubric-rubric-grades")
    private RubricDTO rubric;

    @JsonBackReference("assignment-rubric-grades")
    private AssignmentDTO assignment;

    @JsonProperty("testExecutions")
    @JsonManagedReference("rubric-grade-test-executions")
//    @JsonIgnoreProperties({"})
    private List<TestExecutionDTO> testExecutions;

    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }
}

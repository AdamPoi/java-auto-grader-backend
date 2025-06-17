package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.adampoi.java_auto_grader.domain.RubricGrade;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RubricGradeDTO {

    private UUID id;

    @NotNull(groups = CreateGroup.class)
    @NotEmpty(groups = CreateGroup.class)
    @Size(min = 3, max = 255)
    private String name;

    private String functionName;

    private String description;

    private BigDecimal points;

    private Integer displayOrder;


    private Map<String, Object> arguments;

    private RubricGrade.GradeType gradeType;

    private UUID rubricId;

    private UUID assignmentId;

    private Set<UUID> gradeExecutionIds;

    @JsonIgnore
    private OffsetDateTime createdAt;

    @JsonIgnore
    private OffsetDateTime updatedAt;

    @JsonBackReference("rubric-rubric-grades")
    private RubricDTO rubric;

    @JsonBackReference("assignment-rubric-grades")
    private AssignmentDTO assignment;

    @JsonProperty("gradeExecutions")
    @JsonManagedReference("rubric-grade-executions")
//    @JsonIgnoreProperties({"})
    private List<GradeExecutionDTO> gradeExecutions;

    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }
}

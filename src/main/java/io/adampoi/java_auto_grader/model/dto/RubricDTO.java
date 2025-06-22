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
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RubricDTO extends AuditableDTO {

    private UUID id;

    @NotNull(groups = CreateGroup.class)
    @NotEmpty(groups = CreateGroup.class)
    @Size(min = 3, max = 255)
    private String name;

    private String description;

    private BigDecimal points;

    @NotNull(groups = CreateGroup.class)
    private UUID assignmentId;

    private Set<UUID> rubricGradeIds;

    @JsonProperty("assignment")
    @JsonBackReference("assignment-rubrics")
    @JsonIgnoreProperties({"courseId", "course", "rubrics", "submissions", "assignmentSubmissions", "rubricGrades"})
    private AssignmentDTO assignment;

    @JsonProperty("rubricGrades")
    @JsonManagedReference("rubric-rubric-grades")
//    @JsonIgnoreProperties({"})
    private List<RubricGradeDTO> rubricGrades;

    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }

}

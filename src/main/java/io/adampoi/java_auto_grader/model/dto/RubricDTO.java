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
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RubricDTO {

    private UUID id;

    @NotNull(groups = CreateGroup.class)
    @NotEmpty(groups = CreateGroup.class)
    @Size(min = 3, max = 255)
    private String name;

    private String description;

    private BigDecimal maxPoints;

    private Integer displayOrder;

    @JsonProperty("isActive")
    @Builder.Default
    private Boolean isActive = true;

    @NotNull(groups = CreateGroup.class)
    private UUID assignmentId;

    private Set<UUID> rubricGradeIds;

    @JsonIgnore
    private OffsetDateTime createdAt;

    @JsonIgnore
    private OffsetDateTime updatedAt;


    @JsonBackReference("assignment-rubrics")
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

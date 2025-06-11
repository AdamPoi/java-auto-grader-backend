package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.adampoi.java_auto_grader.domain.RubricGrade;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class RubricGradeDTO {

    private UUID id;

    @NotNull(groups = CreateGroup.class)
    @NotEmpty(groups = CreateGroup.class)
    @Size(min = 3, max = 255)
    private String name;

    private String description;

    private BigDecimal points;

    private Integer displayOrder;

    private String code;

    private Map<String, Object> arguments;

    private RubricGrade.GradeType gradeType;

    private UUID rubric;

    private Set<UUID> gradeExecutions;

    @JsonIgnore
    private OffsetDateTime createdAt;

    @JsonIgnore
    private OffsetDateTime updatedAt;


    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }
}

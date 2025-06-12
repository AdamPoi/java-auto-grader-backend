package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private Boolean isActive = true;

    @NotNull(groups = CreateGroup.class)
    private UUID assignment;

    private Set<UUID> rubricGrades;

    @JsonIgnore
    private OffsetDateTime createdAt;

    @JsonIgnore
    private OffsetDateTime updatedAt;


    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }

}

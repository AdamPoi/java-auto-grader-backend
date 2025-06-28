package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;


@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO extends AuditableDTO {

    @JsonView(Views.External.class)
    private UUID id;

    @NotBlank(groups = {CreateGroup.class})
    @Size(min = 3, max = 255, groups = {CreateGroup.class, UpdateGroup.class})
    @JsonView(Views.External.class)
    private String name;

    @Size(max = 255, groups = {CreateGroup.class, UpdateGroup.class})
    @JsonView(Views.External.class)
    private String description;

    @JsonBackReference
    private RoleDTO role;

    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }

}

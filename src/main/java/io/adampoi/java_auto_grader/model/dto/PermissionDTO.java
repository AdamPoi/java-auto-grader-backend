package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO {
    private UUID id;
    @NotBlank(groups = {CreateGroup.class})
    @Size(min = 3, max = 255, groups = {CreateGroup.class, UpdateGroup.class})
    private String name;

    @Size(max = 255, groups = {CreateGroup.class, UpdateGroup.class})
    private String description;

    @JsonBackReference
    private RoleDTO role;

    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }

}

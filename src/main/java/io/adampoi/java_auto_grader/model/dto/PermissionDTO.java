package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
public class PermissionDTO {
    private UUID id;
    @NotBlank(groups = {CreateGroup.class})
    @Size(min = 3, max = 255, groups = {CreateGroup.class, UpdateGroup.class})
    private String Name;

    @Size(max = 255, groups = {CreateGroup.class, UpdateGroup.class})
    private String description;

    @JsonBackReference
    private RoleDTO role;

    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }

}

package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RoleDTO {

    private UUID id;

    @NotBlank(groups = {CreateGroup.class})
    @Size(min = 3, max = 255, groups = {CreateGroup.class, UpdateGroup.class})
    private String Name;

    @JsonIgnore
    private List<UUID> rolePermissions;

    @JsonManagedReference
    private List<PermissionDTO> permissions;


    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }
}

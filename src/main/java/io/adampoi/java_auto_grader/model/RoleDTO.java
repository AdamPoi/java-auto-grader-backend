package io.adampoi.java_auto_grader.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;


@Getter
@Setter
public class RoleDTO {

    private UUID id;

    @NotNull
    @Size(max = 255)
    private String Name;

    @JsonIgnore
    private List<UUID> rolePermissionPermissions;

    private List<String> permissions;
}

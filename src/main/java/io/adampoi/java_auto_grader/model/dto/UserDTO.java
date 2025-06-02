package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UserDTO {

    private UUID id;

    @NotNull(groups = CreateGroup.class)
    @Size(min = 3, max = 100)
    @Email()
    private String email;

    @NotNull(groups = CreateGroup.class)
    @Size(min = 6, max = 255)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotNull(groups = CreateGroup.class)
    @Size(min = 3, max = 255)
    private String firstName;

    @Size(min = 0, max = 255)
    private String lastName;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonIgnore
    private OffsetDateTime createdAt;

    @JsonIgnore
    private OffsetDateTime updatedAt;

    @JsonIgnore
    private List<UUID> userRoles;

    private List<String> roles;

    private List<String> permissions;

    public interface CreateGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }
}

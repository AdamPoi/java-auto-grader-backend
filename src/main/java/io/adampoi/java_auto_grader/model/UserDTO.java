package io.adampoi.java_auto_grader.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
public class UserDTO {

    private UUID id;

    @NotNull
    @Size(max = 100)
    @Email()
    private String email;

    @NotNull
    @Size(max = 255)
    private String password;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @JsonProperty("isActive")
    private Boolean isActive;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    private List<UUID> userRoleRoles;

}

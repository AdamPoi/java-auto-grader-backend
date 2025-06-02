package io.adampoi.java_auto_grader.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.adampoi.java_auto_grader.model.dto.UserDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UserCreateRequest extends UserDTO {
    @NotBlank()
    @Size(min = 3, max = 100)
    @Email()
    private String email;
    
    @NotBlank()
    @Size(min = 6, max = 255)
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    private String password;

    @NotBlank()
    @Size(min = 3, max = 255)
    private String firstName;

    @Size(min = 3, max = 255)
    private String lastName;

    private List<UUID> userRoles;
}

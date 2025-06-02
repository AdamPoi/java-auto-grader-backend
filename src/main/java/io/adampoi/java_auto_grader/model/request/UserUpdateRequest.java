package io.adampoi.java_auto_grader.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UserUpdateRequest {
    @Size(min = 3, max = 100)
    @Email()
    private String email;

    @Size(min = 6, max = 255)
    @JsonIgnore
    private String password;

    @Size(min = 3, max = 255)
    private String firstName;

    @Size(min = 3, max = 255)
    private String lastName;

    private List<UUID> roles;
}

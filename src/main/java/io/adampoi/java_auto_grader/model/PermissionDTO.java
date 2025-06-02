package io.adampoi.java_auto_grader.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
public class PermissionDTO {

    private UUID id;

    @NotNull
    @Size(max = 100)
    private String Name;

    private String description;

}

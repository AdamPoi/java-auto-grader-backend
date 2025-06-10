package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;


@Getter
@Setter
public class CourseDTO {

    private UUID id;

    @NotNull
    @Size(max = 20)
    private String Code;

    @NotNull
    @Size(max = 255)
    private String Name;

    private String description;

    @JsonProperty("isActive")
    private Boolean isActive;

    private OffsetDateTime enrollmentStartDate;

    private OffsetDateTime enrollmentEndDate;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    private UUID createdByTeacher;

}

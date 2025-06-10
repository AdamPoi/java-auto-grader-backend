package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class StudentClassroomDTO {

    private UUID id;

    private OffsetDateTime enrollmentDate;

    @JsonProperty("isActive")
    private Boolean isActive;

    private UUID student;

    private UUID classroom;

}

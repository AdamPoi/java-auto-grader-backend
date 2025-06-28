package io.adampoi.java_auto_grader.model.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AuditableDTO {

    @JsonView(Views.Internal.class)
    private String createdBy;
    @JsonView(Views.Internal.class)
    private String updatedBy;
    @JsonView(Views.Internal.class)
    private OffsetDateTime createdAt;
    @JsonView(Views.Internal.class)
    private OffsetDateTime updatedAt;
}

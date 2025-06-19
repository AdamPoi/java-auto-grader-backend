package io.adampoi.java_auto_grader.model.dto;

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

    private OffsetDateTime createdBy;
    private OffsetDateTime updatedBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

}

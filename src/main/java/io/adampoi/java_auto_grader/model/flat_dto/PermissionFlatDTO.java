package io.adampoi.java_auto_grader.model.flat_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionFlatDTO {
    private UUID id;
    private String name;
    private String description;
}

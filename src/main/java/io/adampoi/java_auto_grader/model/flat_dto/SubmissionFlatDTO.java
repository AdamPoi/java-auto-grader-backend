package io.adampoi.java_auto_grader.model.flat_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionFlatDTO {
    private UUID id;
    private Long executionTime;
    private String type;
    private String status;
    private String manualFeedback;
    private OffsetDateTime startedAt;
    private OffsetDateTime completedAt;
    private Integer totalPoints;
    private UUID assignmentId;
    private UUID studentId;
    private String studentName;
}

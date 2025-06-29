package io.adampoi.java_auto_grader.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimedAssessmentAttempt implements Serializable {
    private UUID studentId;
    private UUID assignmentId;
    private OffsetDateTime startedAt;
    private boolean submitted;
}
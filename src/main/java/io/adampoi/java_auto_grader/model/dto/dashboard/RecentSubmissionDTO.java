package io.adampoi.java_auto_grader.model.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentSubmissionDTO {
    private UUID submissionId;
    private String studentName;
    private String assignmentTitle;
    private String courseName;
    private OffsetDateTime submittedAt;
    private String status;
}
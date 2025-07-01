package io.adampoi.java_auto_grader.model.dto.dashboard;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WelcomeBannerDTO {
    private String studentName;
    private UUID nextAssignmentId;
    private String nextAssignmentTitle;
    private String nextAssignmentCourse;
    private String nextAssignmentDueDate;
}
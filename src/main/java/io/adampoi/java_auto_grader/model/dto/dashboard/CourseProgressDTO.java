package io.adampoi.java_auto_grader.model.dto.dashboard;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseProgressDTO {
    private UUID courseId;
    private String courseName;
    private double average;
    private List<ScoreOverTimeDTO> chartData;
}
package io.adampoi.java_auto_grader.model.dto.dashboard;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentGradeDTO {
    private String courseName;
    private String assignmentTitle;
    private String grade;
    private String status;
}
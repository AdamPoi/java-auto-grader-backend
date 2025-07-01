package io.adampoi.java_auto_grader.model.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboardDTO {
    private WelcomeBannerDTO welcomeBanner;
    private List<RecentGradeDTO> recentGrades;
    private CourseProgressDTO overallPerformance;
    private List<CourseProgressDTO> courseProgress;
    private List<UpcomingDeadlineDTO> upcomingDeadlines;
}
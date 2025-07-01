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
public class AdminDashboardDTO {
    private DashboardKpiDTO kpis;
    private List<RecentSubmissionDTO> recentSubmissions;
    private List<AssignmentPerformanceDTO> classPerformance;
    private List<UpcomingDeadlineDTO> upcomingDeadlines;
}
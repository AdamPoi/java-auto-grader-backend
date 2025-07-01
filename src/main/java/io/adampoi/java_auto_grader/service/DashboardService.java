package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.domain.Assignment;
import io.adampoi.java_auto_grader.domain.Course;
import io.adampoi.java_auto_grader.domain.Submission;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.model.dto.dashboard.*;
import io.adampoi.java_auto_grader.repository.AssignmentRepository;
import io.adampoi.java_auto_grader.repository.CourseRepository;
import io.adampoi.java_auto_grader.repository.SubmissionRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    public AdminDashboardDTO getAdminDashboardData(User user) {
        AdminDashboardDTO dashboard = new AdminDashboardDTO();

        if (user.isAdmin()) {
            List<Submission> recentSubmissions = submissionRepository.findTop50ByTypeOrderByCompletedAtDesc(Submission.SubmissionType.FINAL);

            List<Assignment> assignmentsByLastSubmission = recentSubmissions.stream()
                    .map(Submission::getAssignment)
                    .filter(Objects::nonNull)
                    .distinct()
                    .limit(5)
                    .collect(Collectors.toList());
            dashboard.setKpis(getAdminKpis());
            dashboard.setRecentSubmissions(getAdminRecentSubmissions());
            dashboard.setUpcomingDeadlines(getAdminUpcomingDeadlines());
            dashboard.setClassPerformance(getAssignmentPerformance(assignmentsByLastSubmission));
        } else {
            List<Course> teacherCourses = new ArrayList<>(user.getTeacherCourses());
            List<Assignment> teacherAssignments = assignmentRepository.findAllByCourseIn(teacherCourses);

            dashboard.setKpis(getTeacherKpis(teacherCourses));
            dashboard.setRecentSubmissions(getTeacherRecentSubmissions(teacherCourses));
            dashboard.setUpcomingDeadlines(getTeacherUpcomingDeadlines(teacherCourses));
            dashboard.setClassPerformance(getAssignmentPerformance(teacherAssignments));
        }

        return dashboard;
    }

    public StudentDashboardDTO getStudentDashboardData(User authenticatedUser) {
        User student = userRepository.findByIdWithCoursesAndAssignments(authenticatedUser.getId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        StudentDashboardDTO dashboard = new StudentDashboardDTO();
        List<Submission> studentFinalSubmissions = submissionRepository.findAllByStudentAndType(student, Submission.SubmissionType.FINAL);

        dashboard.setWelcomeBanner(createWelcomeBanner(student, new ArrayList<>(student.getEnrolledCourses().stream().flatMap(c -> c.getCourseAssignments().stream()).collect(Collectors.toList()))));
        dashboard.setRecentGrades(createRecentGrades(studentFinalSubmissions));
        dashboard.setOverallPerformance(createOverallPerformance(studentFinalSubmissions));
        dashboard.setCourseProgress(createCourseProgress(student, studentFinalSubmissions));
        dashboard.setUpcomingDeadlines(getUpcomingDeadlinesForCourses(new ArrayList<>(student.getEnrolledCourses())));
        return dashboard;
    }

    // Admin Data
    private DashboardKpiDTO getAdminKpis() {
        DashboardKpiDTO kpis = new DashboardKpiDTO();
        kpis.setActiveAssignments(assignmentRepository.count());
        kpis.setActiveCourses(courseRepository.count());
        // Assuming you have a custom method in your repository for this
        kpis.setTotalStudents(userRepository.countUsersByRoleName("STUDENT"));
        return kpis;
    }

    private List<RecentSubmissionDTO> getAdminRecentSubmissions() {
        return submissionRepository.findTop5ByTypeOrderByCompletedAtDesc(Submission.SubmissionType.FINAL).stream()
                .map(this::mapToRecentSubmissionDTO)
                .collect(Collectors.toList());
    }

    private List<UpcomingDeadlineDTO> getAdminUpcomingDeadlines() {
        return assignmentRepository.findTop5ByDueDateAfterOrderByDueDateAsc(OffsetDateTime.now()).stream()
                .map(this::mapToUpcomingDeadlineDTO)
                .collect(Collectors.toList());
    }

    // Teacher Data
    private DashboardKpiDTO getTeacherKpis(List<Course> courses) {
        DashboardKpiDTO kpis = new DashboardKpiDTO();
        long studentCount = courses.stream().mapToLong(c -> c.getEnrolledUsers().size()).sum();

        kpis.setActiveAssignments(assignmentRepository.countByCourseIn(courses));
        kpis.setActiveCourses(courses.size());
        kpis.setTotalStudents(studentCount);
        return kpis;
    }

    private List<RecentSubmissionDTO> getTeacherRecentSubmissions(List<Course> courses) {
        return submissionRepository.findTop5ByTypeAndAssignment_CourseInOrderByCompletedAtDesc(Submission.SubmissionType.FINAL, courses).stream()
                .map(this::mapToRecentSubmissionDTO)
                .collect(Collectors.toList());
    }

    private List<UpcomingDeadlineDTO> getTeacherUpcomingDeadlines(List<Course> courses) {
        return assignmentRepository.findTop5ByCourseInAndDueDateAfterOrderByDueDateAsc(courses, OffsetDateTime.now()).stream()
                .map(this::mapToUpcomingDeadlineDTO)
                .collect(Collectors.toList());
    }

    private List<AssignmentPerformanceDTO> getAssignmentPerformance(List<Assignment> assignments) {
        return assignments.stream()
                .map(assignment -> {
                    double averageScore = assignment.getAssignmentSubmissions().stream()
                            .filter(sub -> sub.getType() == Submission.SubmissionType.FINAL && sub.getTotalPoints() != null)
                            .mapToDouble(Submission::getTotalPoints)
                            .average()
                            .orElse(0.0);
                    return new AssignmentPerformanceDTO(assignment.getTitle(), Math.round(averageScore * 10.0) / 10.0);
                })
                .collect(Collectors.toList());
    }

    private RecentSubmissionDTO mapToRecentSubmissionDTO(Submission sub) {
        RecentSubmissionDTO dto = new RecentSubmissionDTO();
        dto.setSubmissionId(sub.getId());
        if (sub.getStudent() != null) {
            dto.setStudentName(sub.getStudent().getFirstName() + " " + sub.getStudent().getLastName());
        }
        if (sub.getAssignment() != null) {
            dto.setAssignmentTitle(sub.getAssignment().getTitle());
            if (sub.getAssignment().getCourse() != null) {
                dto.setCourseName(sub.getAssignment().getCourse().getName());
            }
        }
        dto.setSubmittedAt(sub.getCompletedAt());
        dto.setStatus(String.valueOf(sub.getStatus()));
        return dto;
    }

    private UpcomingDeadlineDTO mapToUpcomingDeadlineDTO(Assignment a) {
        UpcomingDeadlineDTO dto = new UpcomingDeadlineDTO();
        dto.setAssignmentId(a.getId());
        dto.setTitle(a.getTitle());
        if (a.getCourse() != null) {
            dto.setCourse(a.getCourse().getName());
        }
        dto.setDueDate(a.getDueDate());
        return dto;
    }

    private WelcomeBannerDTO createWelcomeBanner(User student, List<Assignment> assignments) {
        WelcomeBannerDTO banner = new WelcomeBannerDTO();
        banner.setStudentName(student.getFirstName());

        Optional<Assignment> nextAssignment = assignments.stream()
                .filter(a -> a.getDueDate() != null && a.getDueDate().isAfter(OffsetDateTime.now()))
                .min(Comparator.comparing(Assignment::getDueDate));

        nextAssignment.ifPresent(assignment -> {
            banner.setNextAssignmentId(assignment.getId());
            banner.setNextAssignmentTitle(assignment.getTitle());
            if (assignment.getCourse() != null) {
                banner.setNextAssignmentCourse(assignment.getCourse().getName());
            }
            banner.setNextAssignmentDueDate(assignment.getDueDate().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        });

        return banner;
    }

    private List<RecentGradeDTO> createRecentGrades(List<Submission> finalSubmissions) {
        return finalSubmissions.stream()
                .sorted(Comparator.comparing(Submission::getCompletedAt).reversed())
                .limit(5)
                .map(sub -> {
                    RecentGradeDTO dto = new RecentGradeDTO();
                    if (sub.getAssignment() != null) {
                        dto.setAssignmentTitle(sub.getAssignment().getTitle());
                        if (sub.getAssignment().getCourse() != null) {
                            dto.setCourseName(sub.getAssignment().getCourse().getName());
                        }
                    }
                    dto.setStatus(String.valueOf(sub.getStatus()));
                    dto.setGrade(sub.getTotalPoints() != null ? sub.getTotalPoints() + "/100" : "Pending");
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private CourseProgressDTO createOverallPerformance(List<Submission> submissions) {
        CourseProgressDTO performance = new CourseProgressDTO();
        double average = submissions.stream()
                .filter(s -> s.getTotalPoints() != null)
                .mapToDouble(Submission::getTotalPoints)
                .average()
                .orElse(0.0);

        performance.setAverage(Math.round(average * 10.0) / 10.0);
        return performance;
    }

    private List<UpcomingDeadlineDTO> getUpcomingDeadlinesForCourses(List<Course> courses) {
        return assignmentRepository.findTop5ByCourseInAndDueDateAfterOrderByDueDateAsc(courses, OffsetDateTime.now()).stream()
                .map(this::mapToUpcomingDeadlineDTO)
                .collect(Collectors.toList());
    }

    private List<CourseProgressDTO> createCourseProgress(User student, List<Submission> finalSubmissions) {
        Set<UUID> completedAssignmentIds = finalSubmissions.stream()
                .map(submission -> submission.getAssignment().getId())
                .collect(Collectors.toSet());

        return student.getEnrolledCourses().stream().map(course -> {
            CourseProgressDTO progress = new CourseProgressDTO();
            progress.setCourseId(course.getId());
            progress.setCourseName(course.getName());

            Set<Assignment> assignmentsInCourse = course.getCourseAssignments();
            long totalAssignmentsInCourse = assignmentsInCourse.size();

            long completedInCourse = assignmentsInCourse.stream()
                    .filter(assignment -> completedAssignmentIds.contains(assignment.getId()))
                    .count();

            double progressPercent = (totalAssignmentsInCourse > 0)
                    ? ((double) completedInCourse / totalAssignmentsInCourse) * 100
                    : 0;

            progress.setAverage((int) Math.round(progressPercent));
            return progress;
        }).collect(Collectors.toList());
    }
}
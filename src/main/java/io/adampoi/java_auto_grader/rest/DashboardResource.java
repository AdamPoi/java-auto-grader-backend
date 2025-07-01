package io.adampoi.java_auto_grader.rest;


import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.model.response.ApiSuccessResponse;
import io.adampoi.java_auto_grader.service.DashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Dashboard")
public class DashboardResource {

    private final DashboardService dashboardService;

    @GetMapping
    public ApiSuccessResponse<?> getDashboardData(@AuthenticationPrincipal User user) {
        boolean isAdminOrTeacher = user.getUserRoles().stream()
                .anyMatch(role -> role.getName().equals("admin") || role.getName().equals("teacher"));

        if (isAdminOrTeacher) {
            return ApiSuccessResponse.builder()
                    .data(dashboardService.getAdminDashboardData(user))
                    .statusCode(HttpStatus.OK)
                    .build();
        } else {
            return ApiSuccessResponse.builder()
                    .data(dashboardService.getStudentDashboardData(user))
                    .statusCode(HttpStatus.OK)
                    .build();
        }
    }
}
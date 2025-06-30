package io.adampoi.java_auto_grader.rest;

import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.model.dto.SubmissionDTO;
import io.adampoi.java_auto_grader.model.dto.TimedAssessmentAttempt;
import io.adampoi.java_auto_grader.model.request.TestSubmitRequest;
import io.adampoi.java_auto_grader.model.response.ApiSuccessResponse;
import io.adampoi.java_auto_grader.repository.UserRepository;
import io.adampoi.java_auto_grader.service.TimedAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/timed-assessments",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users")
public class TimedAssessmentResource {

    private final TimedAssessmentService timedAssessmentService;
    private final UserRepository userRepository;

    public TimedAssessmentResource(TimedAssessmentService timedAssessmentService, UserRepository userRepository) {
        this.timedAssessmentService = timedAssessmentService;
        this.userRepository = userRepository;
    }

    private UUID getStudentId(Principal principal) {
        User currentUser = userRepository.getUserByEmail(principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return currentUser.getId();
    }

    @PostMapping("/{assignmentId}/start")
    public ResponseEntity<?> start(@PathVariable UUID assignmentId, Principal principal) {
        try {
            TimedAssessmentAttempt attempt = timedAssessmentService.start(assignmentId, getStudentId(principal));
            return ResponseEntity.ok(
                    ApiSuccessResponse.<TimedAssessmentAttempt>builder()
                            .data(attempt)
                            .statusCode(HttpStatus.OK)
                            .build()
            );
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{assignmentId}/status")
    public ResponseEntity<?> status(@PathVariable UUID assignmentId, Principal principal) {
        try {
            TimedAssessmentAttempt attempt = timedAssessmentService.getStatus(assignmentId, getStudentId(principal));
            return ResponseEntity.ok(
                    ApiSuccessResponse.<TimedAssessmentAttempt>builder()
                            .data(attempt)
                            .statusCode(HttpStatus.OK)
                            .build()
            );
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/{assignmentId}/submit")
    @PreAuthorize("hasAuthority('SUBMISSION:CREATE')")
    @Operation(summary = "Create Timed Submission", description = "Create a new timed assessment submission for an assignment")
    @ApiResponse(responseCode = "200", description = "Timed Submission created")
    public ApiSuccessResponse<SubmissionDTO> createTimedSubmission(
            @PathVariable UUID assignmentId,
            @RequestBody @Validated(TestSubmitRequest.CreateGroup.class) final TestSubmitRequest request
    ) {
        SubmissionDTO createdSubmission = timedAssessmentService.submitTimedAssessment(
                UUID.fromString(request.getUserId()), request);
        return ApiSuccessResponse.<SubmissionDTO>builder()
                .data(createdSubmission)
                .statusCode(HttpStatus.OK)
                .build();
    }

//    @PostMapping("/{assignmentId}/submit")
//    public ResponseEntity<?> submit(@PathVariable UUID assignmentId,
//                                    @RequestBody SubmissionCodeDTO codeDto,
//                                    Principal principal) {
//        try {
//            Submission submission = timedAssessmentService.submit(assignmentId, getStudentId(principal), codeDto);
//            return ResponseEntity.ok(Map.of(
//                    "submissionId", submission.getId(),
//                    "completedAt", submission.getCompletedAt()
//            ));
//        } catch (IllegalStateException e) {
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//        }
//    }
}
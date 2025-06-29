package io.adampoi.java_auto_grader.rest;

import io.adampoi.java_auto_grader.domain.Submission;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.model.dto.SubmissionCodeDTO;
import io.adampoi.java_auto_grader.model.dto.TimedAssessmentAttempt;
import io.adampoi.java_auto_grader.repository.UserRepository;
import io.adampoi.java_auto_grader.service.TimedAssessmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
            return ResponseEntity.ok(Map.of(
                    "startedAt", attempt.getStartedAt()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{assignmentId}/status")
    public ResponseEntity<?> status(@PathVariable UUID assignmentId, Principal principal) {
        try {
            Map<String, Object> status = timedAssessmentService.getStatus(assignmentId, getStudentId(principal));
            return ResponseEntity.ok(status);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{assignmentId}/submit")
    public ResponseEntity<?> submit(@PathVariable UUID assignmentId,
                                    @RequestBody SubmissionCodeDTO codeDto,
                                    Principal principal) {
        try {
            Submission submission = timedAssessmentService.submit(assignmentId, getStudentId(principal), codeDto);
            return ResponseEntity.ok(Map.of(
                    "submissionId", submission.getId(),
                    "completedAt", submission.getCompletedAt()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
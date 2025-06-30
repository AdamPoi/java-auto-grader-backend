package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.domain.*;
import io.adampoi.java_auto_grader.model.dto.RubricGradeDTO;
import io.adampoi.java_auto_grader.model.dto.SubmissionDTO;
import io.adampoi.java_auto_grader.model.dto.TimedAssessmentAttempt;
import io.adampoi.java_auto_grader.model.request.TestSubmitRequest;
import io.adampoi.java_auto_grader.repository.AssignmentRepository;
import io.adampoi.java_auto_grader.repository.RubricRepository;
import io.adampoi.java_auto_grader.repository.SubmissionRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.cache.Cache;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TimedAssessmentService {
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final javax.cache.CacheManager jcacheManager;
    private final SubmissionService submissionService;
    private final RubricRepository rubricRepository;

    public TimedAssessmentService(
            SubmissionRepository submissionRepository,
            AssignmentRepository assignmentRepository,
            UserRepository userRepository,
            CacheManager springCacheManager,
            SubmissionService submissionService,
            RubricRepository rubricRepository) {
        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.submissionService = submissionService;
        if (springCacheManager instanceof JCacheCacheManager) {
            this.jcacheManager = ((JCacheCacheManager) springCacheManager).getCacheManager();
        } else {
            throw new IllegalStateException("Expected a JCacheCacheManager!");
        }
        this.rubricRepository = rubricRepository;
    }

    private long getAssignmentTimeLimitMs(Assignment assignment) {
        if (assignment.getOptions() != null && assignment.getOptions().getTimeLimit() != null) {
            return assignment.getOptions().getTimeLimit() * 1000L;
        } else {
            return 3600_000L; // 1 hour default
        }
    }

    private Cache<String, TimedAssessmentAttempt> getOrCreateExamCache(UUID assignmentId, long ttlMs) {
        String cacheName = "examCache-" + assignmentId;
        Cache<String, TimedAssessmentAttempt> cache = jcacheManager.getCache(cacheName, String.class, TimedAssessmentAttempt.class);
        if (cache == null) {
            MutableConfiguration<String, TimedAssessmentAttempt> config =
                    new MutableConfiguration<String, TimedAssessmentAttempt>()
                            .setTypes(String.class, TimedAssessmentAttempt.class)
                            .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.MILLISECONDS, ttlMs)))
                            .setStoreByValue(false);
            try {
                cache = jcacheManager.createCache(cacheName, config);
            } catch (javax.cache.CacheException e) {
                cache = jcacheManager.getCache(cacheName, String.class, TimedAssessmentAttempt.class);
            }
            log.info("Using cache {} with TTL: {} ms", cacheName, ttlMs);
        }
        return cache;
    }


    @Transactional
    public TimedAssessmentAttempt start(UUID assignmentId, UUID studentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        long baseTimeLimitMs = getAssignmentTimeLimitMs(assignment);
        long ttlMs = baseTimeLimitMs + (2 * 60 * 60 * 1000L); // +2 hours
        Cache<String, TimedAssessmentAttempt> cache = getOrCreateExamCache(assignmentId, ttlMs);
        String key = studentId + ":" + assignmentId;

        Optional<Submission> existingSubmissionOpt = submissionRepository.findByAssignmentAndStudentAndType(
                assignment, student, Submission.SubmissionType.FINAL
        );

        if (existingSubmissionOpt.isPresent()) {
            Submission existing = existingSubmissionOpt.get();

            if (existing.getStatus() == Submission.SubmissionStatus.COMPLETED) {
                throw new IllegalStateException("Assessment already submitted.");
            }
            if (existing.getStatus() == Submission.SubmissionStatus.IN_PROGRESS) {
                // If IN_PROGRESS: return cached attempt or recreate cache
                TimedAssessmentAttempt attempt = cache.get(key);
                if (attempt == null) {
                    attempt = new TimedAssessmentAttempt();
                    attempt.setStudentId(studentId);
                    attempt.setAssignmentId(assignmentId);
                    attempt.setStartedAt(existing.getStartedAt());
                    attempt.setSubmitted(false);
                    cache.put(key, attempt);
                }
                return attempt;
            }
            if (existing.getStatus() == Submission.SubmissionStatus.TIMEOUT) {
                throw new IllegalStateException("Time expired for assessment.");
            }
        }

        // If no FINAL submission, create new
        Submission submission = Submission.builder()
                .assignment(assignment)
                .student(student)
                .status(Submission.SubmissionStatus.IN_PROGRESS)
                .type(Submission.SubmissionType.FINAL)
                .startedAt(OffsetDateTime.now())
                .build();

        submissionRepository.save(submission);

        TimedAssessmentAttempt attempt = new TimedAssessmentAttempt();
        attempt.setStudentId(studentId);
        attempt.setAssignmentId(assignmentId);
        attempt.setStartedAt(submission.getStartedAt());
        attempt.setSubmitted(false);
        cache.put(key, attempt);

        return attempt;
    }

    @Transactional(readOnly = true)
    public TimedAssessmentAttempt getStatus(UUID assignmentId, UUID studentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        long baseTimeLimitMs = getAssignmentTimeLimitMs(assignment);
        long ttlMs = baseTimeLimitMs + (2 * 60 * 60 * 1000L);
        Cache<String, TimedAssessmentAttempt> cache = getOrCreateExamCache(assignmentId, ttlMs);

        String key = studentId + ":" + assignmentId;
        TimedAssessmentAttempt attempt = cache.get(key);

        Optional<Submission> submissionOpt = submissionRepository.findByAssignmentAndStudentAndType(
                assignment, userRepository.findById(studentId)
                        .orElseThrow(() -> new EntityNotFoundException("Student not found")),
                Submission.SubmissionType.FINAL
        );
        Submission.SubmissionStatus submissionStatus = Submission.SubmissionStatus.IN_PROGRESS;
        OffsetDateTime startedAt = null;
        if (submissionOpt.isPresent()) {
            submissionStatus = submissionOpt.get().getStatus();
            startedAt = submissionOpt.get().getStartedAt();
        }

        if (attempt == null && submissionOpt.isPresent() && submissionStatus == Submission.SubmissionStatus.IN_PROGRESS) {
            attempt = new TimedAssessmentAttempt();
            attempt.setStudentId(studentId);
            attempt.setAssignmentId(assignmentId);
            attempt.setStartedAt(startedAt);
            attempt.setSubmitted(false);
            cache.put(key, attempt);
        }
        if (attempt == null && submissionOpt.isPresent() && submissionStatus == Submission.SubmissionStatus.COMPLETED) {
            attempt = new TimedAssessmentAttempt();
            attempt.setStudentId(studentId);
            attempt.setAssignmentId(assignmentId);
            attempt.setStartedAt(startedAt);
            attempt.setSubmitted(true);
        }
        if (attempt == null) {
            throw new IllegalStateException("Assessment not started");
        }

        OffsetDateTime attemptStartedAt = attempt.getStartedAt();
        long elapsedMs = 0;
        if (attemptStartedAt != null) {
            elapsedMs = java.time.Duration.between(attemptStartedAt, OffsetDateTime.now()).toMillis();
        } else {
            if (startedAt != null) {
                elapsedMs = java.time.Duration.between(startedAt, OffsetDateTime.now()).toMillis();
                attempt.setStartedAt(startedAt);
            } else {
                attempt.setExpired(true);
                attempt.setSubmitted(submissionStatus == Submission.SubmissionStatus.COMPLETED);
                return attempt;
            }
        }
        long remainingMs = Math.max(0, baseTimeLimitMs - elapsedMs);

        attempt.setExpired(remainingMs == 0 || submissionStatus == Submission.SubmissionStatus.TIMEOUT);
        attempt.setSubmitted(submissionStatus == Submission.SubmissionStatus.COMPLETED);

        attempt.setRemainingMs(remainingMs);

        return attempt;
    }

    @Transactional
    public SubmissionDTO submitTimedAssessment(UUID studentId, TestSubmitRequest request) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
        Assignment assignment = assignmentRepository.findById(UUID.fromString(request.getAssignmentId()))
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));

        long baseTimeLimitMs = getAssignmentTimeLimitMs(assignment);
        long ttlMs = baseTimeLimitMs + (2 * 60 * 60 * 1000L);
        Cache<String, TimedAssessmentAttempt> cache = getOrCreateExamCache(UUID.fromString(request.getAssignmentId()), ttlMs);

        String key = studentId + ":" + request.getAssignmentId();
        TimedAssessmentAttempt attempt = cache.get(key);

        if (attempt == null) throw new IllegalStateException("Assessment not started");
        if (attempt.isSubmitted()) throw new IllegalStateException("Already submitted");

        Submission submission = submissionRepository.findByAssignmentAndStudentAndType(
                assignment, student, Submission.SubmissionType.FINAL
        ).orElseThrow(() -> new EntityNotFoundException("No in-progress Submission found"));

        long elapsedMs = java.time.Duration.between(attempt.getStartedAt(), OffsetDateTime.now()).toMillis();
        if (elapsedMs > baseTimeLimitMs) {
            attempt.setSubmitted(true);
            cache.put(key, attempt);
            submission.setStatus(Submission.SubmissionStatus.TIMEOUT);
            submission.setCompletedAt(OffsetDateTime.now());
            submissionRepository.save(submission);
            throw new IllegalStateException("Time expired");
        }

        SubmissionDTO processed = submissionService.processSubmissionWithTestResults(
                assignment,
                student,
                request.getSourceFiles(),
                request.getTestFiles(),
                request.getMainClassName(),
                request.getBuildTool(),
                Submission.SubmissionType.FINAL,
                false
        );


        Set<TestExecution> newTestExecutions = processed.getTestExecutions().stream()
                .map(exec -> {
                    TestExecution copy = new TestExecution();
                    RubricGrade rubricGrade = mapRubricGradeToEntity(exec.getRubricGrade(), new RubricGrade());
                    copy.setRubricGrade(rubricGrade);
                    copy.setSubmission(submission);
                    copy.setMethodName(exec.getMethodName());
                    copy.setExecutionTime(exec.getExecutionTime());
                    copy.setOutput(exec.getOutput());
                    copy.setError(exec.getError());
                    copy.setStatus(TestExecution.ExecutionStatus.valueOf(exec.getStatus()));
                    return copy;
                })
                .collect(Collectors.toSet());

        submission.setStatus(Submission.SubmissionStatus.COMPLETED);
        submission.setCompletedAt(OffsetDateTime.now());
        submission.setExecutionTime(elapsedMs);
        submission.setFeedback(processed.getFeedback());
        submission.setTotalPoints(processed.getTotalPoints());
        submission.setTestExecutions(newTestExecutions);
        submissionRepository.save(submission);

        attempt.setSubmitted(true);
        cache.put(key, attempt);
        cache.remove(key);

        return SubmissionService.mapToDTO(submission, new SubmissionDTO());
    }

    private RubricGrade mapRubricGradeToEntity(final RubricGradeDTO rubricGradeDTO, final RubricGrade rubricGrade) {
        if (rubricGradeDTO.getId() != null) {
            rubricGrade.setId(UUID.fromString(rubricGradeDTO.getId()));
        }
        if (rubricGradeDTO.getName() != null) {
            rubricGrade.setName(rubricGradeDTO.getName());
        }

        if (rubricGradeDTO.getGradeType() != null) {
            rubricGrade.setGradeType(rubricGradeDTO.getGradeType());
        }
        if (rubricGradeDTO.getRubricId() != null) {
            final Rubric rubric = rubricRepository.findById(rubricGradeDTO.getRubricId())
                    .orElseThrow(() -> new EntityNotFoundException("Rubric not found"));
            rubricGrade.setRubric(rubric);
        }
        if (rubricGradeDTO.getAssignmentId() != null) {
            final Assignment assignment = assignmentRepository.findById(rubricGradeDTO.getAssignmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));
            rubricGrade.setAssignment(assignment);
        }

        return rubricGrade;
    }
}



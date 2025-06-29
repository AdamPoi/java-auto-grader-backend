package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.domain.Assignment;
import io.adampoi.java_auto_grader.domain.Submission;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.model.dto.SubmissionCodeDTO;
import io.adampoi.java_auto_grader.model.dto.TimedAssessmentAttempt;
import io.adampoi.java_auto_grader.repository.AssignmentRepository;
import io.adampoi.java_auto_grader.repository.SubmissionRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.stereotype.Service;

import javax.cache.Cache;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TimedAssessmentService {
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final javax.cache.CacheManager jcacheManager;

    public TimedAssessmentService(
            SubmissionRepository submissionRepository,
            AssignmentRepository assignmentRepository,
            UserRepository userRepository,
            CacheManager springCacheManager
    ) {
        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;

        if (springCacheManager instanceof JCacheCacheManager) {
            this.jcacheManager = ((JCacheCacheManager) springCacheManager).getCacheManager();
        } else {
            throw new IllegalStateException("Expected a JCacheCacheManager!");
        }
    }

    // Helper to get the authoritative time limit (in ms)
    private long getAssignmentTimeLimitMs(Assignment assignment) {
        if (assignment.getOptions() != null && assignment.getOptions().getTimeLimit() != null) {
            return assignment.getOptions().getTimeLimit() * 1000L; // seconds to ms
        } else {
            assert assignment.getOptions() != null;
            return 3600_000L; // 1 hour in ms
        }
    }

    // Each assignment/exam gets its own cache with custom TTL (timeLimit + 2 hours)
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
                // If already exists (race condition), just get it
                cache = jcacheManager.getCache(cacheName, String.class, TimedAssessmentAttempt.class);
            }
            // (Optional) Log cache creation and TTL
            long ttlSeconds = ttlMs / 1000;
            long ttlMinutes = ttlSeconds / 60;
            long ttlHours = ttlMinutes / 60;
            log.info("Using cache {} with TTL: {} ms ({} hours, {} minutes, {} seconds)",
                    cacheName, ttlMs, ttlHours, ttlMinutes % 60, ttlSeconds % 60);
        }
        return cache;
    }

    public TimedAssessmentAttempt start(UUID assignmentId, UUID studentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        long baseTimeLimitMs = getAssignmentTimeLimitMs(assignment);
        long ttlMs = baseTimeLimitMs + (2 * 60 * 60 * 1000L); // +2 hours
        Cache<String, TimedAssessmentAttempt> cache = getOrCreateExamCache(assignmentId, ttlMs);

        String key = studentId + ":" + assignmentId;
        TimedAssessmentAttempt attempt = cache.get(key);

        if (attempt != null && attempt.isSubmitted()) {
            throw new IllegalStateException("Already submitted");
        }
        if (attempt == null) {
            attempt = new TimedAssessmentAttempt();
            attempt.setStudentId(studentId);
            attempt.setAssignmentId(assignmentId);
            attempt.setStartedAt(OffsetDateTime.now());
            attempt.setSubmitted(false);
            cache.put(key, attempt);
        }
        return attempt;
    }

    public Map<String, Object> getStatus(UUID assignmentId, UUID studentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        long baseTimeLimitMs = getAssignmentTimeLimitMs(assignment);
        long ttlMs = baseTimeLimitMs + (2 * 60 * 60 * 1000L);
        Cache<String, TimedAssessmentAttempt> cache = getOrCreateExamCache(assignmentId, ttlMs);

        String key = studentId + ":" + assignmentId;
        TimedAssessmentAttempt attempt = cache.get(key);
        if (attempt == null) throw new IllegalStateException("Not started");

        long elapsedMs = java.time.Duration.between(attempt.getStartedAt(), OffsetDateTime.now()).toMillis();
        long remainingMs = Math.max(0, baseTimeLimitMs - elapsedMs);

        return Map.of(
                "startedAt", attempt.getStartedAt(),
                "remainingMs", remainingMs,
                "expired", remainingMs == 0,
                "submitted", attempt.isSubmitted()
        );
    }

    public Submission submit(UUID assignmentId, UUID studentId, SubmissionCodeDTO codeDto) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        User student = userRepository.findById(studentId).orElseThrow();

        long baseTimeLimitMs = getAssignmentTimeLimitMs(assignment);
        long ttlMs = baseTimeLimitMs + (2 * 60 * 60 * 1000L);
        Cache<String, TimedAssessmentAttempt> cache = getOrCreateExamCache(assignmentId, ttlMs);

        String key = studentId + ":" + assignmentId;
        TimedAssessmentAttempt attempt = cache.get(key);

        if (attempt == null) throw new IllegalStateException("Not started");
        if (attempt.isSubmitted()) throw new IllegalStateException("Already submitted");

        long elapsedMs = java.time.Duration.between(attempt.getStartedAt(), OffsetDateTime.now()).toMillis();
        if (elapsedMs > baseTimeLimitMs) {
            attempt.setSubmitted(true);
            cache.put(key, attempt);
            throw new IllegalStateException("Time expired");
        }

        Submission submission = Submission.builder()
                .assignment(assignment)
                .student(student)
                .status("SUBMITTED")
                .startedAt(attempt.getStartedAt())
                .completedAt(OffsetDateTime.now())
                .executionTime(elapsedMs)
                .totalPoints(0)
                .feedback(null)
                .build();

        submissionRepository.save(submission);

        attempt.setSubmitted(true);
        cache.put(key, attempt);

        return submission;
    }
}

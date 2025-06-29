package io.adampoi.java_auto_grader.cache;


import io.adampoi.java_auto_grader.model.dto.TimedAssessmentAttempt;
import org.ehcache.expiry.ExpiryPolicy;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Per-entry expiry policy for timed assessments.
 */
public class AssignmentExpiryPolicy implements ExpiryPolicy<String, TimedAssessmentAttempt> {

    // Map key -> custom TTL (in ms)
    private final Map<String, Long> keyToCustomTtlMs = new ConcurrentHashMap<>();

    /**
     * Set the TTL for the cache key (assignment timeLimit + 2 hours)
     */
    public void putCustomTtl(String key, long assignmentTimeLimitMs) {
        long ttlMs = assignmentTimeLimitMs + (2 * 60 * 60 * 1000L); // add 2 hours in ms
        keyToCustomTtlMs.put(key, ttlMs);
    }

    @Override
    public Duration getExpiryForCreation(String key, TimedAssessmentAttempt value) {
        Long ttlMs = keyToCustomTtlMs.getOrDefault(key, 4 * 60 * 60 * 1000L); // fallback 4h
        return Duration.ofMillis(ttlMs);
    }

    @Override
    public Duration getExpiryForAccess(String key, Supplier<? extends TimedAssessmentAttempt> value) {
        return null; // don't reset on access
    }

    @Override
    public Duration getExpiryForUpdate(String key, Supplier<? extends TimedAssessmentAttempt> oldValue, TimedAssessmentAttempt newValue) {
        return null; // don't reset on update
    }
}

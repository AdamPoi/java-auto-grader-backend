package io.adampoi.java_auto_grader.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "grade_executions")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class GradeExecution {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal pointsAwarded;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal maxPoints;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ExecutionStatus status;
    @Column(columnDefinition = "TEXT")
    private String actual;
    @Column(columnDefinition = "TEXT")
    private String expected;
    @Column(columnDefinition = "TEXT")
    private String error;
    @Column
    private Long executionTime = 0L; //ms
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rubric_grade_id", referencedColumnName = "id", nullable = false)
    private RubricGrade rubricGrade;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", referencedColumnName = "id", nullable = false)
    private Submission submission;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    @Column(nullable = false, updatable = true)
    private OffsetDateTime updatedAt;

    public double getScorePercentage() {
        if (maxPoints.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return pointsAwarded.divide(maxPoints, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    public enum ExecutionStatus {
        PENDING,
        RUNNING,
        PASSED,
        FAILED,
        ERROR,
        TIMEOUT,
        SKIPPED
    }
}
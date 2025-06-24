package io.adampoi.java_auto_grader.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@Table(name = "test_executions")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class TestExecution extends Auditable {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    private String methodName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ExecutionStatus status;

    @Column(columnDefinition = "TEXT")
    private String output;

    @Column(columnDefinition = "TEXT")
    private String error;

    @Column
    private Long executionTime = 0L; //ms

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rubric_grade_id", referencedColumnName = "id", nullable = false)
    private RubricGrade rubricGrade;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "submission_id", referencedColumnName = "id", nullable = false)
    private Submission submission;

    public enum ExecutionStatus {
        PENDING,
        RUNNING,
        PASSED,
        FAILED,
        TIMEOUT,
        SKIPPED
    }
}
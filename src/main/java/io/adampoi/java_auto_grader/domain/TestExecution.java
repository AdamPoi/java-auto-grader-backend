package io.adampoi.java_auto_grader.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@Table(name = "test_executions")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestExecution extends Auditable {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    private String methodName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 50)
    private ExecutionStatus status;

    @Lob
    private String error;

    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long executionTime; //ms

    @Lob
    private String output;

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
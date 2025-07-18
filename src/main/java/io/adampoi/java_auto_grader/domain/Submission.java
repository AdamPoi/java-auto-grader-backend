package io.adampoi.java_auto_grader.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "submissions")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true, exclude = {"testExecutions", "submissionCodes"})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Submission extends Auditable {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionType type;

    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long executionTime; //ms

    @Lob
    private String manualFeedback;

    @Lob
    private String aiFeedback;

    @Column
    private OffsetDateTime startedAt;

    @Column
    private OffsetDateTime completedAt;

    private Integer totalPoints;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignment_id", referencedColumnName = "id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", referencedColumnName = "id", nullable = true)
    private User student;

    @OneToMany(mappedBy = "submission", fetch = FetchType.LAZY)
    private Set<TestExecution> testExecutions;

    @OneToMany(mappedBy = "submission", fetch = FetchType.LAZY)
    private Set<SubmissionCode> submissionCodes;


    public enum SubmissionType {
        TRYOUT,    // practice or test run
        ATTEMPT,   // a graded attempt (within max attempts)
        FINAL      // the final submission (last or locked-in)
    }

    public enum SubmissionStatus {
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        TIMEOUT
    }
}
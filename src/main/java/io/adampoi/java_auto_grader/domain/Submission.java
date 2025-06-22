package io.adampoi.java_auto_grader.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "submissions")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Submission extends Auditable {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column
    private String status;

    @Column
    private Long executionTime = 0L; //ms

    @Column(columnDefinition = "text")
    private String feedback;

    @Column
    private OffsetDateTime startedAt;

    @Column
    private OffsetDateTime completedAt;

    @Column
    private String mainClassName;

    @Column(precision = 5, scale = 2)
    private BigDecimal totalPoints = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignment_id", referencedColumnName = "id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", referencedColumnName = "id", nullable = false)
    private User student;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<GradeExecution> gradeExecutions;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<SubmissionCode> submissionCodes;

}
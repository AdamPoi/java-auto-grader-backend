package io.adampoi.java_auto_grader.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "submissions")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Submission {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column
    private OffsetDateTime submissionTime;

    @Column(nullable = false)
    private Integer attemptNumber;

    @Column
    private String status;

    @Column(columnDefinition = "text")
    private String graderFeedback;

    @Column
    private OffsetDateTime gradingStartedAt;

    @Column
    private OffsetDateTime gradingCompletedAt;

    @Column
    private String mainClassName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", referencedColumnName = "id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "id", nullable = false)
    private User student;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<GradeExecution> gradeExecutions;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<SubmissionCode> submissionCodes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false, updatable = true)
    private OffsetDateTime updatedAt;

}
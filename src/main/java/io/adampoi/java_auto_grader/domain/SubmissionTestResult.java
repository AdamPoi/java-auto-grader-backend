package io.adampoi.java_auto_grader.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;


@Entity
@Table(name = "submission_test_results")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class SubmissionTestResult {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private Boolean passed;

    @Column
    private Integer score;

    @Column(columnDefinition = "text")
    private String actualOutput;

    @Column(columnDefinition = "text")
    private String errorMessage;

    @Column
    private Integer executionTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id",referencedColumnName = "id", nullable = false)
    private Submission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_case_id",referencedColumnName = "id", nullable = false)
    private TestCase testCase;

    @CreationTimestamp
    @Column
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column
    private OffsetDateTime updatedAt;

}

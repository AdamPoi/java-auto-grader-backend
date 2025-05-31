package io.adampoi.java_auto_grader.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "test_cases")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class TestCase {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String testCaseName;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private String testCaseType;

    @Column(nullable = false, columnDefinition = "text")
    private String testCaseDetails;

    @Column(nullable = false)
    private Integer score;

    @Column
    private Integer executionOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id",referencedColumnName = "id", nullable = false)
    private Assignment assignment;

    @OneToMany(mappedBy = "testCase")
    private Set<SubmissionTestResult> testCaseSubmissionTestResults;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime lastUpdated;

}

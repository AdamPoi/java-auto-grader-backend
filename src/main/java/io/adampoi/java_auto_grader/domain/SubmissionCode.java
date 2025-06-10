package io.adampoi.java_auto_grader.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "submission_codes")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class SubmissionCode {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, columnDefinition = "text")
    private String sourceCode;

    @Column
    private String packagePath;

    @Column
    private String className;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", referencedColumnName = "id", nullable = false)
    private Submission submission;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false, updatable = true)
    private OffsetDateTime updatedAt;

}
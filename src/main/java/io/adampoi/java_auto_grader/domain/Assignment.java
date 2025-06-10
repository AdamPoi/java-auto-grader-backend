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
@Table(name = "assignments")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Assignment {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column
    private OffsetDateTime dueDate;

    @Column
    private Boolean isPublished;

    @Column(length = 512)
    private String starterCodeBasePath;

    @Column(length = 512)
    private String solutionCodeBasePath;

    @Column
    private Integer maxAttempts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", referencedColumnName = "id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_teacher_id", referencedColumnName = "id")
    private User createdByTeacher;


    @OneToMany(mappedBy = "assignment")
    private Set<Submission> assignmentSubmissions;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false, updatable = true)
    private OffsetDateTime updatedAt;

}

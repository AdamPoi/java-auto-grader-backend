package io.adampoi.java_auto_grader.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "assignments")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Assignment extends Auditable {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String resource;

    @Column
    private OffsetDateTime dueDate;

    @Column
    private Boolean isPublished;

    @Column(columnDefinition = "TEXT")
    private String starterCode;

    @Column(columnDefinition = "TEXT")
    private String solutionCode;

    @Column(columnDefinition = "TEXT")
    private String testCode;

    @Column
    private Long timeLimit; // in ms

    @Column(precision = 5, scale = 2)
    private BigDecimal totalPoints = BigDecimal.ZERO;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", referencedColumnName = "id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_teacher_id", referencedColumnName = "id")
    private User createdByTeacher;

    @OneToMany(mappedBy = "assignment", fetch = FetchType.EAGER)
    private Set<Rubric> rubrics = new HashSet<>();


    @OneToMany(mappedBy = "assignment", fetch = FetchType.EAGER)
    private Set<RubricGrade> rubricGrades;

    @OneToMany(mappedBy = "assignment")
    private Set<Submission> assignmentSubmissions;


}

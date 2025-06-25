package io.adampoi.java_auto_grader.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "assignments")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true, exclude = {"rubrics", "rubricGrades", "assignmentSubmissions"})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Assignment extends Auditable {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Lob
    private String resource;

    @Column
    private OffsetDateTime dueDate;

    @Column
    private Boolean isPublished;

    @Lob
    private String starterCode;

    @Lob
    private String solutionCode;

    @Lob
    private String testCode;

    @Column
    private Long timeLimit; // in ms

    private int totalPoints;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", referencedColumnName = "id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_teacher_id", referencedColumnName = "id")
    private User createdByTeacher;

    @OneToMany(mappedBy = "assignment", fetch = FetchType.EAGER)
    private Set<Rubric> rubrics = new HashSet<>();


    @OneToMany(mappedBy = "assignment", fetch = FetchType.EAGER)
    private Set<RubricGrade> rubricGrades;

    @OneToMany(mappedBy = "assignment")
    private Set<Submission> assignmentSubmissions;


}

package io.adampoi.java_auto_grader.domain;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "rubric_grades")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true, exclude = {"testExecutions"})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RubricGrade extends Auditable {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private GradeType gradeType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rubric_id", referencedColumnName = "id", nullable = true)
    private Rubric rubric;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignment_id", referencedColumnName = "id", nullable = false)
    private Assignment assignment;

    @OneToMany(mappedBy = "rubricGrade", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TestExecution> testExecutions = new HashSet<>();

    @PrePersist
    private void ensureId() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }


    public enum GradeType {
        AUTOMATIC,
        COMPILATION,
        ERROR_HANDLING,
        FUNCTIONALITY,
        INPUT_OUTPUT,
        DATA_TYPE,
        REGEX_MATCH,
        FULL_MATCH,
        SUBSTRING_MATCH,
        TIMEOUT,
        FILE_TESTING,
        SCRIPT_TESTING,
        CODE_STYLE,
        CODE_STRUCTURE,
        MANUAL_GRADING,
        OTHER
    }
}
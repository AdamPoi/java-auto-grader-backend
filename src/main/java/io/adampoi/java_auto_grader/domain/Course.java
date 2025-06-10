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
@Table(name = "courses")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Course {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 20, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column
    private Boolean isActive;

    @Column
    private OffsetDateTime enrollmentStartDate;

    @Column
    private OffsetDateTime enrollmentEndDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_teacher_id", referencedColumnName = "id")
    private User createdByTeacher;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Classroom> courseClassrooms;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Assignment> courseAssignments;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false, updatable = true)
    private OffsetDateTime updatedAt;

}

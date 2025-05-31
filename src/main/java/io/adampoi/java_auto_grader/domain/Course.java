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

    @Column(nullable = false, length = 20)
    private String courseCode;

    @Column(nullable = false)
    private String courseName;

    @Column(columnDefinition = "text")
    private String description;

    @Column
    private Boolean isActive;

    @Column
    private OffsetDateTime enrollmentStartDate;

    @Column
    private OffsetDateTime enrollmentEndDate;

    @Column
    private OffsetDateTime createdAt;

    @Column
    private OffsetDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_teacher_id",referencedColumnName = "id")
    private User createdByTeacher;

    @OneToMany(mappedBy = "course")
    private Set<Classroom> courseClassrooms;

    @OneToMany(mappedBy = "course")
    private Set<TeacherCourse> courseTeacherCourses;

    @OneToMany(mappedBy = "course")
    private Set<Assignment> courseAssignments;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime lastUpdated;

}

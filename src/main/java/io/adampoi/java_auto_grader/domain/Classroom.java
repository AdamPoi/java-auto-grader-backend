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
@Table(name = "classrooms")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Classroom {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column
    private Boolean isActive;

    @Column
    private OffsetDateTime enrollmentStartDate;

    @Column
    private OffsetDateTime enrollmentEndDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id",referencedColumnName = "id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id",referencedColumnName = "id")
    private User teacher;

    @OneToMany(mappedBy = "classroom")
    private Set<StudentClassroom> classroomStudentClassrooms;

    @OneToMany(mappedBy = "classroom")
    private Set<Submission> classroomSubmissions;

    @CreationTimestamp
    @Column
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column
    private OffsetDateTime updatedAt;

}

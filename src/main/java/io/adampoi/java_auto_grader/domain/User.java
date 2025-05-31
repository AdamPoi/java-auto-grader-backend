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
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class User {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 50)
    private String firstName;

    @Column(length = 50)
    private String lastName;

    @Column
    private Boolean isActive;

    @Column
    private OffsetDateTime createdAt;

    @Column
    private OffsetDateTime updatedAt;

    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> userRoleRoles;

    @OneToMany(mappedBy = "createdByTeacher")
    private Set<Course> createdByTeacherCourses;

    @OneToMany(mappedBy = "teacher")
    private Set<Classroom> teacherClassrooms;

    @OneToMany(mappedBy = "teacher")
    private Set<TeacherCourse> teacherTeacherCourses;

    @OneToMany(mappedBy = "student")
    private Set<StudentClassroom> studentStudentClassrooms;

    @OneToMany(mappedBy = "createdByTeacher")
    private Set<Assignment> createdByTeacherAssignments;

    @OneToMany(mappedBy = "student")
    private Set<Submission> studentSubmissions;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime lastUpdated;

}

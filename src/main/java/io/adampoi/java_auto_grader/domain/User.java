package io.adampoi.java_auto_grader.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class User implements UserDetails {

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

    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> userRoles;

    @OneToMany(mappedBy = "createdByTeacher")
    private Set<Course> teacherCourses;

    @OneToMany(mappedBy = "teacher")
    private Set<Classroom> teacherClassrooms;

    @OneToMany(mappedBy = "teacher")
    private Set<TeacherCourse> teacherTeacherCourses;

    @OneToMany(mappedBy = "student")
    private Set<StudentClassroom> studentStudentClassrooms;

    @OneToMany(mappedBy = "createdByTeacher")
    private Set<Assignment> teacherAssignments;

    @OneToMany(mappedBy = "student")
    private Set<Submission> studentSubmissions;

    @CreationTimestamp
    @Column
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column
    private OffsetDateTime updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<String> roles = getUserRoles().stream()
                .map(role -> role.getName())
                .toList();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_"+ roles.get(0));

        return List.of(authority);
    }

    @Override
    public String getUsername() {
        return email;
    }
}

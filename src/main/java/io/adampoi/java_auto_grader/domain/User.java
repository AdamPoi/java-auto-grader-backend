package io.adampoi.java_auto_grader.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;


@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class User extends Auditable implements UserDetails {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 50)
    private String firstName;

    @Column(length = 50)
    private String lastName;

    @Column
    private Boolean isActive;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> userRoles;

    @OneToMany(mappedBy = "createdByTeacher", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Course> teacherCourses;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Classroom> teacherClassrooms;

    @OneToMany(mappedBy = "createdByTeacher", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Assignment> teacherAssignments;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Submission> studentSubmissions;

    @ManyToMany(mappedBy = "enrolledUsers", fetch = FetchType.EAGER)
    private Set<Course> enrolledCourses;

    @ManyToMany(mappedBy = "enrolledStudents", cascade = CascadeType.ALL)
    private Set<Classroom> enrolledClassrooms;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (userRoles != null && !userRoles.isEmpty()) {
            for (Role role : userRoles) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

                if (role.getRolePermissions() != null) {
                    for (Permission permission : role.getRolePermissions()) {
                        authorities.add(new SimpleGrantedAuthority(permission.getName()));
                    }
                }
            }
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_STUDENT"));
        }
        return authorities;
    }


    @Override
    public String getUsername() {
        return email;
    }

    public boolean hasRole(String roleName) {
        return userRoles.stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(roleName));
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isSuperAdmin() {
        return hasRole("SUPERADMIN");
    }

    public boolean isTeacher() {
        return hasRole("TEACHER");
    }

    public boolean isStudent() {
        return hasRole("STUDENT");
    }

    public Set<String> getRoleNames() {
        return userRoles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

}



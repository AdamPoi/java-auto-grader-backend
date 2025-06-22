package io.adampoi.java_auto_grader.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "permissions")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Permission extends Auditable {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @ManyToMany(mappedBy = "rolePermissions", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Role> permissionRoles;

}

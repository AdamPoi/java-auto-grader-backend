package io.adampoi.java_auto_grader.repository;

import io.adampoi.java_auto_grader.domain.Permission;
import io.adampoi.java_auto_grader.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface RoleRepository extends JpaRepository<Role, UUID> {

    Role findFirstByRolePermissions(Permission permission);

    List<Role> findAllByRolePermissions(Permission permission);

    boolean existsByName(String name);

    List<Role> findByName(String name);
}

package io.adampoi.java_auto_grader.repository;

import io.adampoi.java_auto_grader.domain.Permission;
import io.adampoi.java_auto_grader.domain.Role;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface RoleRepository extends JpaRepository<Role, UUID>, JpaSpecificationExecutor<Role> {

    Role findFirstByRolePermissions(Permission permission);

    List<Role> findAllByRolePermissions(Permission permission);


    boolean existsByName(String name);

    Optional<Role> findById(UUID id);

    Optional<Role> findByName(String name);

    List<Role> findAllByName(String name, Sort sort);
}

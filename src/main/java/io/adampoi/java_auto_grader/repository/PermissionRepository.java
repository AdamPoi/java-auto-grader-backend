package io.adampoi.java_auto_grader.repository;

import io.adampoi.java_auto_grader.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;


public interface PermissionRepository extends JpaRepository<Permission, UUID>, JpaSpecificationExecutor<Permission> {
    boolean existsByName(String name);

    Set<Permission> findByNameIn(Collection<String> names);
}

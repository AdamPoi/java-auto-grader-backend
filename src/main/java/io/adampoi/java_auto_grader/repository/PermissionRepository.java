package io.adampoi.java_auto_grader.repository;

import io.adampoi.java_auto_grader.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface PermissionRepository extends JpaRepository<Permission, UUID> {
}

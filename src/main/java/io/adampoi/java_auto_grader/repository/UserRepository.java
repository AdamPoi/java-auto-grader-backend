package io.adampoi.java_auto_grader.repository;

import io.adampoi.java_auto_grader.domain.Role;
import io.adampoi.java_auto_grader.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface UserRepository extends JpaRepository<User, UUID> {

    User findFirstByUserRoleRoles(Role role);

    List<User> findAllByUserRoleRoles(Role role);

    Optional<User> findByEmail(String email);
}

package io.adampoi.java_auto_grader.repository;

import io.adampoi.java_auto_grader.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;


public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {


    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> getUserByEmail(String email);
}

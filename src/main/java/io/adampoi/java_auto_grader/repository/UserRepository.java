package io.adampoi.java_auto_grader.repository;

import io.adampoi.java_auto_grader.domain.Role;
import io.adampoi.java_auto_grader.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;


public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);


    Optional<User> getUserByEmail(String email);

    List<User> findByUserRolesContaining(Set<Role> userRoles);

    Optional<User> findByNim(String nim);

    @Query("SELECT COUNT(u) FROM User u JOIN u.userRoles r WHERE UPPER(r.name) = UPPER(:roleName)")
    Long countUsersByRoleName(@Param("roleName") String roleName);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.enrolledCourses c LEFT JOIN FETCH c.courseAssignments WHERE u.id = :id")
    Optional<User> findByIdWithCoursesAndAssignments(@Param("id") UUID id);
}

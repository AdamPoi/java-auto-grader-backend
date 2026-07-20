package io.adampoi.java_auto_grader.repository;

import io.adampoi.java_auto_grader.domain.Rubric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface RubricRepository extends JpaRepository<Rubric, UUID>, JpaSpecificationExecutor<Rubric> {
    List<Rubric> findByAssignmentId(UUID assignmentId);
}

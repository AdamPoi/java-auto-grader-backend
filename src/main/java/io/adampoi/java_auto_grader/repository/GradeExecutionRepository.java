package io.adampoi.java_auto_grader.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.adampoi.java_auto_grader.domain.GradeExecution;

public interface GradeExecutionRepository
        extends JpaRepository<GradeExecution, UUID>, JpaSpecificationExecutor<GradeExecution> {

}

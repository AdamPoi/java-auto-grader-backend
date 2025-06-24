package io.adampoi.java_auto_grader.repository;

import io.adampoi.java_auto_grader.domain.TestExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface TestExecutionRepository
        extends JpaRepository<TestExecution, UUID>, JpaSpecificationExecutor<TestExecution> {

}

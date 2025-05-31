package io.adampoi.java_auto_grader.repository;

import io.adampoi.java_auto_grader.domain.Assignment;
import io.adampoi.java_auto_grader.domain.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface TestCaseRepository extends JpaRepository<TestCase, UUID> {

    TestCase findFirstByAssignment(Assignment assignment);

}

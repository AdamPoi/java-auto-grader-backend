package io.adampoi.java_auto_grader.repository;

import io.adampoi.java_auto_grader.domain.RubricGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface RubricGradeRepository extends JpaRepository<RubricGrade, UUID>, JpaSpecificationExecutor<RubricGrade> {

}

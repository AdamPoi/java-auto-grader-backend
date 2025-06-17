package io.adampoi.java_auto_grader.repository;

import io.adampoi.java_auto_grader.domain.Assignment;
import io.adampoi.java_auto_grader.domain.Course;
import io.adampoi.java_auto_grader.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface AssignmentRepository extends JpaRepository<Assignment, UUID>, JpaSpecificationExecutor<Assignment> {

    Assignment findFirstByCourse(Course course);

    Assignment findFirstByCreatedByTeacher(User createdByTeacher);

    List<Assignment> findByCourse(Course course);

    Page<Assignment> findByCourse(Course course, Pageable pageable);

}

package io.adampoi.java_auto_grader.repository;

import io.adampoi.java_auto_grader.domain.Assignment;
import io.adampoi.java_auto_grader.domain.Course;
import io.adampoi.java_auto_grader.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AssignmentRepository extends JpaRepository<Assignment, UUID>, JpaSpecificationExecutor<Assignment> {

    Assignment findFirstByCourse(Course course);

    Assignment findFirstByCreatedByTeacher(User createdByTeacher);


    List<Assignment> findAllByCourseIn(List<Course> courses);

    List<Assignment> findTop5ByCourseInAndDueDateAfterOrderByDueDateAsc(List<Course> courses, OffsetDateTime now);

    List<Assignment> findTop5ByDueDateAfterOrderByDueDateAsc(OffsetDateTime now);

    long countByCourseIn(Collection<Course> courses);

}

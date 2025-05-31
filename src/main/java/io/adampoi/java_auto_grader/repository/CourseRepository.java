package io.adampoi.java_auto_grader.repository;

import io.adampoi.java_auto_grader.domain.Course;
import io.adampoi.java_auto_grader.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface CourseRepository extends JpaRepository<Course, UUID> {

    Course findFirstByCreatedByTeacher(User user);

}

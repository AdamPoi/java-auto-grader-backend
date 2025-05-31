package io.adampoi.java_auto_grader.repository;

import io.adampoi.java_auto_grader.domain.Course;
import io.adampoi.java_auto_grader.domain.TeacherCourse;
import io.adampoi.java_auto_grader.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TeacherCourseRepository extends JpaRepository<TeacherCourse, Long> {

    TeacherCourse findFirstByTeacher(User user);

    TeacherCourse findFirstByCourse(Course course);

}

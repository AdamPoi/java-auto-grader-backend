package io.adampoi.java_auto_grader.repository;

import io.adampoi.java_auto_grader.domain.Classroom;
import io.adampoi.java_auto_grader.domain.StudentClassroom;
import io.adampoi.java_auto_grader.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface StudentClassroomRepository extends JpaRepository<StudentClassroom, Long> {

    StudentClassroom findFirstByStudent(User user);

    StudentClassroom findFirstByClassroom(Classroom classroom);

}

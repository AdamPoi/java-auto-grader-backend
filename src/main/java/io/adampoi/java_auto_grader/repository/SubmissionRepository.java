package io.adampoi.java_auto_grader.repository;

import io.adampoi.java_auto_grader.domain.Assignment;
import io.adampoi.java_auto_grader.domain.Classroom;
import io.adampoi.java_auto_grader.domain.Submission;
import io.adampoi.java_auto_grader.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    Submission findFirstByAssignment(Assignment assignment);

    Submission findFirstByStudent(User user);

    Submission findFirstByClassroom(Classroom classroom);

}

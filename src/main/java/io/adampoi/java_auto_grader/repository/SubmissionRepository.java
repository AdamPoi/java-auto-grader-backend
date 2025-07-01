package io.adampoi.java_auto_grader.repository;

import io.adampoi.java_auto_grader.domain.Assignment;
import io.adampoi.java_auto_grader.domain.Course;
import io.adampoi.java_auto_grader.domain.Submission;
import io.adampoi.java_auto_grader.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface SubmissionRepository extends JpaRepository<Submission, UUID>, JpaSpecificationExecutor<Submission> {

    Submission findFirstByAssignment(Assignment assignment);

    Submission findFirstByStudent(User user);

    Optional<Submission> findByAssignmentAndStudentAndType(Assignment assignment, User student, Submission.SubmissionType type);

    long countByAssignmentAndStudentAndType(Assignment assignment, User student, Submission.SubmissionType type);

    List<Submission> findAllByStudentAndType(User student, Submission.SubmissionType type);

    List<Submission> findTop5ByTypeAndAssignment_CourseInOrderByCompletedAtDesc(Submission.SubmissionType type, List<Course> courses);

    List<Submission> findTop5ByTypeOrderByCompletedAtDesc(Submission.SubmissionType type);

    List<Submission> findTop50ByTypeOrderByCompletedAtDesc(Submission.SubmissionType type);
}

package io.adampoi.java_auto_grader.repository;

import io.adampoi.java_auto_grader.domain.Submission;
import io.adampoi.java_auto_grader.domain.SubmissionFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface SubmissionFileRepository extends JpaRepository<SubmissionFile, UUID> {

    SubmissionFile findFirstBySubmission(Submission submission);

}

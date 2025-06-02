package io.adampoi.java_auto_grader.repository;

import io.adampoi.java_auto_grader.domain.SubmissionTestResult;
import io.adampoi.java_auto_grader.domain.Submission;
import io.adampoi.java_auto_grader.domain.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;


public interface SubmissionTestResultRepository extends JpaRepository<SubmissionTestResult, UUID>, JpaSpecificationExecutor<SubmissionTestResult> {

    SubmissionTestResult findFirstBySubmission(Submission submission);

    SubmissionTestResult findFirstByTestCase(TestCase testCase);

}

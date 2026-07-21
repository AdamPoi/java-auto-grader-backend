package io.adampoi.java_auto_grader.service;


import io.adampoi.java_auto_grader.domain.*;
import io.adampoi.java_auto_grader.model.dto.RubricGradeDTO;
import io.adampoi.java_auto_grader.model.dto.SubmissionDTO;
import io.adampoi.java_auto_grader.model.dto.TestExecutionDTO;
import io.adampoi.java_auto_grader.model.request.TestCodeRequest;
import io.adampoi.java_auto_grader.model.request.TestSubmitRequest;
import io.adampoi.java_auto_grader.model.response.TestCodeResponse;
import io.adampoi.java_auto_grader.model.type.CodeFile;
import io.adampoi.java_auto_grader.model.type.TestCaseResult;
import io.adampoi.java_auto_grader.repository.AssignmentRepository;
import io.adampoi.java_auto_grader.repository.RubricGradeRepository;
import io.adampoi.java_auto_grader.repository.SubmissionRepository;
import io.adampoi.java_auto_grader.repository.TestExecutionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class TestExecutionService {

    private final TestCodeService testCodeService;
    private final RubricGradeRepository rubricGradeRepository;
    private final TestExecutionRepository testExecutionRepository;
    private final SubmissionService submissionService;
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;

    public static TestExecutionDTO mapToDTO(TestExecution testExecution, final TestExecutionDTO testExecutionDTO) {
        testExecutionDTO.setId(testExecution.getId());
        testExecutionDTO.setMethodName(testExecution.getMethodName());
        testExecutionDTO.setOutput(testExecution.getOutput());
        testExecutionDTO.setError(testExecution.getError());
        testExecutionDTO.setExecutionTime(testExecution.getExecutionTime());
        testExecutionDTO.setStatus(String.valueOf(testExecution.getStatus()));

        if (testExecution.getRubricGrade() != null) {
//        testExecutionDTO.setAssignmentId(testExecution.getRubricGrade().getAssignmentId());
            testExecutionDTO.setRubricGradeId(testExecution.getRubricGrade().getId());
            RubricGradeDTO rubricGradeDTO = RubricGradeService.mapToDTO(testExecution.getRubricGrade(), new RubricGradeDTO());
            testExecutionDTO.setRubricGrade(rubricGradeDTO);
        }

        return testExecutionDTO;
    }

    public SubmissionDTO runTest(TestSubmitRequest request) {

        TestCodeRequest testCodeRequest = TestCodeRequest.builder()
                .sourceFiles(request.getSourceFiles())
                .testFiles(request.getTestFiles())
                .mainClassName("Main.java")
                .buildTool(request.getBuildTool())
                .mutationTestingEnabled(request.isMutationTestingEnabled())
                .build();
        TestCodeResponse testCodeResponse = testCodeService.runTestCode(testCodeRequest);
        boolean compilationFailed = hasCompilationErrors(testCodeResponse);
        boolean hasExecutedTestCases = hasExecutedTestCases(testCodeResponse);

        UUID assignmentId = UUID.fromString(request.getAssignmentId());
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found: " + assignmentId));
        List<RubricGrade> rubricGrades = rubricGradeRepository.findByAssignmentId(assignmentId);
        List<TestExecution> executionResults = new ArrayList<>();
        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStartedAt(OffsetDateTime.now());
        submission.setCompletedAt(OffsetDateTime.now());
        submission.setExecutionTime(testCodeResponse.getExecutionTime());
        submission.setManualFeedback("Test execution completed successfully");

        List<SubmissionCode> submissionCodes = new ArrayList<>();

        for (CodeFile sourceFile : request.getSourceFiles()) {
            SubmissionCode submissionCode = new SubmissionCode();
            submissionCode.setFileName(sourceFile.getFileName());
            submissionCode.setSourceCode(sourceFile.getContent());
            submissionCode.setSubmission(submission);
            submissionCodes.add(submissionCode);
        }

        submission.setSubmissionCodes(new HashSet<>(submissionCodes));

        int earnedPoints = 0;
        for (RubricGrade rubricGrade : rubricGrades) {
            TestExecution testExecution = new TestExecution();
            testExecution.setRubricGrade(rubricGrade);
            testExecution.setSubmission(submission);

            if (compilationFailed) {
                markNotExecuted(testExecution,
                        "Rubric test was not executed because compilation failed during "
                                + compilationStage(testCodeResponse));
            } else if (!hasExecutedTestCases) {
                markNotExecuted(testExecution, "Rubric test did not produce an executable test result");
            } else {
                TestCaseResult matchingTestCase = findMatchingTestCase(testCodeResponse, rubricGrade.getName());
                if (matchingTestCase == null) {
                    markNotExecuted(testExecution,
                            "No executed test case matched rubric grade: " + rubricGrade.getName());
                    executionResults.add(testExecution);
                    continue;
                }

                testExecution.setExecutionTime((long) matchingTestCase.getExecutionTime());
                testExecution.setMethodName(matchingTestCase.getMethodName());
                testExecution.setOutput(testCodeResponse.getError());
                testExecution.setError(matchingTestCase.getFailureMessage());
                testExecution.setStatus(TestExecution.ExecutionStatus.valueOf(matchingTestCase.getStatus()));
                if (testExecution.getStatus() == TestExecution.ExecutionStatus.PASSED
                        && rubricGrade.getRubric() != null) {
                    earnedPoints += rubricGrade.getRubric().getPoints();
                }
            }
            executionResults.add(testExecution);
        }

        submission.setTotalPoints(earnedPoints);
        submission.setStatus(testCodeResponse.isSuccess() ? Submission.SubmissionStatus.COMPLETED : Submission.SubmissionStatus.FAILED);
        submission.setTestExecutions(new HashSet<>(executionResults));
        Submission finalSubmission = submission;
        if (compilationFailed) {
            submission.setStatus(Submission.SubmissionStatus.FAILED);
            submission.setManualFeedback("Compilation failed; rubric tests were not executed.");
        } else if (!hasExecutedTestCases) {
            submission.setStatus(Submission.SubmissionStatus.FAILED);
            submission.setManualFeedback("No test cases were executed.");
            finalSubmission = submissionRepository.save(submission);
        } else {
            submission.setManualFeedback("All tests executed successfully.");
            finalSubmission = submissionRepository.save(submission);
        }

        return SubmissionService.mapToDTO(finalSubmission, new SubmissionDTO());
    }

    private boolean hasCompilationErrors(TestCodeResponse response) {
        return response != null
                && response.getCompilationErrors() != null
                && !response.getCompilationErrors().isEmpty();
    }

    private boolean hasExecutedTestCases(TestCodeResponse response) {
        return response != null
                && response.getTestSuites() != null
                && response.getTestSuites().stream()
                .filter(java.util.Objects::nonNull)
                .map(suite -> suite.getTestCases())
                .filter(java.util.Objects::nonNull)
                .anyMatch(testCases -> !testCases.isEmpty());
    }

    private TestCaseResult findMatchingTestCase(TestCodeResponse response, String rubricName) {
        return response.getTestSuites().stream()
                .filter(java.util.Objects::nonNull)
                .filter(suite -> suite.getTestCases() != null)
                .flatMap(suite -> suite.getTestCases().stream())
                .filter(java.util.Objects::nonNull)
                .filter(testCase -> testCase.getMethodName() != null)
                .filter(testCase -> testCase.getMethodName().equals(rubricName)
                        || testCase.getMethodName().equals(rubricName + "()"))
                .findFirst()
                .orElse(null);
    }

    private String compilationStage(TestCodeResponse response) {
        return response == null || response.getCompilationStage() == null
                ? "unknown build stage"
                : response.getCompilationStage().name();
    }

    private void markNotExecuted(TestExecution execution, String reason) {
        execution.setMethodName(execution.getRubricGrade().getName());
        execution.setExecutionTime(0L);
        execution.setOutput("");
        execution.setError(reason);
        execution.setStatus(TestExecution.ExecutionStatus.NOT_EXECUTED);
    }

    private TestExecution mapToEntity(TestExecutionDTO dto) {
        TestExecution entity = new TestExecution();
        entity.setId(dto.getId());
        entity.setMethodName(dto.getMethodName());
        entity.setOutput(dto.getOutput());
        entity.setError(dto.getError());
        entity.setExecutionTime(dto.getExecutionTime());
        entity.setStatus(TestExecution.ExecutionStatus.valueOf(dto.getStatus()));

//    if (dto.getRubricGradeId() != null) {
//        RubricGrade rubricGrade = rubricGradeRepository.findById(dto.getRubricGradeId())
//                .orElseThrow(() -> new RuntimeException("RubricGrade not found with id: " + dto.getRubricGradeId()));
//        entity.setRubricGrade(rubricGrade);
//    }

        return entity;
    }
}

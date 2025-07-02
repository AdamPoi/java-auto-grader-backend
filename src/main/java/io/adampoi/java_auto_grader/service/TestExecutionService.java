package io.adampoi.java_auto_grader.service;


import io.adampoi.java_auto_grader.domain.RubricGrade;
import io.adampoi.java_auto_grader.domain.Submission;
import io.adampoi.java_auto_grader.domain.SubmissionCode;
import io.adampoi.java_auto_grader.domain.TestExecution;
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
                .build();
        TestCodeResponse testCodeResponse = testCodeService.runTestCode(testCodeRequest);

        List<RubricGrade> rubricGrades = rubricGradeRepository.findByAssignmentId(UUID.fromString(request.getAssignmentId()));
        List<TestExecutionDTO> executionResultDTOs = new ArrayList<>();
        List<TestExecution> executionResults = new ArrayList<>();
        Submission submission = new Submission();
        submission.setAssignment(assignmentRepository.getById(UUID.fromString(request.getAssignmentId())));
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

        for (RubricGrade rubricGrade : rubricGrades) {

            TestCaseResult matchingTestCase = testCodeResponse.getTestSuites().stream()
                    .flatMap(suite -> suite.getTestCases().stream())
                    .filter(testCase -> testCase.getMethodName().equals(rubricGrade.getName() + "()"))
                    .findFirst()
                    .orElse(null);

            TestExecution testExecution = new TestExecution();
            testExecution.setRubricGrade(rubricGrade);
            testExecution.setSubmission(submission);

            if (matchingTestCase != null) {
                testExecution.setExecutionTime((long) matchingTestCase.getExecutionTime());
                testExecution.setMethodName(matchingTestCase.getMethodName());
                testExecution.setOutput(testCodeResponse.getError());
                testExecution.setError(matchingTestCase.getFailureMessage());
                testExecution.setStatus(TestExecution.ExecutionStatus.valueOf(matchingTestCase.getStatus()));

            } else {
                testExecution.setStatus(TestExecution.ExecutionStatus.FAILED);
                log.warn("No matching test case found for rubric grade: {}", rubricGrade.getName());
            }
            executionResultDTOs.add(mapToDTO(testExecution, new TestExecutionDTO()));
            executionResults.add(testExecution);
        }

        submission.setStatus(testCodeResponse.isSuccess() ? Submission.SubmissionStatus.COMPLETED : Submission.SubmissionStatus.FAILED);
        submission.setTestExecutions(new HashSet<>(executionResults));
        Submission finalSubmission = submission;
        if (!testCodeResponse.getCompilationErrors().isEmpty()) {
            submission.setStatus(Submission.SubmissionStatus.FAILED);
            submission.setManualFeedback("Compilation errors occurred during test execution.");
        } else if (testCodeResponse.getTestSuites().isEmpty()) {
            submission.setStatus(Submission.SubmissionStatus.FAILED);
            submission.setManualFeedback("No test cases were executed.");
            finalSubmission = submissionRepository.save(submission);
        } else {
            submission.setManualFeedback("All tests executed successfully.");
            finalSubmission = submissionRepository.save(submission);
        }

        finalSubmission.setSubmissionCodes(null);
        finalSubmission.setTestExecutions(null);

        return SubmissionService.mapToDTO(finalSubmission, new SubmissionDTO());
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
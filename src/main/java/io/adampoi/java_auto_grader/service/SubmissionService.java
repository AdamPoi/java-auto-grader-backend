package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.domain.*;
import io.adampoi.java_auto_grader.model.dto.*;
import io.adampoi.java_auto_grader.model.request.TestCodeRequest;
import io.adampoi.java_auto_grader.model.request.TestSubmitRequest;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.model.response.TestCodeResponse;
import io.adampoi.java_auto_grader.model.type.CodeFile;
import io.adampoi.java_auto_grader.model.type.TestCaseResult;
import io.adampoi.java_auto_grader.repository.*;
import io.adampoi.java_auto_grader.util.ReferencedException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class SubmissionService {

    private static final long EXECUTION_TIMEOUT_SECONDS = 30;
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final TestCodeService testCodeService;
    private final RubricGradeRepository rubricGradeRepository;

    public SubmissionService(final SubmissionRepository submissionRepository,
                             final AssignmentRepository assignmentRepository, final UserRepository userRepository,
                             final ClassroomRepository classroomRepository, TestCodeService testCodeService, RubricGradeRepository rubricGradeRepository) {
        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.testCodeService = testCodeService;
        this.rubricGradeRepository = rubricGradeRepository;
    }

    public static SubmissionDTO mapToDTO(final Submission submission, final SubmissionDTO submissionDTO) {
        submissionDTO.setId(submission.getId());
        submissionDTO.setExecutionTime(submission.getExecutionTime());
        submissionDTO.setStatus(submission.getStatus());
        submissionDTO.setFeedback(submission.getFeedback());
        submissionDTO.setStartedAt(submission.getStartedAt());
        submissionDTO.setCompletedAt(submission.getCompletedAt());
        submissionDTO.setTotalPoints(submission.getTotalPoints());
        submissionDTO.setSubmissionCodes(submission.getSubmissionCodes().stream()
                .map(submissionCode -> SubmissionCodeService.mapToDTO(submissionCode, new SubmissionCodeDTO()))
                .collect(Collectors.toList()));
        if (submission.getTestExecutions() != null) {
            submissionDTO.setTestExecutions(submission.getTestExecutions().stream()
                    .map(testExecution -> TestExecutionService.mapToDTO(testExecution, new TestExecutionDTO()))
                    .collect(Collectors.toList()));
        } else {
            submissionDTO.setTestExecutions(new ArrayList<>());
        }
        if (submission.getStudent() != null) {
        submissionDTO.setStudent(UserService.mapToDTO(submission.getStudent(), new UserDTO()));
            submissionDTO.setStudentId(submission.getStudent().getId());
        }
        return submissionDTO;
    }

    public PageResponse<SubmissionDTO> findAll(QueryFilter<Submission> filter, Pageable pageable) {
        final Page<Submission> page = submissionRepository.findAll(filter, pageable);
        Page<SubmissionDTO> dtoPage = new PageImpl<>(page.getContent()
                .stream()
                .map(submission -> mapToDTO(submission, new SubmissionDTO()))
                .collect(Collectors.toList()),
                pageable, page.getTotalElements());
        return PageResponse.from(dtoPage);
    }

    public SubmissionDTO get(final UUID submissionId) {
        return submissionRepository.findById(submissionId)
                .map(submission -> mapToDTO(submission, new SubmissionDTO()))
                .orElseThrow(() -> new EntityNotFoundException("Submission not found"));
    }

    public SubmissionDTO create(final SubmissionDTO submissionDTO) {
        final Submission submission = new Submission();
        mapToEntity(submissionDTO, submission);

        if (submission.getSubmissionCodes() != null) {
            submission.getSubmissionCodes().forEach(submissionCode ->
                    submissionCode.setSubmission(submission)
            );
        }

        Submission savedSubmission = submissionRepository.save(submission);
        return mapToDTO(savedSubmission, new SubmissionDTO());
    }

    public SubmissionDTO update(final UUID submissionId, final SubmissionDTO submissionDTO) {
        final Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found"));
        mapToEntity(submissionDTO, submission);
        Submission savedSubmission = submissionRepository.save(submission);
        return mapToDTO(savedSubmission, new SubmissionDTO());
    }

    public void delete(final UUID submissionId) {
        final ReferencedWarning referencedWarning = getReferencedWarning(submissionId);
        if (referencedWarning != null) {
            throw new ReferencedException(referencedWarning);
        }
        submissionRepository.deleteById(submissionId);
    }

    public String compile(SubmissionCompileDTO code) throws IOException, NoSuchMethodException, ClassNotFoundException,
            InvocationTargetException, InstantiationException, IllegalAccessException {
        File root = Files.createTempDirectory("java").toFile();
        File sourceFile = new File(root, "HelloWorld.java");
        sourceFile.getParentFile().mkdirs();
        Files.write(sourceFile.toPath(), code.getCode().getBytes(StandardCharsets.UTF_8));

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        ByteArrayOutputStream compileErrors = new ByteArrayOutputStream();
        int compilationResult = compiler.run(null, null, compileErrors, sourceFile.getPath());

        if (compilationResult != 0) {
            throw new RuntimeException("Compilation failed: " + compileErrors.toString());
        }

        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{root.toURI().toURL()});
        Class<?> cls = Class.forName("HelloWorld", true, classLoader);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        System.setOut(ps);

        try {
            cls.getDeclaredMethod("main", String[].class).invoke(null, (Object) new String[0]);
        } finally {
            System.out.flush();
            System.setOut(old);
            classLoader.close();
        }

        return baos.toString();
    }

    @Transactional
    public SubmissionDTO submitStudentSubmission(UUID studentId, TestSubmitRequest request) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
        Assignment assignment = assignmentRepository.getById(UUID.fromString(request.getAssignmentId()));

        Submission savedSubmission = processSubmissionWithTestResults(
                assignment,
                student,
                request.getSourceFiles(),
                request.getTestFiles(),
                request.getMainClassName(),
                request.getBuildTool(),
                true
        );

        return SubmissionService.mapToDTO(savedSubmission, new SubmissionDTO());
    }


    public SubmissionDTO tryoutSubmission(TestSubmitRequest request) {
        Assignment assignment = assignmentRepository.getById(UUID.fromString(request.getAssignmentId()));

        Submission simulatedSubmission = processSubmissionWithTestResults(
                assignment,
                null,
                request.getSourceFiles(),
                request.getTestFiles(),
                request.getMainClassName(),
                request.getBuildTool(),
                false
        );

        return SubmissionService.mapToDTO(simulatedSubmission, new SubmissionDTO());
    }


    @Transactional
    public BulkSubmissionDTO uploadBulkSubmissionByNime(
            UUID teacherId,
            UUID assignmentId,
            Map<String, List<CodeFile>> nimToCodeFiles,
            List<CodeFile> testFiles,
            String mainClassName,
            String buildTool
    ) {
        BulkSubmissionDTO result = new BulkSubmissionDTO();
        Assignment assignment = assignmentRepository.getById(assignmentId);

        for (Map.Entry<String, List<CodeFile>> entry : nimToCodeFiles.entrySet()) {
            String nim = entry.getKey();
            BulkSubmissionDTO.Item item = new BulkSubmissionDTO.Item();
            item.setNim(nim);

            try {
                User student = userRepository.findByNim(nim)
                        .orElseThrow(() -> new EntityNotFoundException("Student with NIM " + nim + " not found"));

                Submission savedSubmission = processSubmissionWithTestResults(
                        assignment,
                        student,
                        entry.getValue(),
                        testFiles,
                        mainClassName,
                        buildTool,
                        true
                );

                item.setSuccess(true);
                item.setMessage("Submission processed, ID: " + savedSubmission.getId());
            } catch (Exception e) {
                item.setSuccess(false);
                item.setMessage(e.getMessage());
            }

            result.getResults().add(item);
        }
        return result;
    }

    private Submission mapToEntity(final SubmissionDTO submissionDTO, final Submission submission) {
        if (submissionDTO.getExecutionTime() != null) {
            submission.setExecutionTime(submissionDTO.getExecutionTime());
        }

        if (submissionDTO.getStatus() != null) {
            submission.setStatus(submissionDTO.getStatus());
        }
        if (submissionDTO.getFeedback() != null) {
            submission.setFeedback(submissionDTO.getFeedback());
        }
        if (submissionDTO.getStartedAt() != null) {
            submission.setStartedAt(submissionDTO.getStartedAt());
        }
        if (submissionDTO.getCompletedAt() != null) {
            submission.setCompletedAt(submissionDTO.getCompletedAt());
        }
        if (submissionDTO.getAssignmentId() != null) {
            final Assignment assignment = assignmentRepository.findById(submissionDTO.getAssignmentId())
                    .orElseThrow(() -> new EntityNotFoundException("assignment not found"));
            submission.setAssignment(assignment);
        }
        if (submissionDTO.getStudentId() != null) {
            final User student = userRepository.findById(submissionDTO.getStudentId())
                    .orElseThrow(() -> new EntityNotFoundException("student not found"));
            submission.setStudent(student);
        }
        if (submissionDTO.getSubmissionCodes() != null && !submissionDTO.getSubmissionCodes().isEmpty()) {
            submission.setSubmissionCodes(submissionDTO.getSubmissionCodes()
                    .stream()
                    .map(codeDTO -> {
                        SubmissionCode submissionCode = SubmissionCodeService.mapToEntity(codeDTO, new SubmissionCode());
                        // Set the submission reference for bidirectional relationship
                        submissionCode.setSubmission(submission);
                        return submissionCode;
                    })
                    .collect(Collectors.toSet()));
        }

        return submission;
    }


    public ReferencedWarning getReferencedWarning(final UUID submissionId) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found"));

        return null;
    }

    private Submission processSubmissionWithTestResults(
            Assignment assignment,
            User student,
            List<CodeFile> sourceFiles,
            List<CodeFile> testFiles,
            String mainClassName,
            String buildTool,
            boolean persist
    ) {
        // --- 1. Run Tests ---
        TestCodeResponse testCodeResponse = testCodeService.runTestCode(
                TestCodeRequest.builder()
                        .sourceFiles(sourceFiles)
                        .testFiles(testFiles != null ? testFiles : Collections.emptyList())
                        .mainClassName(mainClassName)
                        .buildTool(buildTool)
                        .build()
        );

        // --- 2. Build Submission Codes ---
        Set<SubmissionCode> codes = sourceFiles.stream()
                .map(sf -> SubmissionCode.builder()
                        .fileName(sf.getFileName())
                        .sourceCode(sf.getContent())
                        .build()
                )
                .collect(Collectors.toSet());


        // --- 3. Build Test Executions (from rubrics) ---


        Set<TestExecution> testExecutions = rubricGradeRepository.findByAssignmentId(assignment.getId()).stream()
                .map(rubricGrade -> mapRubricToTestExecution(rubricGrade, testCodeResponse, null))
                .collect(Collectors.toSet());

        // --- 4. Calculate total points ---
        int totalPoints = calculateTotalPoints(testExecutions);

        // --- 5. Build Submission ---
        Submission submission = Submission.builder()
                .assignment(assignment)
                .student(student)
                .submissionCodes(codes)
                .testExecutions(testExecutions)
                .startedAt(OffsetDateTime.now())
                .completedAt(OffsetDateTime.now())
                .executionTime(testCodeResponse.getExecutionTime())
                .totalPoints(totalPoints)
                .feedback(testCodeResponse.isSuccess() ? "All tests passed" : "Some or all tests failed")
                .status(testCodeResponse.isSuccess() ? "PASSED" : "FAILED")
                .build();

        codes.forEach(code -> code.setSubmission(submission));
        testExecutions.forEach(exec -> exec.setSubmission(submission));

        return persist ? submissionRepository.save(submission) : submission;
    }


    private TestExecution mapRubricToTestExecution(
            RubricGrade rubricGrade,
            TestCodeResponse testCodeResponse,
            Submission submission
    ) {
        String expectedMethodName = rubricGrade.getName() + "()";
        log.info("Looking for test method: {}", expectedMethodName);

        // Handle null/empty test response
        if (testCodeResponse == null || testCodeResponse.getTestSuites() == null) {
            log.warn("TestCodeResponse or test suites is null for rubric: {}", rubricGrade.getName());
            return createFailedTestExecution(rubricGrade, submission, "Test execution failed - no test results available");
        }

        Optional<TestCaseResult> maybeCase = testCodeResponse.getTestSuites().stream()
                .filter(suite -> suite != null && suite.getTestCases() != null) // Null safety
                .flatMap(suite -> suite.getTestCases().stream())
                .filter(tc -> tc != null && matchesTestMethod(tc, rubricGrade.getName())) // Improved matching
                .findFirst();

        if (maybeCase.isPresent()) {
            TestCaseResult tc = maybeCase.get();
            return TestExecution.builder()
                    .rubricGrade(rubricGrade)
                    .submission(submission)
                    .methodName(tc.getMethodName())
                    .executionTime(tc.getExecutionTime() > 0 ? Math.round(tc.getExecutionTime() * 1000) : 0L)
                    .output(String.valueOf(Optional.ofNullable(tc.getFailureMessage())))
                    .error(tc.getFailureMessage())
                    .status(parseExecutionStatus(tc.getStatus()))
                    .build();
        } else {
            log.warn("No matching test case found for rubric grade: {}", rubricGrade.getName());
            return createFailedTestExecution(rubricGrade, submission, "No matching test case found for rubric grade: " + rubricGrade.getName());
        }
    }

    // Helper method for more flexible test method matching
    private boolean matchesTestMethod(TestCaseResult testCase, String rubricName) {
        if (testCase.getMethodName() == null || rubricName == null) {
            return false;
        }

        String methodName = testCase.getMethodName();
        String expectedName = rubricName + "()";

        // Exact match
        if (methodName.equals(expectedName)) {
            return true;
        }

        // Case-insensitive match
        if (methodName.equalsIgnoreCase(expectedName)) {
            return true;
        }

        // Match without parentheses
        if (methodName.equals(rubricName)) {
            return true;
        }

        // Match with test prefix (e.g., "testMethodName" matches "methodName")
        if (methodName.toLowerCase().startsWith("test") &&
                methodName.substring(4).equalsIgnoreCase(rubricName)) {
            return true;
        }

        return false;
    }

    // Helper method to safely parse execution status
    private TestExecution.ExecutionStatus parseExecutionStatus(String status) {
        if (status == null) {
            return TestExecution.ExecutionStatus.FAILED;
        }

        try {
            return TestExecution.ExecutionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown test execution status: {}, defaulting to FAILED", status);
            return TestExecution.ExecutionStatus.FAILED;
        }
    }

    // Helper method to create failed test execution
    private TestExecution createFailedTestExecution(RubricGrade rubricGrade, Submission submission, String errorMessage) {
        return TestExecution.builder()
                .rubricGrade(rubricGrade)
                .submission(submission)
                .methodName(rubricGrade.getName())
                .executionTime(0L)
                .output("")
                .error(errorMessage)
                .status(TestExecution.ExecutionStatus.FAILED)
                .build();
    }

    private int calculateTotalPoints(Set<TestExecution> testExecutions) {
        return testExecutions.stream()
                .filter(execution -> execution.getStatus() == TestExecution.ExecutionStatus.PASSED)
                .mapToInt(execution -> execution.getRubricGrade().getRubric().getPoints())
                .sum();
    }

}

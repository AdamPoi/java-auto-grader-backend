package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.domain.Assignment;
import io.adampoi.java_auto_grader.domain.Submission;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.model.dto.RunResultDTO;
import io.adampoi.java_auto_grader.model.dto.SubmissionCompileDTO;
import io.adampoi.java_auto_grader.model.dto.SubmissionDTO;
import io.adampoi.java_auto_grader.model.dto.TestResultDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.model.response.SubmissionCompileResponse;
import io.adampoi.java_auto_grader.repository.AssignmentRepository;
import io.adampoi.java_auto_grader.repository.ClassroomRepository;
import io.adampoi.java_auto_grader.repository.SubmissionRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import io.adampoi.java_auto_grader.util.NotFoundException;
import io.adampoi.java_auto_grader.util.ReferencedException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

@Service
@Transactional
@Slf4j
public class SubmissionService {

    private static final long EXECUTION_TIMEOUT_SECONDS = 30;
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;

    public SubmissionService(final SubmissionRepository submissionRepository,
                             final AssignmentRepository assignmentRepository, final UserRepository userRepository,
                             final ClassroomRepository classroomRepository) {
        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.classroomRepository = classroomRepository;
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
                .orElseThrow(() -> new NotFoundException("Submission not found"));
    }

    public SubmissionDTO create(final SubmissionDTO submissionDTO) {
        final Submission submission = new Submission();
        mapToEntity(submissionDTO, submission);
        Submission savedSubmission = submissionRepository.save(submission);
        return mapToDTO(savedSubmission, new SubmissionDTO());
    }

    public SubmissionDTO update(final UUID submissionId, final SubmissionDTO submissionDTO) {
        final Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));
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

    private SubmissionDTO mapToDTO(final Submission submission, final SubmissionDTO submissionDTO) {
        submissionDTO.setId(submission.getId());
        submissionDTO.setSubmissionTime(submission.getSubmissionTime());
        submissionDTO.setAttemptNumber(submission.getAttemptNumber());
        submissionDTO.setStatus(submission.getStatus());
        submissionDTO.setGraderFeedback(submission.getGraderFeedback());
        submissionDTO.setGradingStartedAt(submission.getGradingStartedAt());
        submissionDTO.setGradingCompletedAt(submission.getGradingCompletedAt());
        submissionDTO.setAssignment(submission.getAssignment() == null ? null : submission.getAssignment().getId());
        submissionDTO.setStudent(submission.getStudent() == null ? null : submission.getStudent().getId());
        return submissionDTO;
    }

    private Submission mapToEntity(final SubmissionDTO submissionDTO, final Submission submission) {
        if (submissionDTO.getSubmissionTime() != null) {
            submission.setSubmissionTime(submissionDTO.getSubmissionTime());
        }
        if (submissionDTO.getAttemptNumber() != null) {
            submission.setAttemptNumber(submissionDTO.getAttemptNumber());
        }
        if (submissionDTO.getStatus() != null) {
            submission.setStatus(submissionDTO.getStatus());
        }
        if (submissionDTO.getGraderFeedback() != null) {
            submission.setGraderFeedback(submissionDTO.getGraderFeedback());
        }
        if (submissionDTO.getGradingStartedAt() != null) {
            submission.setGradingStartedAt(submissionDTO.getGradingStartedAt());
        }
        if (submissionDTO.getGradingCompletedAt() != null) {
            submission.setGradingCompletedAt(submissionDTO.getGradingCompletedAt());
        }
        if (submissionDTO.getAssignment() != null) {
            final Assignment assignment = assignmentRepository.findById(submissionDTO.getAssignment())
                    .orElseThrow(() -> new NotFoundException("assignment not found"));
            submission.setAssignment(assignment);
        }
        if (submissionDTO.getStudent() != null) {
            final User student = userRepository.findById(submissionDTO.getStudent())
                    .orElseThrow(() -> new NotFoundException("student not found"));
            submission.setStudent(student);
        }
        return submission;
    }

    public ReferencedWarning getReferencedWarning(final UUID submissionId) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));

        return null;
    }

    public SubmissionCompileResponse executeDockerGradleTest(MultipartFile zipFile) throws Exception {
        Path tempDir = Files.createTempDirectory("gradle-project-");
        log.info("Created temporary directory for user project at: {}", tempDir);

        try {
            unzip(zipFile, tempDir);

            File gradlew = findGradlewScript(tempDir.toFile());
            if (gradlew == null) {
                throw new IllegalStateException("gradlew script not found in the project or its subdirectories.");
            }

            gradlew.setExecutable(true);

            Path relativePath = tempDir.relativize(gradlew.toPath());
            String gradlewPath = "./" + relativePath.toString().replace("\\", "/");

            ProcessBuilder downloadBuilder = new ProcessBuilder(
                    "docker", "run", "--rm", "--memory=512m",
                    "--memory-swap=512m", "--cpus=1.0", "--pids-limit=200",
                    "-v", tempDir.toAbsolutePath() + ":/project",
                    "-w", "/project",
                    "gradle-runner:latest",
                    "/bin/bash", "-c",
                    "chmod +x " + gradlewPath + " && " + gradlewPath + " --version && " + gradlewPath
                            + " dependencies --no-daemon");

            // log.info("Downloading Gradle dependencies...");
            // downloadBuilder.redirectErrorStream(true);
            // Process downloadProcess = downloadBuilder.start();
            //
            // StringBuilder downloadOutput = new StringBuilder();
            // try (BufferedReader reader = new BufferedReader(new
            // InputStreamReader(downloadProcess.getInputStream()))) {
            // String line;
            // while ((line = reader.readLine()) != null) {
            // downloadOutput.append(line).append("\n");
            // log.debug("Download: {}", line);
            // }
            // }
            //
            // if (!downloadProcess.waitFor(120, TimeUnit.SECONDS)) { // 2 minutes for
            // download
            // downloadProcess.destroyForcibly();
            // throw new InterruptedException("Dependency download timed out.");
            // }

            // int downloadExitCode = downloadProcess.exitValue();
            // if (downloadExitCode != 0) {
            // log.error("Dependency download failed with exit code: {}", downloadExitCode);
            // log.error("Download output: {}", downloadOutput.toString());
            // }

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "docker", "run", "--rm", "--network=none", "--memory=512m",
                    "--memory-swap=512m", "--cpus=1.0", "--pids-limit=200",
                    "-v", tempDir.toAbsolutePath() + ":/project",
                    "-w", "/project",
                    "gradle-runner:latest",
                    "/bin/bash", "-c",
                    "chmod +x " + gradlewPath + " && " + gradlewPath
                            + " test  --build-cache --stacktrace --info --no-daemon --no-scan --offline --parallel --max-workers=2");

            log.info("Executing tests with network isolation...");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            StringBuilder rawOutputBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    rawOutputBuilder.append(line).append("\n");
                    log.debug("Test output: {}", line);
                }
            }

            boolean finished = process.waitFor(EXECUTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            int exitCode = process.exitValue();

            if (!finished) {
                process.destroyForcibly();
                throw new InterruptedException("Execution timed out after " + EXECUTION_TIMEOUT_SECONDS + " seconds.");
            }

            String fullOutput = rawOutputBuilder.toString();
            log.info("Process completed with exit code: {}", exitCode);
            log.debug("Full output length: {}", fullOutput.length());

            return parseTestOutput(fullOutput);

        } finally {
            log.info("Cleaning up host directory: {}", tempDir);
            FileSystemUtils.deleteRecursively(tempDir);
        }
    }

    private SubmissionCompileResponse parseTestOutput(String rawOutput) {
        int passed = 0;
        int failed = 0;
        int skipped = 0;
        List<String> failedTests = new ArrayList<>();

        String[] lines = rawOutput.split("\n");
        for (String line : lines) {
            if (line.contains(" PASSED")) {
                passed++;
            } else if (line.contains(" SKIPPED")) {
                skipped++;
            } else if (line.contains(" FAILED")) {
                failed++;

                String testName = line.substring(0, line.indexOf(" FAILED")).trim();
                failedTests.add(testName);
            }
        }
        int total = passed + failed + skipped;

        SubmissionCompileResponse response = new SubmissionCompileResponse();
        response.setTotal(total);
        response.setFailed(failed);
        response.setPassed(passed);
        response.setSkipped(skipped);

        RunResultDTO runResult = new RunResultDTO();
        runResult.setError(failedTests.toString());
        runResult.setOutput(rawOutput);
        response.setRun(runResult);
        return response;
    }

    public SubmissionCompileResponse executeGradleTest(MultipartFile zipFile) throws Exception {
        Path tempDir = Files.createTempDirectory("gradle-project-");

        try {
            unzip(zipFile, tempDir);

            File gradlew = findGradlewScript(tempDir.toFile());
            if (gradlew == null) {
                throw new IllegalStateException("gradlew script not found in the project or its subdirectories.");
            }

            gradlew.setExecutable(true);
            File workingDirectory = gradlew.getParentFile();

            StringBuilder combinedOutput = new StringBuilder();

            combinedOutput.append("=== GRADLE TEST OUTPUT ===\n");
            String testOutput = executeOptimizedGradleTest(gradlew, workingDirectory);
            combinedOutput.append(testOutput).append("\n");

            List<TestResultDTO> testResults = parseTestResults(testOutput);

            SubmissionCompileResponse response = new SubmissionCompileResponse();
            response.setScore(calculateScore(testResults));
            response.setTests(testResults);
            response.setPassed(calculateOverallSuccess(testResults));

            return response;

        } finally {
            cleanupAsync(tempDir);
        }
    }

    private Boolean determineOverallSuccess(String testOutput, String runOutput) {
        boolean testsSuccessful = !testOutput.contains("FAILED") && testOutput.contains("BUILD SUCCESSFUL");
        boolean runSuccessful = runOutput.contains("BUILD SUCCESSFUL") || !runOutput.contains("FAILED");
        return testsSuccessful && runSuccessful;
    }

    private Integer calculateScore(List<TestResultDTO> testResults) {
        if (testResults.isEmpty()) {
            return null;
        }

        long passedTests = testResults.stream()
                .filter(test -> "PASSED".equals(test.getStatus()))
                .count();

        return (int) ((passedTests * 100) / testResults.size());
    }

    private String extractErrorMessage(String testOutput, String runOutput, String cleanOutput) {
        if (testOutput.contains("FAILED")) {
            return extractFailureDetails(testOutput);
        }
        if (runOutput.contains("FAILED") || runOutput.contains("Exception")) {
            return extractFailureDetails(runOutput);
        }
        if (cleanOutput.contains("FAILED")) {
            return extractFailureDetails(cleanOutput);
        }
        return null;
    }

    private String extractFailureDetails(String output) {
        String[] lines = output.split("\n");
        StringBuilder errorDetails = new StringBuilder();

        boolean inErrorSection = false;
        for (String line : lines) {

            if (line.contains("FAILED") || line.contains("Exception") || line.contains("Error")) {
                inErrorSection = true;
            }

            if (inErrorSection && !line.trim().isEmpty()) {
                errorDetails.append(line).append("\n");
            }

            if (inErrorSection && line.startsWith("===")) {
                break;
            }
        }

        return errorDetails.length() > 0 ? errorDetails.toString().trim() : null;
    }

    private List<TestResultDTO> parseTestResults(String testOutput) {
        List<TestResultDTO> testResults = new ArrayList<>();

        if (testOutput == null || testOutput.trim().isEmpty()) {
            return testResults;
        }

        String[] lines = testOutput.split("\n");

        java.util.Arrays.stream(lines)
                .parallel()
                .map(String::trim)
                .filter(this::isTestResultLine)
                .forEach(line -> {
                    TestResultDTO testResult = parseTestResultLine(line, lines, 0);
                    if (testResult != null) {
                        synchronized (testResults) { // Thread-safe addition
                            testResults.add(testResult);
                        }
                    }
                });

        log.info("Parsed {} test results", testResults.size());
        return testResults;
    }

    private boolean isTestResultLine(String line) {
        return (line.contains(" PASSED ") || line.contains(" FAILED ")) && line.contains("()");
    }

    private TestResultDTO parseTestResultLine(String line, String[] allLines, int currentIndex) {
        TestResultDTO testResult = new TestResultDTO();

        String status = line.contains(" PASSED ") ? "PASSED" : "FAILED";
        String delimiter = " " + status + " ";
        int delimiterIndex = line.indexOf(delimiter);

        String beforeStatus = line.substring(0, delimiterIndex);
        String afterStatus = line.substring(delimiterIndex + delimiter.length());

        String testClass = parseTestClass(beforeStatus);
        String testName = parseTestName(beforeStatus);

        if (testClass == null || testName == null) {
            return null;
        }

        testResult.setName(testName);
        testResult.setTestClass(testClass);
        testResult.setStatus(status);
        testResult.setExecutionTime(parseExecutionTime(afterStatus));

        if ("FAILED".equals(status)) {
            testResult.setError(collectErrorMessage(allLines, currentIndex));
        }

        return testResult;
    }

    private String parseTestClass(String beforeStatus) {
        int lastSpaceIndex = beforeStatus.lastIndexOf(' ');
        if (lastSpaceIndex <= 0) {
            return null;
        }

        return beforeStatus.substring(0, lastSpaceIndex).trim();
    }

    private String parseTestName(String beforeStatus) {
        int lastSpaceIndex = beforeStatus.lastIndexOf(' ');
        if (lastSpaceIndex <= 0) {
            return null;
        }

        String testName = beforeStatus.substring(lastSpaceIndex + 1).trim();

        if (testName.endsWith("()")) {
            testName = testName.substring(0, testName.length() - 2);
        }

        return testName;
    }

    private int parseExecutionTime(String afterStatus) {
        if (afterStatus.contains("(") && afterStatus.contains(")")) {
            int startParen = afterStatus.indexOf("(");
            int endParen = afterStatus.indexOf(")");
            String timeStr = afterStatus.substring(startParen + 1, endParen);

            try {
                return Integer.parseInt(timeStr.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                System.out.println("Could not parse execution time: " + timeStr);
                return 0;
            }
        }
        return 0;
    }

    private String collectErrorMessage(String[] lines, int startIndex) {
        StringBuilder errorBuilder = new StringBuilder();

        for (int j = startIndex + 1; j < lines.length; j++) {
            String errorLine = lines[j].trim();

            if (isTestResultLine(errorLine)) {
                break;
            }

            if (!errorLine.isEmpty()) {
                if (errorBuilder.length() > 0) {
                    errorBuilder.append("\n");
                }
                errorBuilder.append(errorLine);
            }
        }

        return errorBuilder.length() > 0 ? errorBuilder.toString().trim() : null;
    }

    private void logParsedTest(TestResultDTO testResult) {
        System.out.println("Parsed test: " + testResult.getTestClass() + "." +
                testResult.getName() + " - " + testResult.getStatus() +
                " (" + testResult.getExecutionTime() + "ms)");

        if (testResult.getError() != null && !testResult.getError().isEmpty()) {
            System.out.println("Error: " + testResult.getError());
        }
    }

    private void unzip(MultipartFile file, Path destination) throws Exception {
        byte[] buffer = new byte[8192];

        try (ZipInputStream zipIn = new ZipInputStream(file.getInputStream())) {
            for (var entry = zipIn.getNextEntry(); entry != null; entry = zipIn.getNextEntry()) {
                Path filePath = destination.resolve(entry.getName()).normalize();

                if (!filePath.startsWith(destination)) {
                    throw new SecurityException("Invalid zip entry: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    Files.createDirectories(filePath.getParent());

                    try (var outputStream = Files.newOutputStream(filePath);
                         var bufferedOutputStream = new BufferedOutputStream(outputStream, 8192)) {

                        int len;
                        while ((len = zipIn.read(buffer)) > 0) {
                            bufferedOutputStream.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }

    private void logDirectoryContents(Path directory) {
        System.out.println("=== Extracted files in: " + directory.toString() + " ===");
        try {
            Files.walk(directory)
                    .forEach(path -> {
                        String relativePath = directory.relativize(path).toString();
                        if (Files.isDirectory(path)) {
                            // System.out.println("[DIR] " + relativePath);
                        } else {
                            try {
                                long size = Files.size(path);
                                // System.out.println("[FILE] " + relativePath + " (" + size + " bytes)");
                            } catch (Exception e) {
                                // System.out.println("[FILE] " + relativePath + " (size unknown)");
                            }
                        }
                    });
        } catch (Exception e) {
            System.err.println("Error listing directory contents: " + e.getMessage());
        }
        System.out.println("=== End of file listing ===");
    }

    private File findGradlewScript(File directory) {
        log.info("Searching for gradlew script in: {}", directory.getAbsolutePath());

        File gradlew = new File(directory, "gradlew");
        if (gradlew.exists() && gradlew.isFile()) {
            log.info("Found gradlew at: {}", gradlew.getAbsolutePath());
            return gradlew;
        }

        File gradlewBat = new File(directory, "gradlew.bat");
        if (gradlewBat.exists() && gradlewBat.isFile()) {
            log.info("Found gradlew.bat at: {}", gradlewBat.getAbsolutePath());
            return gradlewBat;
        }

        File[] subdirs = directory.listFiles(File::isDirectory);
        if (subdirs != null) {
            for (File subdir : subdirs) {
                gradlew = new File(subdir, "gradlew");
                if (gradlew.exists() && gradlew.isFile()) {
                    log.info("Found gradlew in subdirectory: {}", gradlew.getAbsolutePath());
                    return gradlew;
                }

                gradlewBat = new File(subdir, "gradlew.bat");
                if (gradlewBat.exists() && gradlewBat.isFile()) {
                    log.info("Found gradlew.bat in subdirectory: {}", gradlewBat.getAbsolutePath());
                    return gradlewBat;
                }
            }
        }

        log.error("Could not find gradlew script in {} or its subdirectories", directory.getAbsolutePath());
        return null;
    }

    private String executeOptimizedGradleTest(File gradlew, File workingDirectory) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder();

        List<String> command = new ArrayList<>();
        command.add(gradlew.getAbsolutePath());
        command.add("test");
        command.add("--no-daemon");
        command.add("--stacktrace");
        command.add("--continue");

        processBuilder.command(command);
        processBuilder.directory(workingDirectory);
        processBuilder.redirectErrorStream(true);

        Map<String, String> env = processBuilder.environment();
        env.put("GRADLE_OPTS", "-Xmx1g");

        if (!gradlew.canExecute()) {
            if (!gradlew.setExecutable(true)) {
                throw new RuntimeException("Cannot make gradlew executable");
            }
        }

        log.info("Executing Gradle test in directory: {}", workingDirectory.getAbsolutePath());
        log.info("Command: {}", String.join(" ", command));

        long startTime = System.currentTimeMillis();
        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            boolean finished = false;
            long lastOutputTime = startTime;

            while (!finished) {
                if (!process.isAlive()) {
                    finished = true;
                }

                while (reader.ready() && (line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    lastOutputTime = System.currentTimeMillis();

                    if (line.contains("FAILED") || line.contains("ERROR") || line.contains("Exception") ||
                            line.contains("Test") || line.contains("BUILD")) {
                    }
                }

                long currentTime = System.currentTimeMillis();
                if (currentTime - startTime > 180000) { // 3 minutes total timeout
                    log.warn("Gradle test execution timed out after 3 minutes");
                    process.destroyForcibly();
                    throw new RuntimeException("Gradle test execution timed out after 3 minutes");
                }

                if (currentTime - lastOutputTime > 60000) { // 1 minute without output
                    log.warn("No output from Gradle for 1 minute, considering it stuck");
                    process.destroyForcibly();
                    throw new RuntimeException("Gradle process appears to be stuck (no output for 1 minute)");
                }

                if (!finished) {
                    Thread.sleep(100);
                }
            }

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        log.info(output.toString());
        int exitCode = process.waitFor();
        long executionTime = System.currentTimeMillis() - startTime;

        log.info("Gradle test execution completed in {} ms with exit code {}", executionTime, exitCode);

        String outputStr = output.toString();
        StringBuilder result = new StringBuilder();

        result.append("=== GRADLE TEST EXECUTION RESULTS ===\n");
        result.append("Exit Code: ").append(exitCode).append("\n");
        result.append("Execution Time: ").append(executionTime).append("ms\n\n");

        String[] lines = outputStr.split("\n");
        boolean inTestSection = false;
        StringBuilder testResults = new StringBuilder();
        StringBuilder failureDetails = new StringBuilder();

        for (String line : lines) {
            if (line.contains("Test") && (line.contains("PASSED") || line.contains("FAILED"))) {
                testResults.append(line).append("\n");
            }

            if (line.contains("FAILURE:") || line.contains("java.lang.AssertionError") ||
                    line.contains("Expected:") || line.contains("Actual:")) {
                failureDetails.append(line).append("\n");
                inTestSection = true;
            } else if (inTestSection && (line.trim().isEmpty() || line.startsWith(">"))) {
                inTestSection = false;
            } else if (inTestSection) {
                failureDetails.append(line).append("\n");
            }

            if (line.contains("BUILD FAILED") || line.contains("BUILD SUCCESSFUL")) {
                result.append("Build Status: ").append(line.trim()).append("\n");
            }
        }

        if (testResults.length() > 0) {
            result.append("\n=== TEST RESULTS ===\n");
            result.append(testResults);
        }

        if (failureDetails.length() > 0) {
            result.append("\n=== FAILURE DETAILS ===\n");
            result.append(failureDetails);
        }

        result.append("\n=== FULL GRADLE OUTPUT ===\n");
        if (outputStr.length() > 5000) {
            result.append(outputStr.substring(outputStr.length() - 5000));
            result.append("\n[... output truncated ...]\n");
        } else {
            result.append(outputStr);
        }

        return result.toString();
    }

    private Integer calculateOverallSuccess(List<TestResultDTO> testResults) {
        if (testResults.isEmpty()) {
            return 0;
        }

        return testResults.stream()
                .filter(test -> "PASSED".equals(test.getStatus())).toList().size();
    }

    private void cleanupAsync(Path tempDir) {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                FileSystemUtils.deleteRecursively(tempDir);
                log.debug("Cleaned up temporary directory: {}", tempDir);
            } catch (Exception e) {
                log.warn("Failed to cleanup temporary directory: {}", tempDir, e);
            }
        });
    }

}

package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.model.dto.CodeFile;
import io.adampoi.java_auto_grader.model.dto.TestCaseResult;
import io.adampoi.java_auto_grader.model.dto.TestSuiteResult;
import io.adampoi.java_auto_grader.model.request.TestCodeRequest;
import io.adampoi.java_auto_grader.model.response.TestCodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TestCodeService {

    private static final String PERSISTENT_CONTAINER_NAME = "gradle-sandbox";
    private static final int TIMEOUT_SECONDS = 60;
    private static final String GRADLE_USER_HOME = "/workspace/.gradle";
    private volatile boolean containerReady = false;
    private volatile boolean gradleDaemonInitialized = false;

    private void ensureContainerReady() {
        if (!containerReady) {
            synchronized (this) {
                if (!containerReady) {
                    try {
                        if (isContainerRunning()) {
                            containerReady = true;
                            ensureGradleDaemonReady();
                            return;
                        }

                        startPersistentContainer();
                        containerReady = true;
                        ensureGradleDaemonReady();
                        log.info("Docker container {} started successfully", PERSISTENT_CONTAINER_NAME);

                    } catch (IOException | InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Failed to ensure Docker container is ready", e);
                    }
                }
            }
        }
    }

    private void ensureGradleDaemonReady() {
        if (!gradleDaemonInitialized) {
            synchronized (this) {
                if (!gradleDaemonInitialized) {
                    try {
                        initializeGradleDaemon();
                        gradleDaemonInitialized = true;
                        log.info("Gradle daemon initialized successfully");
                    } catch (Exception e) {
                        log.warn("Failed to pre-initialize Gradle daemon, will start on first use", e);
                    }
                }
            }
        }
    }


    private boolean isContainerRunning() throws IOException, InterruptedException {
        ProcessBuilder checkContainer = new ProcessBuilder(
                "docker", "inspect", "--format={{.State.Running}}", PERSISTENT_CONTAINER_NAME
        );
        Process checkProcess = checkContainer.start();
        checkProcess.waitFor();

        if (checkProcess.exitValue() == 0) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(checkProcess.getInputStream()))) {
                String isRunning = reader.readLine();
                return "true".equals(isRunning);
            }
        }
        return false;
    }

    private void initializeGradleDaemon() throws IOException, InterruptedException {
        log.info("Pre-initializing Gradle daemon in container");

        // Create a minimal project to warm up the daemon
        ProcessBuilder warmupCommand = new ProcessBuilder(
                "docker", "exec", PERSISTENT_CONTAINER_NAME,
                "sh", "-c",
                "mkdir -p /workspace/warmup && " +
                        "cd /workspace/warmup && " +
                        "echo 'plugins { id \"java\" }' > build.gradle && " +
                        "gradle --version --daemon --parallel --build-cache --configuration-cache --quiet && " +
                        "rm -rf /workspace/warmup"
        );

        Process warmupProcess = warmupCommand.start();
        boolean finished = warmupProcess.waitFor(30, TimeUnit.SECONDS);

        if (!finished) {
            warmupProcess.destroyForcibly();
            log.warn("Gradle daemon warmup timed out");
        } else {
            log.info("Gradle daemon warmed up, exit code: {}", warmupProcess.exitValue());
        }
    }

    private void startPersistentContainer() throws IOException, InterruptedException {
        // Remove existing stopped container if it exists
        ProcessBuilder removeContainer = new ProcessBuilder(
                "docker", "rm", "-f", PERSISTENT_CONTAINER_NAME
        );
        removeContainer.start().waitFor();

        // Start persistent container with optimized settings
        ProcessBuilder startContainer = new ProcessBuilder(
                "docker", "run", "-d", "--name", PERSISTENT_CONTAINER_NAME,
                "-v", "/tmp:/workspace",
                "--memory=3g",
                "--cpus=4",
                "--shm-size=1g", // Increase shared memory for better performance
                "-e", "GRADLE_OPTS=-Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+UseJVMCICompiler",
                "-e", "GRADLE_USER_HOME=" + GRADLE_USER_HOME,
                "-e", "JAVA_OPTS=-Xmx2g",
                "gradle-sandbox", "tail", "-f", "/dev/null"
        );
        startContainer.redirectErrorStream(true);
        Process startProcess = startContainer.start();

        int exitCode = startProcess.waitFor();
        if (exitCode != 0) {
            String errorOutput = readProcessOutput(startProcess);
            throw new RuntimeException("Failed to start Docker container: " + errorOutput);
        }

        // Wait for container to be fully ready and create gradle home
        Thread.sleep(1000);

        // Create gradle user home directory with proper permissions
        ProcessBuilder createGradleHome = new ProcessBuilder(
                "docker", "exec", PERSISTENT_CONTAINER_NAME,
                "sh", "-c", "mkdir -p " + GRADLE_USER_HOME + " && chmod 755 " + GRADLE_USER_HOME
        );
        createGradleHome.start().waitFor();
    }

    private void copyProjectToContainer(Path tempDir, String uuid) throws IOException, InterruptedException {
        log.info("Cleaning up container workspace for UUID: {}", uuid);
        // Clean up existing files in the workspace for this UUID
        ProcessBuilder cleanCommand = new ProcessBuilder(
                "docker", "exec", PERSISTENT_CONTAINER_NAME,
                "sh", "-c", "rm -rf /workspace/" + uuid + "/*"
        );
        Process cleanProcess = cleanCommand.start();
        cleanProcess.waitFor();

        log.info("Copying project files to container workspace for UUID: {}", uuid);
        ProcessBuilder copyCommand = new ProcessBuilder(
                "docker", "cp", tempDir.toString() + "/.",
                PERSISTENT_CONTAINER_NAME + ":/workspace/" + uuid + "/"
        );
        Process copyProcess = copyCommand.start();
        copyProcess.waitFor();
    }


    private String readProcessOutput(Process process) throws IOException {
        return new String(process.getInputStream().readAllBytes());
    }

    private String readProcessErrors(Process process) throws IOException {
        return new String(process.getErrorStream().readAllBytes());
    }

    public TestCodeResponse runTestCodeInDocker(TestCodeRequest testExecution) {
        Path tempDir = null;
        String uuid = java.util.UUID.randomUUID().toString();
        try {
            ensureContainerReady();

            // Create temporary directory for the project
            tempDir = Files.createTempDirectory("gradle-test-" + uuid + "-");
            log.info("Created temporary directory for UUID {}: {}", uuid, tempDir);

            // Set up Gradle project structure
            setupGradleProject(tempDir, testExecution);
            log.info("Gradle project structure set up for UUID {} in: {}", uuid, tempDir);

            // Copy project files to container
            copyProjectToContainer(tempDir, uuid);

            // Execute gradle test in container
            TestCodeResponse result = executeGradleTestWithXmlReport(uuid);

            // Copy test results back from container
            copyTestResultsFromContainer(tempDir, uuid);


            // Parse XML test reports and enhance the result
            parseAndAddXmlReports(result, tempDir);

            return result;

        } catch (Exception e) {
            log.error("Docker execution failed for UUID: {}", uuid, e);
            return createErrorResult("Docker execution failed: " + e.getMessage());
        } finally {
            if (tempDir != null) {
                cleanupTempDirectory(tempDir);
            }
            // Clean up container workspace for this UUID
            cleanupContainerWorkspace(uuid);
        }
    }


    private TestCodeResponse createTestResult(int exitCode, String output, String error) {
        TestCodeResponse result = new TestCodeResponse();
        result.setSuccess(exitCode == 0);
        result.setOutput(output);
        if (exitCode != 0) {
            result.setError(error);
        }
        return result;
    }

    private TestCodeResponse createErrorResult(String errorMessage) {
        TestCodeResponse errorResult = new TestCodeResponse();
        errorResult.setSuccess(false);
        errorResult.setError(errorMessage);
        return errorResult;
    }

    private void cleanupTempDirectory(Path tempDir) {
        try {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            log.info("Cleaned up temporary directory: {}", tempDir);
        } catch (IOException e) {
            log.warn("Failed to cleanup temporary directory: {}", tempDir, e);
        }
    }

    private void setupGradleProject(Path projectDir, TestCodeRequest testExecution) throws IOException {
        // Create optimized build.gradle
        String buildGradle = """
                plugins {
                    id 'java'
                    id 'application'
                    id 'com.adarshr.test-logger' version '4.0.0'
                }
                
                group = 'com.test'
                version = '1.0.0'
                
                repositories {
                    mavenCentral()
                }
                
                dependencies {
                    implementation 'org.junit.jupiter:junit-jupiter-api:5.10.0'
                    testImplementation 'org.junit.jupiter:junit-jupiter-engine:5.10.0'
                    testImplementation 'org.junit.jupiter:junit-jupiter-params:5.10.0'
                    testImplementation 'org.mockito:mockito-core:5.5.0'
                    testImplementation 'org.assertj:assertj-core:3.27.2'
                }
                
                java {
                    toolchain {
                        languageVersion = JavaLanguageVersion.of(21)
                    }
                }
                
                test {
                    useJUnitPlatform()
                    maxHeapSize = '1g'
                    maxParallelForks = Runtime.runtime.availableProcessors()
                    forkEvery = 100
                    testLogging {
                        events 'passed', 'skipped', 'failed'
                        exceptionFormat = 'short'
                    }
                }
                
                compileJava {
                    options.encoding = 'UTF-8'
                    options.incremental = true
                    options.fork = true
                    options.forkOptions.jvmArgs = ['-Xmx512m']
                }
                
                compileTestJava {
                    options.encoding = 'UTF-8'
                    options.incremental = true
                    options.fork = true
                    options.forkOptions.jvmArgs = ['-Xmx512m']
                }
                
                testlogger {
                    theme 'standard-parallel'
                    showExceptions true
                    showStackTraces true
                    showFullStackTraces false
                    showCauses true
                    slowThreshold 2000
                    showSummary true
                    showSimpleNames false
                    showPassed true
                    showSkipped true
                    showFailed true
                    showOnlySlow false
                    showStandardStreams false
                    showPassedStandardStreams true
                    showSkippedStandardStreams true
                    showFailedStandardStreams true
                    logLevel 'lifecycle'
                }
                """;

        Files.writeString(projectDir.resolve("build.gradle"), buildGradle);

        // Create source directories and files
        createSourceDirectories(projectDir);
        writeSourceFiles(projectDir, testExecution);
        writeTestFiles(projectDir, testExecution);
        createOptimizedGradleProperties(projectDir);
    }

    private void createSourceDirectories(Path projectDir) throws IOException {
        Files.createDirectories(projectDir.resolve("src/main/java"));
        Files.createDirectories(projectDir.resolve("src/test/java"));
    }

    private void writeSourceFiles(Path projectDir, TestCodeRequest testExecution) throws IOException {
        Path srcMainJava = projectDir.resolve("src/main/java");
        log.info("Writing {} source files to {}", testExecution.getSourceFiles().size(), srcMainJava);

        for (CodeFile sourceFile : testExecution.getSourceFiles()) {
            Path filePath = srcMainJava.resolve(sourceFile.getFileName());
            // Create parent directories if they don't exist
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, sourceFile.getContent());
            log.debug("Wrote source file: {}", sourceFile.getFileName());
        }
    }

    private void writeTestFiles(Path projectDir, TestCodeRequest testExecution) throws IOException {
        Path srcTestJava = projectDir.resolve("src/test/java");
        log.info("Writing {} test files to {}", testExecution.getTestFiles().size(), srcTestJava);

        for (CodeFile testFile : testExecution.getTestFiles()) {
            Path filePath = srcTestJava.resolve(testFile.getFileName());
            // Create parent directories if they don't exist
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, testFile.getContent());
            log.debug("Wrote test file: {}", testFile.getFileName());
        }
    }


    private void parseAndAddXmlReports(TestCodeResponse result, Path tempDir) {
        try {
            Path testResultsDir = tempDir.resolve("test-results/test");
            if (!Files.exists(testResultsDir)) {
                log.warn("Test results directory not found: {}", testResultsDir);
                return;
            }

            List<TestSuiteResult> testSuites = new ArrayList<>();

            Files.walk(testResultsDir)
                    .filter(path -> path.toString().endsWith(".xml"))
                    .forEach(xmlPath -> {
                        try {
                            TestSuiteResult testSuite = parseTestXmlReport(xmlPath);
                            if (testSuite != null) {
                                testSuites.add(testSuite);
                            }
                        } catch (Exception e) {
                            log.warn("Failed to parse XML report: {}", xmlPath, e);
                        }
                    });

            result.setTestSuites(testSuites);

        } catch (Exception e) {
            log.error("Failed to parse XML reports", e);
        }
    }

    private TestSuiteResult parseTestXmlReport(Path xmlPath) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlPath.toFile());

        Element testSuite = document.getDocumentElement();

        TestSuiteResult suiteResult = new TestSuiteResult();
        suiteResult.setName(testSuite.getAttribute("name"));
        suiteResult.setTotalTests(Integer.parseInt(testSuite.getAttribute("tests")));
        suiteResult.setFailures(Integer.parseInt(testSuite.getAttribute("failures")));
        suiteResult.setErrors(Integer.parseInt(testSuite.getAttribute("errors")));

        // Calculate skipped tests if attribute exists
        String skippedAttr = testSuite.getAttribute("skipped");
        suiteResult.setSkipped(skippedAttr.isEmpty() ? 0 : Integer.parseInt(skippedAttr));

        // Parse execution time
        String timeAttr = testSuite.getAttribute("time");
        suiteResult.setExecutionTime(timeAttr.isEmpty() ? 0.0 : Double.parseDouble(timeAttr));

        // Parse individual test cases
        List<TestCaseResult> testCases = new ArrayList<>();
        NodeList testCaseNodes = document.getElementsByTagName("testcase");

        for (int i = 0; i < testCaseNodes.getLength(); i++) {
            Element testCase = (Element) testCaseNodes.item(i);
            TestCaseResult testCaseResult = parseTestCase(testCase);
            testCases.add(testCaseResult);
        }

        suiteResult.setTestCases(testCases);
        return suiteResult;
    }

    private TestCaseResult parseTestCase(Element testCase) {
        TestCaseResult result = new TestCaseResult();

        result.setClassName(testCase.getAttribute("classname"));
        result.setMethodName(testCase.getAttribute("name"));

        String timeAttr = testCase.getAttribute("time");
        result.setExecutionTime(timeAttr.isEmpty() ? 0.0 : Double.parseDouble(timeAttr));

        // Check for failures
        NodeList failures = testCase.getElementsByTagName("failure");
        if (failures.getLength() > 0) {
            Element failure = (Element) failures.item(0);
            result.setStatus("FAILED");
            result.setFailureMessage(failure.getAttribute("message"));
            result.setStackTrace(failure.getTextContent());
            return result;
        }

        // Check for errors
        NodeList errors = testCase.getElementsByTagName("error");
        if (errors.getLength() > 0) {
            Element error = (Element) errors.item(0);
            result.setStatus("ERROR");
            result.setErrorMessage(error.getAttribute("message"));
            result.setStackTrace(error.getTextContent());
            return result;
        }

        // Check for skipped tests
        NodeList skipped = testCase.getElementsByTagName("skipped");
        if (skipped.getLength() > 0) {
            result.setStatus("SKIPPED");
            return result;
        }

        // If no failure, error, or skip elements, the test passed
        result.setStatus("PASSED");
        return result;
    }

    private void createOptimizedGradleProperties(Path projectDir) throws IOException {
        String gradleProps = """
                org.gradle.daemon=true
                org.gradle.parallel=true
                org.gradle.caching=true
                org.gradle.configureondemand=true
                org.gradle.configuration-cache=true
                org.gradle.build-cache=true
                org.gradle.workers.max=4
                org.gradle.jvmargs=-Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dfile.encoding=UTF-8
                org.gradle.console=rich
                org.gradle.logging.level=lifecycle
                """;
        Files.writeString(projectDir.resolve("gradle.properties"), gradleProps);
    }


    private TestCodeResponse executeGradleTestWithXmlReport(String uuid) throws IOException, InterruptedException {
        log.info("Executing Gradle test with XML reports in Docker container for UUID: {}", uuid);

        ProcessBuilder dockerExec = new ProcessBuilder(
                "docker", "exec",
                "-e", "GRADLE_USER_HOME=" + GRADLE_USER_HOME,
                "-e", "TERM=xterm-256color", // Enable terminal colors
                "-t", // Allocate a pseudo-TTY for better formatting
                PERSISTENT_CONTAINER_NAME,
                "sh", "-c",
                "cd /workspace/" + uuid + " && " +
                        "gradle test " +
                        "--daemon " +
                        "--parallel " +
                        "--build-cache " +
                        "--configuration-cache " +
                        "--console=rich " + // Use rich console output instead of plain
                        "--info"
        );

        // Capture both output streams separately for better logging
        Process runProcess = dockerExec.start();

        boolean finished = runProcess.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!finished) {
            runProcess.destroyForcibly();
            throw new RuntimeException("Docker process timed out after " + TIMEOUT_SECONDS + " seconds");
        }

        String output = readProcessOutput(runProcess);
        String errors = readProcessErrors(runProcess);
        int exitCode = runProcess.exitValue();

        logExecutionResults(exitCode, output, errors, uuid);

        return createTestResult(exitCode, output, errors);
    }

    private void copyTestResultsFromContainer(Path tempDir, String uuid) throws IOException, InterruptedException {
        log.info("Copying test results from container back to host for UUID: {}", uuid);
        ProcessBuilder copyResults = new ProcessBuilder(
                "docker", "cp",
                PERSISTENT_CONTAINER_NAME + ":/workspace/" + uuid + "/build/test-results/.",
                tempDir.resolve("test-results").toString()
        );
        Process copyProcess = copyResults.start();
        copyProcess.waitFor();

        // Also copy reports directory if it exists
        ProcessBuilder copyReports = new ProcessBuilder(
                "docker", "cp",
                PERSISTENT_CONTAINER_NAME + ":/workspace/" + uuid + "/build/reports/.",
                tempDir.resolve("reports").toString()
        );
        copyReports.start().waitFor();
    }

    private void copyTestResultsFromContainer(Path tempDir) throws IOException, InterruptedException {
        log.info("Copying test results from container back to host");
        ProcessBuilder copyResults = new ProcessBuilder(
                "docker", "cp",
                PERSISTENT_CONTAINER_NAME + ":/workspace/build/test-results/.",
                tempDir.resolve("test-results").toString()
        );
        Process copyProcess = copyResults.start();
        copyProcess.waitFor();

        // Also copy reports directory if it exists
        ProcessBuilder copyReports = new ProcessBuilder(
                "docker", "cp",
                PERSISTENT_CONTAINER_NAME + ":/workspace/build/reports/.",
                tempDir.resolve("reports").toString()
        );
        copyReports.start().waitFor();
    }

    private void cleanupContainerWorkspace(String uuid) {
        try {
            log.info("Cleaning up container workspace for UUID: {}", uuid);
            ProcessBuilder cleanupCommand = new ProcessBuilder(
                    "docker", "exec", PERSISTENT_CONTAINER_NAME,
                    "sh", "-c", "rm -rf /workspace/" + uuid
            );
            Process cleanupProcess = cleanupCommand.start();
            cleanupProcess.waitFor();
            log.info("Successfully cleaned up container workspace for UUID: {}", uuid);
        } catch (Exception e) {
            log.warn("Failed to cleanup container workspace for UUID: {}", uuid, e);
        }
    }

    private void logExecutionResults(int exitCode, String output, String errors, String uuid) {
        log.info("=== Gradle Execution Summary for UUID: {} ===", uuid);
        log.info("Exit code: {}", exitCode);
        log.info("Docker/Gradle standard output:\n{}", output);
        if (!errors.isEmpty()) {
            log.warn("Docker/Gradle error output:\n{}", errors);
        }
    }
}

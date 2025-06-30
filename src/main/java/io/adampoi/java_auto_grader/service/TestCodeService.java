package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.model.enums.BuildTool;
import io.adampoi.java_auto_grader.model.request.TestCodeRequest;
import io.adampoi.java_auto_grader.model.response.TestCodeResponse;
import io.adampoi.java_auto_grader.model.type.CompilationError;
import io.adampoi.java_auto_grader.model.type.ProcessResult;
import io.adampoi.java_auto_grader.util.DockerContainerManager;
import io.adampoi.java_auto_grader.util.TestReportParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class TestCodeService {

    private static final int TIMEOUT_SECONDS = 300;
    private final ConcurrentMap<BuildTool, ContainerInstance> containers = new ConcurrentHashMap<>();

    private final DockerContainerManager dockerManager;
    private final TestReportParser reportParser;
    private final ProjectSetupService projectSetupService;

    private void initializeGradleDaemon() throws IOException, InterruptedException {
        log.info("Pre-initializing Gradle daemon in container");

        ProcessBuilder warmupCommand = new ProcessBuilder(
                "docker", "exec", "gradle-sandbox",
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

    public TestCodeResponse runTestCode(TestCodeRequest request) {
        String uuid = java.util.UUID.randomUUID().toString();
        Path tempDir = null;

        try {
            BuildTool buildTool = determineBuildTool(request);
            ContainerInstance container = ensureContainerReady(buildTool);
            initializeGradleDaemon();

            tempDir = createTempDirectory(uuid);
            String workspace = "/workspace/" + uuid;

            projectSetupService.setupProject(tempDir, request, buildTool);

            dockerManager.copyToContainer(container.getName(), tempDir, workspace);


            ProcessResult result = executeBuildCommand(container, workspace, buildTool);
            copyTestResults(container, workspace, tempDir);

            TestCodeResponse response = createResponse(result, tempDir);

            if (!result.isSuccess() && result.getOutput().contains("error:")) {
                List<CompilationError> compilationErrors = parseGradleCompilationErrors(result.getOutput());
                log.info("Parsed {} compilation errors", compilationErrors.size());
                compilationErrors.stream().forEach(error ->
                        log.error("Compilation error in {} at line {}: {}. code snippet {} , pointer {}", error.getErrorFile(), error.getLine(), error.getErrorMessage(), error.getCodeSnippet(), error.getPointer())
                );
                response.setCompilationErrors(compilationErrors);
            }

            return response;

        } catch (Exception e) {
            log.error("Test execution failed for UUID: {}", uuid, e);
            return createErrorResponse("Test execution failed: " + e.getMessage());
        } finally {
            cleanupResources(tempDir, uuid);
        }
    }


    private BuildTool determineBuildTool(TestCodeRequest request) {
        if (request.getBuildTool() == null || request.getBuildTool().isEmpty()) {
            log.warn("No build tool specified, defaulting to Gradle");
            return BuildTool.GRADLE;
        }
        try {
            return BuildTool.valueOf(request.getBuildTool().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid build tool specified: {}, defaulting to Gradle", request.getBuildTool());
            return BuildTool.GRADLE;
        }
    }

    private ContainerInstance ensureContainerReady(BuildTool buildTool) throws IOException, InterruptedException {
        ContainerInstance container = containers.computeIfAbsent(buildTool, bt -> {
            String containerName = bt.name().toLowerCase() + "-sandbox";
            return new ContainerInstance(containerName, bt);
        });

        if (!dockerManager.isContainerRunning(container.getName())) {
            log.info("Starting {} container: {}", buildTool, container.getName());
            dockerManager.startContainer(container.getName(), buildTool);
        }

        return container;
    }

    private Path createTempDirectory(String uuid) throws IOException {
        Path tempDir = Files.createTempDirectory("gradle-test-" + uuid);
        log.info("Created temporary directory for UUID {}: {}", uuid, tempDir);
        return tempDir;
    }

    private ProcessResult executeBuildCommand(ContainerInstance container, String workspace, BuildTool buildTool)
            throws IOException, InterruptedException {
        String command = buildCommand(buildTool, workspace);
        log.info("Executing build command in container {}: {}", container.getName(), command);
        return dockerManager.executeCommand(container.getName(), command, TIMEOUT_SECONDS);
    }

    private String buildCommand(BuildTool buildTool, String workspace) {
        String baseCommand = String.format("cd %s && ", workspace);
        switch (buildTool) {
            case GRADLE:
                return baseCommand + "gradle test" +
//                        "-x compileJava"+
                        " --rerun-tasks " +
                        "--daemon --parallel --build-cache --configuration-cache " +
                        "--console=plain";
            case MAVEN:
                return baseCommand + "mvn test -Dmaven.repo.local=/workspace/.m2/repository";
            default:
                throw new IllegalArgumentException("Unsupported build tool: " + buildTool);
        }
    }

    private void copyTestResults(ContainerInstance container, String workspace, Path tempDir) {
        try {
            String testResultsGradle = workspace + "/build/test-results/test";
            String testResultsMaven = workspace + "/target/surefire-reports";

            dockerManager.copyFromContainer(container.getName(), testResultsGradle, tempDir.resolve("gradle-results"));
            dockerManager.copyFromContainer(container.getName(), testResultsMaven, tempDir.resolve("maven-results"));
        } catch (Exception e) {
            log.warn("Failed to copy test results", e);
        }
    }

    private TestCodeResponse createResponse(ProcessResult result, Path tempDir) {
        TestCodeResponse response = new TestCodeResponse();
        response.setSuccess(result.isSuccess());
        response.setOutput(result.getOutput());
        response.setError(result.getErrors());
        response.setExecutionTime(result.getExecutionTime());
        response.setExitCode(result.getExitCode());

        var testSuites = reportParser.parseTestReports(tempDir.resolve("gradle-results"));
        if (testSuites.isEmpty()) {
            testSuites = reportParser.parseTestReports(tempDir.resolve("maven-results"));
        }
        response.setTestSuites(testSuites);

        return response;
    }

    private TestCodeResponse createErrorResponse(String errorMessage) {
        TestCodeResponse response = new TestCodeResponse();
        response.setSuccess(false);
        response.setError(errorMessage);
        response.setExitCode(-1);
        return response;
    }

    private void cleanupResources(Path tempDir, String uuid) {
        if (tempDir != null) {
            try {
                Files.walk(tempDir)
                        .sorted((a, b) -> b.toString().length() - a.toString().length())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                log.warn("Failed to delete: {}", path, e);
                            }
                        });
            } catch (Exception e) {
                log.warn("Failed to cleanup temp directory: {}", tempDir, e);
            }
        }

        containers.values().forEach(container ->
                dockerManager.cleanupWorkspace(container.getName(), "/workspace/" + uuid)
        );
    }

    private List<CompilationError> parseGradleCompilationErrors(String buildOutput) {
        String[] lines = buildOutput.split("\n");
        List<CompilationError> errors = new ArrayList<>();

        boolean inCompileJavaSection = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            // Start processing when we hit the compileJava FAILED task
            if (line.contains("> Task :compileJava FAILED")) {
                inCompileJavaSection = true;
                continue;
            }

            // Stop processing when we hit certain sections that mark the end of the error list
            if (inCompileJavaSection && (line.startsWith("FAILURE:") ||
                    line.startsWith("* What went wrong:") ||
                    line.startsWith("[Incubating]") ||
                    line.matches("\\d+ errors?"))) {
                break;
            }

            // Only process errors in the compileJava section
            if (inCompileJavaSection && line.contains(".java:") && line.contains(": error:")) {
                String fileName = extractFileNameFromGradleError(line);
                int lineNumber = extractLineNumberFromGradleError(line);
                String errorMessage = extractErrorMessageFromGradleError(line);

                // Get the code snippet (next line after error)
                String codeSnippet = null;
                String pointer = null;

                // Check bounds before accessing next line
                if (i + 1 < lines.length) {
                    String nextLine = lines[i + 1];
                    if (!nextLine.isEmpty() &&
                            !nextLine.contains(".java:") &&
                            !nextLine.contains("FAILURE:") &&
                            !nextLine.matches("\\d+ errors?") &&
                            !nextLine.startsWith("[Incubating]")) {
                        codeSnippet = nextLine;
                        i++; // Move to next line

                        // Get the pointer line (line with ^)
                        if (i + 1 < lines.length && lines[i + 1].contains("^")) {
                            pointer = lines[i + 1];
                            i++; // Move to next line
                        }
                    }
                }

                errors.add(CompilationError.builder()
                        .errorFile(fileName)
                        .line(lineNumber - 1) // compensate package code at the top
                        .errorMessage(errorMessage)
                        .codeSnippet(codeSnippet)
                        .pointer(pointer)
                        .build());

                // Continue processing to find more errors - don't break here
            }
        }

        log.debug("Parsed {} compilation errors from build output", errors.size());
        return errors;
    }

    private String extractFileNameFromGradleError(String line) {
        if (line.contains("/workspace/") && line.contains(".java:")) {
            // Extract filename from path like: /workspace/uuid/src/main/java/workspace/Main.java:8: error:
            int fileNameStart = line.lastIndexOf("/") + 1;
            int colonIndex = line.indexOf(":", fileNameStart);
            if (colonIndex != -1 && fileNameStart < colonIndex) {
                return line.substring(fileNameStart, colonIndex);
            }
        }
        return "unknown";
    }

    private int extractLineNumberFromGradleError(String line) {
        try {
            // Pattern: filename.java:lineNumber: error:
            if (line.contains(".java:")) {
                int javaIndex = line.indexOf(".java:");
                int nextColon = line.indexOf(":", javaIndex + 6);
                if (nextColon != -1) {
                    String lineNumberStr = line.substring(javaIndex + 6, nextColon).trim();
                    return Integer.parseInt(lineNumberStr);
                }
            }
        } catch (NumberFormatException e) {
            log.warn("Failed to parse line number from: {}", line);
        }
        return -1;
    }

    private String extractErrorMessageFromGradleError(String line) {
        int errorIndex = line.indexOf(": error:");
        if (errorIndex != -1) {
            String fullMessage = line.substring(errorIndex + 8).trim(); // +8 to skip ": error:"

            // If the message is just a symbol like "';' expected",
            // try to create a more descriptive message
            if (fullMessage.length() < 20) { // Short messages are likely just symbols
                if (fullMessage.contains("';'")) {
                    return "Missing semicolon (;) at end of statement";
                } else if (fullMessage.contains("')'")) {
                    return "Missing closing parenthesis )";
                } else if (fullMessage.contains("'('")) {
                    return "Missing opening parenthesis (";
                } else if (fullMessage.contains("'}'")) {
                    return "Missing closing brace }";
                } else if (fullMessage.contains("'{'")) {
                    return "Missing opening brace {";
                }
            }

            return fullMessage;
        }
        return line.trim();
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class ContainerInstance {
        private String name;
        private BuildTool buildTool;
    }
}
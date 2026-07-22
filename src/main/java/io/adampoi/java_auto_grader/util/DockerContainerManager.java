package io.adampoi.java_auto_grader.util;

import io.adampoi.java_auto_grader.model.enums.BuildTool;
import io.adampoi.java_auto_grader.model.type.ProcessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class DockerContainerManager {

    private static final int DEFAULT_TIMEOUT_SECONDS = 180;
    private static final String DOCKER_COMMAND = "docker";
    private static final String GRADLE_USER_HOME = "/workspace/.gradle";
    private static final String MAVEN_USER_HOME = "/workspace/.m2";

    public boolean isContainerRunning(String containerName) throws IOException, InterruptedException {
        ProcessBuilder checkContainer = new ProcessBuilder(
                DOCKER_COMMAND, "inspect", "--format={{.State.Running}}", containerName
        );
        Process checkProcess = checkContainer.start();
        checkProcess.waitFor();

        if (checkProcess.exitValue() == 0) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(checkProcess.getInputStream(), StandardCharsets.UTF_8))) {
                String isRunning = reader.readLine();
                return "true".equals(isRunning);
            }
        }
        return false;
    }

    public void startContainer(String containerName, BuildTool buildTool) throws IOException, InterruptedException {
        removeContainerIfExists(containerName);

        String[] dockerRunCmd = createDockerRunCommand(containerName, buildTool);
        ProcessBuilder startContainer = new ProcessBuilder(dockerRunCmd);
        startContainer.redirectErrorStream(true);
        Process startProcess = startContainer.start();

        int exitCode = startProcess.waitFor();
        if (exitCode != 0) {
            String errorOutput = ProcessUtils.readOutput(startProcess);
            throw new RuntimeException("Failed to start Docker container: " + errorOutput);
        }

        Thread.sleep(1000); // Allow container to fully start
        setupBuildToolHome(containerName, buildTool);
    }


    public void copyToContainer(String containerName, Path source, String destination)
            throws IOException, InterruptedException {
        ProcessResult prepareResult = executeCommand(
                containerName,
                "mkdir -p " + shellQuote(destination) + " && test -w " + shellQuote(destination),
                15
        );
        if (!prepareResult.isSuccess()) {
            throw new RuntimeException("Container workspace is not writable: "
                    + firstNonBlank(prepareResult.getErrors(), prepareResult.getOutput()));
        }

        ProcessBuilder copyCommand = new ProcessBuilder(
                DOCKER_COMMAND, "cp", source.toString() + "/.",
                containerName + ":" + destination
        );
        copyCommand.redirectErrorStream(true);
        Process copyProcess = copyCommand.start();
        String commandOutput = ProcessUtils.readOutput(copyProcess);
        int exitCode = copyProcess.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to copy files to container " + containerName
                    + " at " + destination + ": " + commandOutput.strip());
        }
    }

    public void copyFromContainer(String containerName, String source, Path destination)
            throws IOException, InterruptedException {
        ProcessBuilder copyCommand = new ProcessBuilder(
                DOCKER_COMMAND, "cp", containerName + ":" + source, destination.toString()
        );
        copyCommand.redirectErrorStream(true);
        Process copyProcess = copyCommand.start();
        String commandOutput = ProcessUtils.readOutput(copyProcess);
        int exitCode = copyProcess.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to copy files from container " + containerName
                    + " at " + source + ": " + commandOutput.strip());
        }
    }

    public boolean isContainerUsable(String containerName, BuildTool buildTool)
            throws IOException, InterruptedException {
        String requiredCommand = buildTool == BuildTool.GRADLE ? "gradle" : "mvn";
        ProcessResult result = executeCommand(
                containerName,
                "test -d /workspace && test -w /workspace && command -v " + requiredCommand + " >/dev/null 2>&1",
                15
        );
        if (!result.isSuccess()) {
            log.warn("Container {} failed readiness check: {}", containerName,
                    firstNonBlank(result.getErrors(), result.getOutput()));
        }
        return result.isSuccess();
    }

    public ProcessResult executeCommand(String containerName, String command, int timeoutSeconds)
            throws IOException, InterruptedException {
        ProcessBuilder dockerExec = new ProcessBuilder(
                DOCKER_COMMAND, "exec", "-t", containerName, "sh", "-c", command
        );
        long startTime = System.currentTimeMillis();
        Process process = dockerExec.start();
        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        long executionTime = System.currentTimeMillis() - startTime;

        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Command timed out after " + timeoutSeconds + " seconds (executed for " + executionTime + "ms)");
        }

        String output = ProcessUtils.readOutput(process);
        String errors = ProcessUtils.readErrors(process);
        return new ProcessResult(process.exitValue(), output, errors, executionTime);
    }

    public void cleanupWorkspace(String containerName, String workspace) {
        try {
            ProcessBuilder cleanupCommand = new ProcessBuilder(
                    DOCKER_COMMAND, "exec", containerName, "sh", "-c", "rm -rf " + workspace
            );
            Process cleanupProcess = cleanupCommand.start();
            cleanupProcess.waitFor();
            log.info("Cleaned up workspace: {}", workspace);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while cleaning workspace: {}", workspace, e);
        } catch (IOException e) {
            log.warn("Failed to cleanup workspace: {}", workspace, e);
        }
    }

    private void removeContainerIfExists(String containerName) throws IOException, InterruptedException {
        ProcessBuilder removeContainer = new ProcessBuilder(DOCKER_COMMAND, "rm", "-f", containerName);
        removeContainer.start().waitFor();
    }

    private String[] createDockerRunCommand(String containerName, BuildTool buildTool) {
        String userHome = buildTool == BuildTool.GRADLE ? GRADLE_USER_HOME : MAVEN_USER_HOME;
        String imageName = buildTool == BuildTool.GRADLE ? "gradle" : "maven-sandbox";

        return new String[]{
                DOCKER_COMMAND, "run", "-d", "--name", containerName,
                "-v", "/tmp:/workspace",
                "--memory=3g", "--cpus=4", "--shm-size=1g",
                "-e", "GRADLE_OPTS=-Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200",
                "-e", "GRADLE_USER_HOME=" + userHome,
                "-e", "JAVA_OPTS=-Xmx2g",
                imageName, "tail", "-f", "/dev/null"
        };
    }

    private void setupBuildToolHome(String containerName, BuildTool buildTool)
            throws IOException, InterruptedException {
        String userHome = buildTool == BuildTool.GRADLE ? GRADLE_USER_HOME : MAVEN_USER_HOME;
        ProcessBuilder createHome = new ProcessBuilder(
                DOCKER_COMMAND, "exec", containerName,
                "sh", "-c", "mkdir -p " + userHome + " && chmod 755 " + userHome
        );
        createHome.start().waitFor();
    }

    private String shellQuote(String value) {
        return "'" + value.replace("'", "'\\''") + "'";
    }

    private String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second == null ? "" : second;
    }
}

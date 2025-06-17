package io.adampoi.java_auto_grader.util;

import io.adampoi.java_auto_grader.model.enums.BuildTool;
import io.adampoi.java_auto_grader.model.type.ProcessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class DockerContainerManager {

    private static final int DEFAULT_TIMEOUT_SECONDS = 60;
    private static final String GRADLE_USER_HOME = "/workspace/.gradle";
    private static final String MAVEN_USER_HOME = "/workspace/.m2";

    public boolean isContainerRunning(String containerName) throws IOException, InterruptedException {
        ProcessBuilder checkContainer = new ProcessBuilder(
                "docker", "inspect", "--format={{.State.Running}}", containerName
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
        ProcessBuilder copyCommand = new ProcessBuilder(
                "docker", "cp", source.toString() + "/.",
                containerName + ":" + destination
        );
        Process copyProcess = copyCommand.start();
        int exitCode = copyProcess.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to copy files to container");
        }
    }

    public void copyFromContainer(String containerName, String source, Path destination)
            throws IOException, InterruptedException {
        ProcessBuilder copyCommand = new ProcessBuilder(
                "docker", "cp", containerName + ":" + source, destination.toString()
        );
        Process copyProcess = copyCommand.start();
        copyProcess.waitFor(); // Don't fail if source doesn't exist
    }

    public ProcessResult executeCommand(String containerName, String command, int timeoutSeconds)
            throws IOException, InterruptedException {
        ProcessBuilder dockerExec = new ProcessBuilder(
                "docker", "exec", "-t", containerName, "sh", "-c", command
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
//        int exitCode = process.exitValue();

//        logExecutionResults(exitCode, output, errors);

        return new ProcessResult(process.exitValue(), output, errors, executionTime);
    }

    private void logExecutionResults(int exitCode, String output, String errors) {
        log.info("=== Gradle Execution Summary");
        log.info("Exit code: {}", exitCode);
        log.info("Docker/Gradle standard output:\n{}", output);
        if (!errors.isEmpty()) {
            log.warn("Docker/Gradle error output:\n{}", errors);
        }
    }

    public void cleanupWorkspace(String containerName, String workspace) {
        try {
            ProcessBuilder cleanupCommand = new ProcessBuilder(
                    "docker", "exec", containerName, "sh", "-c", "rm -rf " + workspace
            );
            Process cleanupProcess = cleanupCommand.start();
            cleanupProcess.waitFor();
            log.info("Cleaned up workspace: {}", workspace);
        } catch (Exception e) {
            log.warn("Failed to cleanup workspace: {}", workspace, e);
        }
    }

    private void removeContainerIfExists(String containerName) throws IOException, InterruptedException {
        ProcessBuilder removeContainer = new ProcessBuilder("docker", "rm", "-f", containerName);
        removeContainer.start().waitFor();
    }

    private String[] createDockerRunCommand(String containerName, BuildTool buildTool) {
        String userHome = buildTool == BuildTool.GRADLE ? GRADLE_USER_HOME : MAVEN_USER_HOME;
        String imageName = buildTool == BuildTool.GRADLE ? "gradle" : "maven-sandbox";

        return new String[]{
                "docker", "run", "-d", "--name", containerName,
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
                "docker", "exec", containerName,
                "sh", "-c", "mkdir -p " + userHome + " && chmod 755 " + userHome
        );
        createHome.start().waitFor();
    }
}
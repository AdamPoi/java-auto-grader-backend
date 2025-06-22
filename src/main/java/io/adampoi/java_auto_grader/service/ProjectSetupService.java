package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.model.enums.BuildTool;
import io.adampoi.java_auto_grader.model.request.TestCodeRequest;
import io.adampoi.java_auto_grader.model.type.CodeFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Service
@Slf4j
public class ProjectSetupService {

    public void setupProject(Path projectDir, TestCodeRequest request, BuildTool buildTool) throws IOException {
        switch (buildTool) {
            case GRADLE:
                setupGradleProject(projectDir, request);
                break;
            case MAVEN:
                setupMavenProject(projectDir, request);
                break;
        }
        createSourceDirectories(projectDir);
        writeSourceFiles(projectDir, request);
        writeTestFiles(projectDir, request);
    }

    private void setupGradleProject(Path projectDir, TestCodeRequest request) throws IOException {
        writeGradleBuildFile(projectDir);
        writeGradleProperties(projectDir);
//        writeGradleSettings(projectDir);
    }

    private void setupMavenProject(Path projectDir, TestCodeRequest request) throws IOException {
        writeMavenPomFile(projectDir);
        createMavenDirectoryStructure(projectDir);
    }

    private void writeSourceFiles(Path projectDir, TestCodeRequest request) throws IOException {
        Path srcMainJava = projectDir.resolve("src/main/java");
        log.info("Writing {} source files to {}", request.getSourceFiles().size(), srcMainJava);

        for (CodeFile sourceFile : request.getSourceFiles()) {
            Path filePath = srcMainJava.resolve(sourceFile.getFileName());
            // Create parent directories if they don't exist
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, sourceFile.getContent());
        }
    }

    private void writeTestFiles(Path projectDir, TestCodeRequest request) throws IOException {
        Path srcTestJava = projectDir.resolve("src/test/java");
        log.info("Writing {} test files to {}", request.getTestFiles().size(), srcTestJava);

        for (CodeFile testFile : request.getTestFiles()) {
            Path filePath = srcTestJava.resolve(testFile.getFileName());
            // Create parent directories if they don't exist
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, testFile.getContent());
        }
    }

    private Path createSourceDirectories(Path projectDir) throws IOException {
        Path srcDir = projectDir.resolve("src");
        Files.createDirectories(srcDir.resolve("main/java"));
        Files.createDirectories(srcDir.resolve("test/java"));
        return srcDir;
    }

    private void writeGradleBuildFile(Path projectDir) throws IOException {
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
                     implementation 'com.github.javaparser:javaparser-core:3.25.10'
                     implementation 'com.github.javaparser:javaparser-symbol-solver-core:3.25.10'
                     implementation 'com.github.javaparser:javaparser-core-serialization:3.25.10'
                     testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'
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

        writeFile(projectDir.resolve("build.gradle"), buildGradle);
    }

    private void writeGradleProperties(Path projectDir) throws IOException {
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

    private void writeGradleSettings(Path projectDir) throws IOException {
        writeFile(projectDir.resolve("settings.gradle"), "rootProject.name = 'auto-grader-project'");
    }

    private void writeMavenPomFile(Path projectDir) throws IOException {
        String pomXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                         http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                
                    <groupId>com.example</groupId>
                    <artifactId>auto-grader-project</artifactId>
                    <version>1.0.0</version>
                    <packaging>jar</packaging>
                
                    <properties>
                        <maven.compiler.source>11</maven.compiler.source>
                        <maven.compiler.target>11</maven.compiler.target>
                        <junit.version>5.9.2</junit.version>
                    </properties>
                
                    <dependencies>
                        <dependency>
                            <groupId>org.junit.jupiter</groupId>
                            <artifactId>junit-jupiter</artifactId>
                            <version>${junit.version}</version>
                            <scope>test</scope>
                        </dependency>
                        <dependency>
                            <groupId>org.assertj</groupId>
                            <artifactId>assertj-core</artifactId>
                            <version>3.24.2</version>
                            <scope>test</scope>
                        </dependency>
                    </dependencies>
                
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-surefire-plugin</artifactId>
                                <version>3.0.0-M9</version>
                                <configuration>
                                    <includes>
                                        <include>**/*Test.java</include>
                                        <include>**/Test*.java</include>
                                    </includes>
                                    <reportsDirectory>${project.build.directory}/surefire-reports</reportsDirectory>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """;

        writeFile(projectDir.resolve("pom.xml"), pomXml);
    }

    private void createMavenDirectoryStructure(Path projectDir) throws IOException {
        Files.createDirectories(projectDir.resolve("src/main/java"));
        Files.createDirectories(projectDir.resolve("src/test/java"));
        Files.createDirectories(projectDir.resolve("src/main/resources"));
        Files.createDirectories(projectDir.resolve("src/test/resources"));
    }

    private void writeFile(Path filePath, String content) throws IOException {
        Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
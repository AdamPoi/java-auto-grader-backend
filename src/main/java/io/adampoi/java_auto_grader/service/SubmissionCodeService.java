package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.domain.SubmissionCode;
import io.adampoi.java_auto_grader.model.dto.SubmissionCodeDTO;
import io.adampoi.java_auto_grader.model.request.RunCodeRequest;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.model.response.RunCodeResponse;
import io.adampoi.java_auto_grader.model.type.CodeFile;
import io.adampoi.java_auto_grader.model.type.CompilationError;
import io.adampoi.java_auto_grader.repository.SubmissionCodeRepository;
import io.adampoi.java_auto_grader.repository.SubmissionRepository;
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
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class SubmissionCodeService {

    private static final String JAVA_ERROR_MARKER = ".java:";

    private final SubmissionCodeRepository submissionCodeRepository;
    private final SubmissionRepository submissionRepository;

    public SubmissionCodeService(final SubmissionCodeRepository submissionCodeRepository,
                                 final SubmissionRepository submissionRepository) {
        this.submissionCodeRepository = submissionCodeRepository;
        this.submissionRepository = submissionRepository;
    }

    public static SubmissionCode mapToEntity(final SubmissionCodeDTO submissionCodeDTO, final SubmissionCode submissionCode) {
        if (submissionCodeDTO.getFileName() != null) {
            submissionCode.setFileName(submissionCodeDTO.getFileName());
        }
        if (submissionCodeDTO.getSourceCode() != null) {
            submissionCode.setSourceCode(submissionCodeDTO.getSourceCode());
        }


//        if (submissionCodeDTO.getSubmission() != null) {
//            final Submission submission = submissionRepository.findById(submissionCodeDTO.getSubmission())
//                    .orElseThrow(() -> new EntityNotFoundException("Submission not found"));
//            submissionCode.setSubmission(submission);
//        }

        return submissionCode;
    }

    public static SubmissionCodeDTO mapToDTO(final SubmissionCode submissionCode, final SubmissionCodeDTO submissionCodeDTO) {
        submissionCodeDTO.setId(submissionCode.getId());
        submissionCodeDTO.setFileName(submissionCode.getFileName());
        submissionCodeDTO.setSourceCode(submissionCode.getSourceCode());
//        submissionCodeDTO
//                .setSubmission(submissionCode.getSubmission() == null ? null : submissionCode.getSubmission().getId());
//        submissionCodeDTO.setCreatedAt(submissionCode.getCreatedAt());
        return submissionCodeDTO;
    }

    public PageResponse<SubmissionCodeDTO> findAll(QueryFilter<SubmissionCode> filter, Pageable pageable) {
        final Page<SubmissionCode> page = submissionCodeRepository.findAll(filter, pageable);
        Page<SubmissionCodeDTO> dtoPage = new PageImpl<>(page.getContent()
                .stream()
                .map(submissioNCode -> mapToDTO(submissioNCode, new SubmissionCodeDTO()))
                .collect(Collectors.toList()),
                pageable, page.getTotalElements());
        return PageResponse.from(dtoPage);
    }

    public SubmissionCodeDTO get(final UUID submissionCodeId) {
        return submissionCodeRepository.findById(submissionCodeId)
                .map(submissionCode -> mapToDTO(submissionCode, new SubmissionCodeDTO()))
                .orElseThrow(() -> new EntityNotFoundException("SubmissionCode not found"));
    }

    public SubmissionCodeDTO create(final SubmissionCodeDTO submissionCodeDTO) {
        final SubmissionCode submissionCode = new SubmissionCode();
        mapToEntity(submissionCodeDTO, submissionCode);
        SubmissionCode savedSubmissionCode = submissionCodeRepository.save(submissionCode);
        return mapToDTO(savedSubmissionCode, new SubmissionCodeDTO());
    }

    public SubmissionCodeDTO update(final UUID submissionCodeId, final SubmissionCodeDTO submissionCodeDTO) {
        final SubmissionCode submissionCode = submissionCodeRepository.findById(submissionCodeId)
                .orElseThrow(() -> new EntityNotFoundException("SubmissionCode not found"));
        mapToEntity(submissionCodeDTO, submissionCode);
        SubmissionCode savedSubmissionCode = submissionCodeRepository.save(submissionCode);
        return mapToDTO(savedSubmissionCode, new SubmissionCodeDTO());
    }

    public void delete(final UUID submissionCodeId) {
        submissionCodeRepository.deleteById(submissionCodeId);
    }

    @SuppressWarnings("PMD.CloseResource") // System.out is process-owned and must remain open.
    public RunCodeResponse runCode(RunCodeRequest code) {
        long startTime = System.currentTimeMillis();

        try {
            File root = Files.createTempDirectory("java").toFile();

            List<File> sourceFiles = new ArrayList<>();
            for (CodeFile codeFile : code.getFiles()) {
                File sourceFile = new File(root, codeFile.getFileName());
                sourceFile.getParentFile().mkdirs();
                Files.write(sourceFile.toPath(), codeFile.getContent().getBytes(StandardCharsets.UTF_8));
                sourceFiles.add(sourceFile);
            }

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

            try (ByteArrayOutputStream compileErrors = new ByteArrayOutputStream()) {
                String[] sourceFilePaths = sourceFiles.stream()
                        .map(File::getPath)
                        .toArray(String[]::new);
                List<String> options = List.of("-proc:none", "-Xlint:-options");
                List<String> compilerArgs = new ArrayList<>(options);
                compilerArgs.addAll(Arrays.asList(sourceFilePaths));

                int compilationResult = compiler.run(null, null, compileErrors,
                        compilerArgs.toArray(String[]::new));

                if (compilationResult != 0) {
                    long executionTime = System.currentTimeMillis() - startTime;
                    String errorOutput = compileErrors.toString(StandardCharsets.UTF_8);
                    String filteredError = filterCompilationErrors(errorOutput);
                    List<CompilationError> compilationErrors = parseCompilationErrors(errorOutput);

                    return RunCodeResponse.builder()
                            .success(false)
                            .output(null)
                            .error(filteredError)
                            .exception(null)
                            .executionTime(executionTime)
                            .compilationErrors(compilationErrors)
                            .build();
                }
            }

            try (URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{root.toURI().toURL()});
                 ByteArrayOutputStream output = new ByteArrayOutputStream();
                 PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
                Class<?> studentClass = Class.forName(code.getMainClassName(), true, classLoader);
                PrintStream originalOutput = System.out;
                System.setOut(capturedOutput);
                try {
                    studentClass.getDeclaredMethod("main", String[].class)
                            .invoke(null, (Object) new String[0]);
                    long executionTime = System.currentTimeMillis() - startTime;
                    return RunCodeResponse.builder()
                            .success(true)
                            .output(output.toString(StandardCharsets.UTF_8))
                            .error(null)
                            .exception(null)
                            .executionTime(executionTime)
                            .build();
                } catch (ReflectiveOperationException exception) {
                    long executionTime = System.currentTimeMillis() - startTime;
                    return RunCodeResponse.builder()
                            .success(false)
                            .output(output.toString(StandardCharsets.UTF_8))
                            .error(null)
                            .exception(exception.getMessage())
                            .executionTime(executionTime)
                            .build();
                } finally {
                    capturedOutput.flush();
                    System.setOut(originalOutput);
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        } catch (IOException | RuntimeException | LinkageError e) {
            long executionTime = System.currentTimeMillis() - startTime;
            return RunCodeResponse.builder()
                    .success(false)
                    .output(null)
                    .error(null)
                    .exception(e.getMessage())
                    .executionTime(executionTime)
                    .build();
        }
    }

    private String filterCompilationErrors(String errorOutput) {
        String[] lines = errorOutput.split("\n");
        StringBuilder filteredErrors = new StringBuilder();

        for (String line : lines) {
            // Skip annotation processing warnings
            if (line.contains("Annotation processing is enabled") ||
                    line.contains("A future release of javac") ||
                    line.contains("unless at least one processor") ||
                    line.contains("Use -Xlint:-options") ||
                    line.contains("Use -proc:none")) {
                continue;
            }

            // Skip empty lines only if they're at the beginning or end
            if (line.isBlank()) {
                continue;
            }

            // Remove file path from error lines but keep all error content
            String processedLine = line;
            if (line.contains("/tmp/") && line.contains(JAVA_ERROR_MARKER)) {
                // Extract just the filename and error details
                int fileNameStart = line.lastIndexOf('/') + 1;
                int colonIndex = line.indexOf(':', fileNameStart);
                if (colonIndex != -1 && fileNameStart < colonIndex) {
                    String fileName = line.substring(fileNameStart, colonIndex);
                    String errorDetails = line.substring(colonIndex);
                    processedLine = fileName + errorDetails;
                }
            }

            // Add line to output
            if (filteredErrors.length() > 0) {
                filteredErrors.append('\n');
            }
            filteredErrors.append(processedLine);
        }

        return filteredErrors.toString();
    }

    private List<CompilationError> parseCompilationErrors(String errorOutput) {
        String[] lines = errorOutput.split("\n");
        List<CompilationError> errors = new ArrayList<>();

        int index = 0;
        while (index < lines.length) {
            String line = lines[index];

            // Skip annotation processing warnings
            if (line.contains("Annotation processing is enabled") ||
                    line.contains("A future release of javac") ||
                    line.contains("unless at least one processor") ||
                    line.contains("Use -Xlint:-options") ||
                    line.contains("Use -proc:none")) {
                index++;
                continue;
            }

            // Look for error pattern: filename:line: error: message
            if (line.contains(JAVA_ERROR_MARKER) && line.contains(": error:")) {
                String fileName = extractFileName(line);
                int lineNumber = extractLineNumber(line);
                String errorMessage = extractErrorMessage(line);

                // Look for code snippet and pointer in following lines
                String codeSnippet = null;
                String pointer = null;

                if (index + 1 < lines.length && !lines[index + 1].isEmpty() &&
                        !lines[index + 1].contains(JAVA_ERROR_MARKER)) {
                    codeSnippet = lines[index + 1];
                    index++; // Skip this line in next iteration
                }

                if (index + 1 < lines.length && lines[index + 1].contains("^")) {
                    pointer = lines[index + 1];
                    index++; // Skip this line in next iteration
                }

                errors.add(CompilationError.builder()
                        .errorFile(fileName)
                        .line(lineNumber)
                        .errorMessage(errorMessage)
                        .codeSnippet(codeSnippet)
                        .pointer(pointer)
                        .build());
            }
            index++;
        }

        return errors;
    }

    private String extractFileName(String line) {
        if (line.contains("/tmp/") && line.contains(JAVA_ERROR_MARKER)) {
            int fileNameStart = line.lastIndexOf('/') + 1;
            int colonIndex = line.indexOf(':', fileNameStart);
            return line.substring(fileNameStart, colonIndex);
        }
        return null;
    }

    private int extractLineNumber(String line) {
        try {
            String[] parts = line.split(":");
            for (int i = 0; i < parts.length - 1; i++) {
                if (parts[i].endsWith(".java") && i + 1 < parts.length) {
                    return Integer.parseInt(parts[i + 1].trim());
                }
            }
        } catch (NumberFormatException exception) {
            log.debug("Unable to parse compilation error line number from: {}", line, exception);
        }
        return -1;
    }

    private String extractErrorMessage(String line) {
        int errorIndex = line.indexOf(": error:");
        if (errorIndex != -1) {
            return line.substring(errorIndex + 8).trim(); // +8 to skip ": error:"
        }
        return line;
    }
}

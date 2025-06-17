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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
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

    private final SubmissionCodeRepository submissionCodeRepository;
    private final SubmissionRepository submissionRepository;

    public SubmissionCodeService(final SubmissionCodeRepository submissionCodeRepository,
                                 final SubmissionRepository submissionRepository) {
        this.submissionCodeRepository = submissionCodeRepository;
        this.submissionRepository = submissionRepository;
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

    private SubmissionCodeDTO mapToDTO(final SubmissionCode submissionCode, final SubmissionCodeDTO submissionCodeDTO) {
        submissionCodeDTO.setId(submissionCode.getId());
        submissionCodeDTO.setFileName(submissionCode.getFileName());
        submissionCodeDTO.setSourceCode(submissionCode.getSourceCode());
        submissionCodeDTO.setClassName(submissionCode.getClassName());
//        submissionCodeDTO
//                .setSubmission(submissionCode.getSubmission() == null ? null : submissionCode.getSubmission().getId());
//        submissionCodeDTO.setCreatedAt(submissionCode.getCreatedAt());
        submissionCodeDTO.setUpdatedAt(submissionCode.getUpdatedAt());
        return submissionCodeDTO;
    }

    private SubmissionCode mapToEntity(final SubmissionCodeDTO submissionCodeDTO, final SubmissionCode submissionCode) {
        if (submissionCodeDTO.getFileName() != null) {
            submissionCode.setFileName(submissionCodeDTO.getFileName());
        }
        if (submissionCodeDTO.getSourceCode() != null) {
            submissionCode.setSourceCode(submissionCodeDTO.getSourceCode());
        }

        if (submissionCodeDTO.getClassName() != null) {
            submissionCode.setClassName(submissionCodeDTO.getClassName());
        }
//        if (submissionCodeDTO.getSubmission() != null) {
//            final Submission submission = submissionRepository.findById(submissionCodeDTO.getSubmission())
//                    .orElseThrow(() -> new EntityNotFoundException("Submission not found"));
//            submissionCode.setSubmission(submission);
//        }
        if (submissionCodeDTO.getCreatedAt() != null) {
            submissionCode.setCreatedAt(submissionCodeDTO.getCreatedAt());
        }
        if (submissionCodeDTO.getUpdatedAt() != null) {
            submissionCode.setUpdatedAt(submissionCodeDTO.getUpdatedAt());
        }
        return submissionCode;
    }

    @SneakyThrows
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

            ByteArrayOutputStream compileErrors = new ByteArrayOutputStream();

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
                String errorOutput = compileErrors.toString();

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

            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{root.toURI().toURL()});
            Class<?> cls = Class.forName(code.getMainClassName(), true, classLoader);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream old = System.out;
            System.setOut(ps);

            try {
                cls.getDeclaredMethod("main", String[].class).invoke(null, (Object) new String[0]);

                long executionTime = System.currentTimeMillis() - startTime;
                return RunCodeResponse.builder()
                        .success(true)
                        .output(baos.toString())
                        .error(null)
                        .exception(null)
                        .executionTime(executionTime)
                        .build();

            } catch (Exception e) {
                long executionTime = System.currentTimeMillis() - startTime;
                return RunCodeResponse.builder()
                        .success(false)
                        .output(baos.toString())
                        .error(null)
                        .exception(e.getMessage())
                        .executionTime(executionTime)
                        .build();
            } finally {
                System.out.flush();
                System.setOut(old);
                classLoader.close();
            }

        } catch (Exception e) {
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
            if (line.trim().isEmpty()) {
                continue;
            }

            // Remove file path from error lines but keep all error content
            String processedLine = line;
            if (line.contains("/tmp/") && line.contains(".java:")) {
                // Extract just the filename and error details
                int fileNameStart = line.lastIndexOf("/") + 1;
                int colonIndex = line.indexOf(":", fileNameStart);
                if (colonIndex != -1 && fileNameStart < colonIndex) {
                    String fileName = line.substring(fileNameStart, colonIndex);
                    String errorDetails = line.substring(colonIndex);
                    processedLine = fileName + errorDetails;
                }
            }

            // Add line to output
            if (filteredErrors.length() > 0) {
                filteredErrors.append("\n");
            }
            filteredErrors.append(processedLine);
        }

        return filteredErrors.toString();
    }

    private List<CompilationError> parseCompilationErrors(String errorOutput) {
        String[] lines = errorOutput.split("\n");
        List<CompilationError> errors = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // Skip annotation processing warnings
            if (line.contains("Annotation processing is enabled") ||
                    line.contains("A future release of javac") ||
                    line.contains("unless at least one processor") ||
                    line.contains("Use -Xlint:-options") ||
                    line.contains("Use -proc:none")) {
                continue;
            }

            // Look for error pattern: filename:line: error: message
            if (line.contains(".java:") && line.contains(": error:")) {
                String fileName = extractFileName(line);
                int lineNumber = extractLineNumber(line);
                String errorMessage = extractErrorMessage(line);

                // Look for code snippet and pointer in following lines
                String codeSnippet = null;
                String pointer = null;

                if (i + 1 < lines.length && !lines[i + 1].trim().isEmpty() &&
                        !lines[i + 1].contains(".java:")) {
                    codeSnippet = lines[i + 1].trim();
                    i++; // Skip this line in next iteration
                }

                if (i + 1 < lines.length && lines[i + 1].contains("^")) {
                    pointer = lines[i + 1];
                    i++; // Skip this line in next iteration
                }

                errors.add(CompilationError.builder()
                        .errorFile(fileName)
                        .line(lineNumber)
                        .errorMessage(errorMessage)
                        .codeSnippet(codeSnippet)
                        .pointer(pointer)
                        .build());
            }
        }

        return errors;
    }

    private String extractFileName(String line) {
        if (line.contains("/tmp/") && line.contains(".java:")) {
            int fileNameStart = line.lastIndexOf("/") + 1;
            int colonIndex = line.indexOf(":", fileNameStart);
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
        } catch (NumberFormatException e) {
            // Handle parsing error
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

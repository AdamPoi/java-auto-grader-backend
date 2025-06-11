package io.adampoi.java_auto_grader.rest;

import io.adampoi.java_auto_grader.domain.Assignment;
import io.adampoi.java_auto_grader.domain.Classroom;
import io.adampoi.java_auto_grader.domain.Submission;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.filter.SubmissionFilterDef;
import io.adampoi.java_auto_grader.model.dto.SubmissionCompileDTO;
import io.adampoi.java_auto_grader.model.dto.SubmissionDTO;
import io.adampoi.java_auto_grader.model.response.ApiSuccessResponse;
import io.adampoi.java_auto_grader.model.response.SubmissionCompileResponse;
import io.adampoi.java_auto_grader.repository.AssignmentRepository;
import io.adampoi.java_auto_grader.repository.ClassroomRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import io.adampoi.java_auto_grader.service.SubmissionService;
import io.adampoi.java_auto_grader.util.CustomCollectors;
import io.adampoi.java_auto_grader.util.ReferencedException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.annotations.QFParam;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/submissions",
        produces = MediaType.APPLICATION_JSON_VALUE)
public class SubmissionResource {

    private final SubmissionService submissionService;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;

    public SubmissionResource(final SubmissionService submissionService,
                              final AssignmentRepository assignmentRepository, final UserRepository userRepository,
                              final ClassroomRepository classroomRepository) {
        this.submissionService = submissionService;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.classroomRepository = classroomRepository;
    }

    @GetMapping
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('SUBMISSION:LIST')")
    @Operation(summary = "Get Submissions", description = "Get all submissions with pagination and filtering capabilities")
    public ApiSuccessResponse<Page<SubmissionDTO>> getAllSubmissions(
            @RequestParam(required = false, defaultValue = "") @QFParam(SubmissionFilterDef.class) QueryFilter<Submission> filter,
            @ParameterObject Pageable pageable) {
        return ApiSuccessResponse.<Page<SubmissionDTO>>builder()
                .data(submissionService.findAll(filter, pageable))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PostMapping("/compile")
    @ApiResponse(responseCode = "200")
    @Operation(summary = "Compile Submission", description = "Compile a submission")
    public ApiSuccessResponse<String> compile(@Valid @RequestBody SubmissionCompileDTO submissionDTO)
            throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        return ApiSuccessResponse.<String>builder()
                .data(submissionService.compile(submissionDTO))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponse(responseCode = "200")
    @Operation(summary = "Upload Submission", description = "Upload a submission file")
    public ApiSuccessResponse<SubmissionCompileResponse> handleFileUpload(@Valid SubmissionCompileDTO submissionDTO)
            throws Exception {
        return ApiSuccessResponse.<SubmissionCompileResponse>builder()
                .data(submissionService.executeGradleTest(submissionDTO.getFile()))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PostMapping(value = "/upload-docker", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponse(responseCode = "200")
    @Operation(summary = "Upload Submission to Docker", description = "Upload a submission file and execute tests in Docker")
    public ApiSuccessResponse<SubmissionCompileResponse> handleFileUploadDocker(
            @Valid SubmissionCompileDTO submissionDTO) throws Exception {
        return ApiSuccessResponse.<SubmissionCompileResponse>builder()
                .data(submissionService.executeDockerGradleTest(submissionDTO.getFile()))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @GetMapping(value = "/{submissionId}")
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('SUBMISSION:READ')")
    @Operation(summary = "Get Submission", description = "Get a submission by id")
    public ApiSuccessResponse<SubmissionDTO> getSubmission(
            @PathVariable(name = "submissionId") final UUID submissionId) {
        return ApiSuccessResponse.<SubmissionDTO>builder()
                .data(submissionService.get(submissionId))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    @PreAuthorize("hasAuthority('SUBMISSION:CREATE')")
    @Operation(summary = "Create Submission", description = "Create a new submission")
    public ApiSuccessResponse<SubmissionDTO> createSubmission(
            @RequestBody @Validated(SubmissionDTO.CreateGroup.class) final SubmissionDTO submissionDTO) {
        final SubmissionDTO createdSubmission = submissionService.create(submissionDTO);
        return ApiSuccessResponse.<SubmissionDTO>builder()
                .data(createdSubmission)
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @PatchMapping("/{submissionId}")
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('SUBMISSION:UPDATE')")
    @Operation(summary = "Update Submission", description = "Update an existing submission")
    public ApiSuccessResponse<SubmissionDTO> updateSubmission(
            @PathVariable(name = "submissionId") final UUID submissionId,
            @RequestBody @Validated(SubmissionDTO.UpdateGroup.class) final SubmissionDTO submissionDTO) {
        final SubmissionDTO updatedSubmission = submissionService.update(submissionId, submissionDTO);
        return ApiSuccessResponse.<SubmissionDTO>builder()
                .data(updatedSubmission)
                .statusCode(HttpStatus.OK)
                .build();
    }

    @DeleteMapping("/{submissionId}")
    @ApiResponse(responseCode = "204")
    @PreAuthorize("hasAuthority('SUBMISSION:DELETE')")
    @Operation(summary = "Delete Submission", description = "Delete an existing submission")
    public ApiSuccessResponse<Void> deleteSubmission(
            @PathVariable(name = "submissionId") final UUID submissionId) {
        final ReferencedWarning referencedWarning = submissionService.getReferencedWarning(submissionId);
        if (referencedWarning != null) {
            throw new ReferencedException(referencedWarning);
        }
        submissionService.delete(submissionId);
        return ApiSuccessResponse.<Void>builder()
                .statusCode(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/assignmentValues")
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('SUBMISSION:READ')")
    @Operation(summary = "Get Assignment Values", description = "Get assignment values for dropdowns")
    public ApiSuccessResponse<Map<UUID, String>> getAssignmentValues() {
        return ApiSuccessResponse.<Map<UUID, String>>builder()
                .data(assignmentRepository.findAll(Sort.by("assignmentId"))
                        .stream()
                        .collect(CustomCollectors.toSortedMap(Assignment::getId,
                                Assignment::getTitle)))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @GetMapping("/studentValues")
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('SUBMISSION:READ')")
    @Operation(summary = "Get Student Values", description = "Get student values for dropdowns")
    public ApiSuccessResponse<Map<UUID, String>> getStudentValues() {
        return ApiSuccessResponse.<Map<UUID, String>>builder()
                .data(userRepository.findAll(Sort.by("userId"))
                        .stream()
                        .collect(CustomCollectors.toSortedMap(User::getId, User::getEmail)))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @GetMapping("/classroomValues")
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('SUBMISSION:READ')")
    @Operation(summary = "Get Classroom Values", description = "Get classroom values for dropdowns")
    public ApiSuccessResponse<Map<UUID, String>> getClassroomValues() {
        return ApiSuccessResponse.<Map<UUID, String>>builder()
                .data(classroomRepository.findAll(Sort.by("classroomId"))
                        .stream()
                        .collect(CustomCollectors.toSortedMap(Classroom::getId,
                                Classroom::getName)))
                .statusCode(HttpStatus.OK)
                .build();
    }

}

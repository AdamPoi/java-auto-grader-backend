package io.adampoi.java_auto_grader.rest;

import io.adampoi.java_auto_grader.domain.Submission;
import io.adampoi.java_auto_grader.filter.SubmissionFilterDef;
import io.adampoi.java_auto_grader.model.dto.SubmissionCompileDTO;
import io.adampoi.java_auto_grader.model.dto.SubmissionDTO;
import io.adampoi.java_auto_grader.model.request.TestSubmitRequest;
import io.adampoi.java_auto_grader.model.response.ApiSuccessResponse;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.repository.AssignmentRepository;
import io.adampoi.java_auto_grader.repository.ClassroomRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import io.adampoi.java_auto_grader.service.SubmissionService;
import io.adampoi.java_auto_grader.util.ReferencedException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.annotations.QFParam;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
    public ApiSuccessResponse<PageResponse<SubmissionDTO>> getAllSubmissions(
            @RequestParam(required = false, defaultValue = "") @QFParam(SubmissionFilterDef.class) QueryFilter<Submission> filter,
            @ParameterObject Pageable pageable) {
        return ApiSuccessResponse.<PageResponse<SubmissionDTO>>builder()
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

//    @PostMapping
//    @ApiResponse(responseCode = "201")
//    @PreAuthorize("hasAuthority('SUBMISSION:CREATE')")
//    @Operation(summary = "Create Submission", description = "Create a new submission")
//    public ApiSuccessResponse<SubmissionDTO> createSubmission(
//            @RequestBody @Validated(SubmissionDTO.CreateGroup.class) final SubmissionDTO submissionDTO) {
//        final SubmissionDTO createdSubmission = submissionService.create(submissionDTO);
//        return ApiSuccessResponse.<SubmissionDTO>builder()
//                .data(createdSubmission)
//                .statusCode(HttpStatus.CREATED)
//                .build();
//    }

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



    // --- 1. Student submits their assignment ---
    @PostMapping
    @PreAuthorize("hasAuthority('SUBMISSION:CREATE')")
    @Operation(summary = "Create Submission", description = "Create a new student submission for an assignment")
    @ApiResponse(responseCode = "201", description = "Submission created")
    public ApiSuccessResponse<SubmissionDTO> createSubmission(
            @RequestBody @Validated(SubmissionDTO.CreateGroup.class) final TestSubmitRequest request
    ) {
        final SubmissionDTO createdSubmission = submissionService.submitStudentSubmission(UUID.fromString(request.getUserId()), request);
        return ApiSuccessResponse.<SubmissionDTO>builder()
                .data(createdSubmission)
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    // --- 2. Teacher bulk upload ---
//    @PostMapping("/bulk")

    /// /    @PreAuthorize("hasAuthority('SUBMISSION:BULK_CREATE')")
//    @Operation(summary = "Bulk Submission Upload", description = "Teacher bulk upload student submissions")
//    @ApiResponse(responseCode = "201", description = "Bulk submissions processed")
//    public ApiSuccessResponse<BulkSubmissionDTO> createBulkSubmission(
//            @RequestBody @Validated(BulkUploadSubmissionRequest.CreateGroup.class) final BulkUploadSubmissionRequest bulkUploadRequest
//    ) {
//        // Set teacherId from principal
//
//        BulkSubmissionDTO result = submissionService.uploadBulkSubmission(
//                UUID.fromString(bulkUploadRequest.getTeacherId()),
//                bulkUploadRequest.getAssignmentId(),
//                bulkUploadRequest.getNimToCodeFiles(),
//                bulkUploadRequest.getTestFiles(),
//                bulkUploadRequest.getMainClassName(),
//                bulkUploadRequest.getBuildTool()
//        );
//        return ApiSuccessResponse.<BulkSubmissionDTO>builder()
//                .data(result)
//                .statusCode(HttpStatus.CREATED)
//                .build();
//    }

    // --- 3. Tryout submission ---
    @PostMapping("/tryout")
//    @PreAuthorize("hasAuthority('SUBMISSION:TRYOUT')")
    @Operation(summary = "Tryout Submission", description = "Try out code submission (not persisted, just returns results)")
    @ApiResponse(responseCode = "200", description = "Tryout results returned")
    public ApiSuccessResponse<SubmissionDTO> tryoutSubmission(
            @RequestBody @Validated(TestSubmitRequest.TryoutGroup.class) final TestSubmitRequest request
    ) {
        SubmissionDTO result = submissionService.tryoutSubmission(request);
        return ApiSuccessResponse.<SubmissionDTO>builder()
                .data(result)
                .statusCode(HttpStatus.OK)
                .build();
    }


}

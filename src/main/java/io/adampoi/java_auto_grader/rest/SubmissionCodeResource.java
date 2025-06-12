package io.adampoi.java_auto_grader.rest;

import io.adampoi.java_auto_grader.domain.SubmissionCode;
import io.adampoi.java_auto_grader.filter.SubmissionCodeFilterDef;
import io.adampoi.java_auto_grader.model.dto.SubmissionCodeDTO;
import io.adampoi.java_auto_grader.model.request.RunCodeRequest;
import io.adampoi.java_auto_grader.model.request.TestCodeRequest;
import io.adampoi.java_auto_grader.model.response.ApiSuccessResponse;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.model.response.RunCodeResponse;
import io.adampoi.java_auto_grader.model.response.TestCodeResponse;
import io.adampoi.java_auto_grader.service.SubmissionCodeService;
import io.adampoi.java_auto_grader.service.TestCodeService;
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

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/submission-codes",
        produces = MediaType.APPLICATION_JSON_VALUE)
public class SubmissionCodeResource {

    private final SubmissionCodeService submissionCodeService;
    private final TestCodeService testCodeService;

    public SubmissionCodeResource(final SubmissionCodeService submissionCodeService, TestCodeService testCodeService) {
        this.submissionCodeService = submissionCodeService;
        this.testCodeService = testCodeService;
    }

    @GetMapping
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('SUBMISSION_CODE:LIST')")
    @Operation(summary = "Get Submission Codes", description = "Get all submission codes with pagination and filtering capabilities")
    public ApiSuccessResponse<PageResponse<SubmissionCodeDTO>> getAllSubmissionCodes(
            @RequestParam(required = false, defaultValue = "") @QFParam(SubmissionCodeFilterDef.class) QueryFilter<SubmissionCode> filter,
            @ParameterObject Pageable pageable) {
        return ApiSuccessResponse.<PageResponse<SubmissionCodeDTO>>builder()
                .data(submissionCodeService.findAll(filter, pageable))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @GetMapping("/{submissionCodeId}")
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('SUBMISSION_CODE:READ')")
    @Operation(summary = "Get Submission Code", description = "Get a submission code by id")
    public ApiSuccessResponse<SubmissionCodeDTO> getSubmissionCode(
            @PathVariable(name = "submissionCodeId") final UUID submissionCodeId) {
        return ApiSuccessResponse.<SubmissionCodeDTO>builder()
                .data(submissionCodeService.get(submissionCodeId))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    @PreAuthorize("hasAuthority('SUBMISSION_CODE:CREATE')")
    @Operation(summary = "Create Submission Code", description = "Create a new submission code")
    public ApiSuccessResponse<SubmissionCodeDTO> createSubmissionCode(
            @RequestBody @Validated(SubmissionCodeDTO.CreateGroup.class) final SubmissionCodeDTO submissionCodeDTO) {
        final SubmissionCodeDTO createdSubmissionCode = submissionCodeService.create(submissionCodeDTO);
        return ApiSuccessResponse.<SubmissionCodeDTO>builder()
                .data(createdSubmissionCode)
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @PatchMapping("/{submissionCodeId}")
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('SUBMISSION_CODE:UPDATE')")
    @Operation(summary = "Update Submission Code", description = "Update an existing submission code")
    public ApiSuccessResponse<SubmissionCodeDTO> updateSubmissionCode(
            @PathVariable(name = "submissionCodeId") final UUID submissionCodeId,
            @RequestBody @Validated(SubmissionCodeDTO.UpdateGroup.class) final SubmissionCodeDTO submissionCodeDTO) {
        final SubmissionCodeDTO updatedSubmissionCode = submissionCodeService.update(submissionCodeId,
                submissionCodeDTO);
        return ApiSuccessResponse.<SubmissionCodeDTO>builder()
                .data(updatedSubmissionCode)
                .statusCode(HttpStatus.OK)
                .build();
    }

//    @DeleteMapping("/{submissionCodeId}")
//    @ApiResponse(responseCode = "204")
//    @PreAuthorize("hasAuthority('SUBMISSION_CODE:DELETE')")
//    @Operation(summary = "Delete Submission Code", description = "Delete an existing submission code")
//    public ApiSuccessResponse<Void> deleteSubmissionCode(
//            @PathVariable(name = "submissionCodeId") final UUID submissionCodeId) {
//        final ReferencedWarning referencedWarning = submissionCodeService.getReferencedWarning(submissionCodeId);
//        if (referencedWarning != null) {
//            throw new ReferencedException(referencedWarning);
//        }
//        submissionCodeService.delete(submissionCodeId);
//        return ApiSuccessResponse.<Void>builder()
//                .statusCode(HttpStatus.NO_CONTENT)
//                .build();
//    }

    @PostMapping("/run")
//    @PreAuthorize("hasAuthority('SUBMISSION_CODE:CREATE')")
    @Operation(summary = "Run Code", description = "Run Code")
    public ApiSuccessResponse<RunCodeResponse> runCode(
            @RequestBody @Valid final RunCodeRequest submissionCodeDTO) {
        final RunCodeResponse createdSubmissionCode = submissionCodeService.runCode(submissionCodeDTO);
        return ApiSuccessResponse.<RunCodeResponse>builder()
                .data(createdSubmissionCode)
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PostMapping("/test")
//    @PreAuthorize("hasAuthority('SUBMISSION_CODE:CREATE')")
    @Operation(summary = "Test Code", description = "Test Code")
    public ApiSuccessResponse<TestCodeResponse> testCode(
            @RequestBody @Valid final TestCodeRequest submissionCodeDTO) {
        final TestCodeResponse createdSubmissionCode = testCodeService.runTestCodeInDocker(submissionCodeDTO);
        return ApiSuccessResponse.<TestCodeResponse>builder()
                .data(createdSubmissionCode)
                .statusCode(HttpStatus.OK)
                .build();
    }
}

package io.adampoi.java_auto_grader.rest;


import io.adampoi.java_auto_grader.model.dto.SubmissionDTO;
import io.adampoi.java_auto_grader.model.request.TestSubmitRequest;
import io.adampoi.java_auto_grader.model.response.ApiSuccessResponse;
import io.adampoi.java_auto_grader.repository.RoleRepository;
import io.adampoi.java_auto_grader.repository.TestExecutionRepository;
import io.adampoi.java_auto_grader.service.SubmissionService;
import io.adampoi.java_auto_grader.service.TestExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/test-executions",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "TestExecutions")
public class TestExecutionResource {

    private final TestExecutionService testExecutionService;
    private final RoleRepository roleRepository;
    private final TestExecutionRepository testExecutionRepository;
    private final SubmissionService submissionService;

    public TestExecutionResource(final TestExecutionService testExecutionService, final RoleRepository roleRepository, TestExecutionRepository testExecutionRepository, SubmissionService submissionService) {
        this.testExecutionService = testExecutionService;
        this.roleRepository = roleRepository;
        this.testExecutionRepository = testExecutionRepository;
        this.submissionService = submissionService;
    }

//    @PreAuthorize("hasAuthority('USER:LIST')")
//    @GetMapping
//    @Operation(summary = "Get TestExecution",
//            description = "Get all testExecutions with pagination and filtering capabilities")
//    public ApiSuccessResponse<PageResponse<TestExecutionDTO>> getAllTestExecutions(
//            @RequestParam(required = false, defaultValue = "") @QFParam(TestExecutionFilterDef.class) QueryFilter<TestExecution> filter,
//            @ParameterObject @PageableDefault(page = 0, size = 10) Pageable params) {
//        return ApiSuccessResponse.<PageResponse<TestExecutionDTO>>builder()
//                .data(testExecutionService.findAll(filter, params))
//                .statusCode(HttpStatus.OK)
//                .build();
//    }


    @PostMapping("/test")
//    @PreAuthorize("hasAuthority('SUBMISSION_CODE:CREATE')")
    @Operation(summary = "Test Code", description = "Test Code")
    public ApiSuccessResponse<SubmissionDTO> testCodNew(
            @RequestBody @Valid final TestSubmitRequest request) {
        final SubmissionDTO createdSubmission = testExecutionService.runTest(request);

        return ApiSuccessResponse.<SubmissionDTO>builder()
                .data(createdSubmission)
                .statusCode(HttpStatus.OK)
                .build();
    }

}

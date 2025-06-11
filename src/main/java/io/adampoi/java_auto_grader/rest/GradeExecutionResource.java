package io.adampoi.java_auto_grader.rest;

import io.adampoi.java_auto_grader.domain.GradeExecution;
import io.adampoi.java_auto_grader.filter.GradeExecutionFilterDef;
import io.adampoi.java_auto_grader.model.dto.GradeExecutionDTO;
import io.adampoi.java_auto_grader.model.response.ApiSuccessResponse;
import io.adampoi.java_auto_grader.service.GradeExecutionService;
import io.adampoi.java_auto_grader.util.ReferencedException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.annotations.QFParam;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/grade-executions",
        produces = MediaType.APPLICATION_JSON_VALUE)
public class GradeExecutionResource {

    private final GradeExecutionService gradeExecutionService;

    public GradeExecutionResource(final GradeExecutionService gradeExecutionService) {
        this.gradeExecutionService = gradeExecutionService;
    }

    @GetMapping
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('GRADE_EXECUTION:LIST')")
    @Operation(summary = "Get Grade Executions", description = "Get all grade executions with pagination and filtering capabilities")
    public ApiSuccessResponse<Page<GradeExecutionDTO>> getAllGradeExecutions(
            @RequestParam(required = false, defaultValue = "") @QFParam(GradeExecutionFilterDef.class) QueryFilter<GradeExecution> filter,
            @ParameterObject Pageable pageable) {
        return ApiSuccessResponse.<Page<GradeExecutionDTO>>builder()
                .data(gradeExecutionService.findAll(filter, pageable))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @GetMapping("/{gradeExecutionId}")
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('GRADE_EXECUTION:READ')")
    @Operation(summary = "Get Grade Execution", description = "Get a grade execution by id")
    public ApiSuccessResponse<GradeExecutionDTO> getGradeExecution(
            @PathVariable(name = "gradeExecutionId") final UUID gradeExecutionId) {
        return ApiSuccessResponse.<GradeExecutionDTO>builder()
                .data(gradeExecutionService.get(gradeExecutionId))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    @PreAuthorize("hasAuthority('GRADE_EXECUTION:CREATE')")
    @Operation(summary = "Create Grade Execution", description = "Create a new grade execution")
    public ApiSuccessResponse<GradeExecutionDTO> createGradeExecution(
            @RequestBody @Validated(GradeExecutionDTO.CreateGroup.class) final GradeExecutionDTO gradeExecutionDTO) {
        final GradeExecutionDTO createdGradeExecution = gradeExecutionService.create(gradeExecutionDTO);
        return ApiSuccessResponse.<GradeExecutionDTO>builder()
                .data(createdGradeExecution)
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @PatchMapping("/{gradeExecutionId}")
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('GRADE_EXECUTION:UPDATE')")
    @Operation(summary = "Update Grade Execution", description = "Update an existing grade execution")
    public ApiSuccessResponse<GradeExecutionDTO> updateGradeExecution(
            @PathVariable(name = "gradeExecutionId") final UUID gradeExecutionId,
            @RequestBody @Validated(GradeExecutionDTO.UpdateGroup.class) final GradeExecutionDTO gradeExecutionDTO) {
        final GradeExecutionDTO updatedGradeExecution = gradeExecutionService.update(gradeExecutionId,
                gradeExecutionDTO);
        return ApiSuccessResponse.<GradeExecutionDTO>builder()
                .data(updatedGradeExecution)
                .statusCode(HttpStatus.OK)
                .build();
    }

    @DeleteMapping("/{gradeExecutionId}")
    @ApiResponse(responseCode = "204")
    @PreAuthorize("hasAuthority('GRADE_EXECUTION:DELETE')")
    @Operation(summary = "Delete Grade Execution", description = "Delete an existing grade execution")
    public ApiSuccessResponse<Void> deleteGradeExecution(
            @PathVariable(name = "gradeExecutionId") final UUID gradeExecutionId) {
        final ReferencedWarning referencedWarning = gradeExecutionService.getReferencedWarning(gradeExecutionId);
        if (referencedWarning != null) {
            throw new ReferencedException(referencedWarning);
        }
        gradeExecutionService.delete(gradeExecutionId);
        return ApiSuccessResponse.<Void>builder()
                .statusCode(HttpStatus.NO_CONTENT)
                .build();
    }
}

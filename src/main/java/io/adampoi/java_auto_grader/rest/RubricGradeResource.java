package io.adampoi.java_auto_grader.rest;

import io.adampoi.java_auto_grader.domain.RubricGrade;
import io.adampoi.java_auto_grader.filter.RubricGradeFilterDef;
import io.adampoi.java_auto_grader.model.dto.RubricGradeDTO;
import io.adampoi.java_auto_grader.model.response.ApiSuccessResponse;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.service.RubricGradeService;
import io.adampoi.java_auto_grader.util.ReferencedException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.annotations.QFParam;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/rubric-grades",
        produces = MediaType.APPLICATION_JSON_VALUE)
public class RubricGradeResource {

    private final RubricGradeService rubricGradeService;

    public RubricGradeResource(final RubricGradeService rubricGradeService) {
        this.rubricGradeService = rubricGradeService;
    }

    @GetMapping
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('RUBRIC_GRADE:LIST')")
    @Operation(summary = "Get Rubric Grades", description = "Get all rubric grades with pagination and filtering capabilities")
    public ApiSuccessResponse<PageResponse<RubricGradeDTO>> getAllRubricGrades(
            @RequestParam(required = false, defaultValue = "") @QFParam(RubricGradeFilterDef.class) QueryFilter<RubricGrade> filter,
            @ParameterObject Pageable pageable) {
        return ApiSuccessResponse.<PageResponse<RubricGradeDTO>>builder()
                .data(rubricGradeService.findAll(filter, pageable))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @GetMapping("/{rubricGradeId}")
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('RUBRIC_GRADE:READ')")
    @Operation(summary = "Get Rubric Grade", description = "Get a rubric grade by id")
    public ApiSuccessResponse<RubricGradeDTO> getRubricGrade(
            @PathVariable(name = "rubricGradeId") final UUID rubricGradeId) {
        return ApiSuccessResponse.<RubricGradeDTO>builder()
                .data(rubricGradeService.get(rubricGradeId))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    @PreAuthorize("hasAuthority('RUBRIC_GRADE:CREATE')")
    @Operation(summary = "Create Rubric Grade", description = "Create a new rubric grade")
    public ApiSuccessResponse<RubricGradeDTO> createRubricGrade(
            @RequestBody @Validated(RubricGradeDTO.CreateGroup.class) final RubricGradeDTO rubricGradeDTO) {
        final RubricGradeDTO createdRubricGrade = rubricGradeService.create(rubricGradeDTO);
        return ApiSuccessResponse.<RubricGradeDTO>builder()
                .data(createdRubricGrade)
                .statusCode(HttpStatus.CREATED)
                .build();
    }


    @PatchMapping("/{rubricGradeId}")
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('RUBRIC_GRADE:UPDATE')")
    @Operation(summary = "Update Rubric Grade", description = "Update an existing rubric grade")
    public ApiSuccessResponse<RubricGradeDTO> updateRubricGrade(
            @PathVariable(name = "rubricGradeId") final UUID rubricGradeId,
            @RequestBody @Validated(RubricGradeDTO.UpdateGroup.class) final RubricGradeDTO rubricGradeDTO) {
        final RubricGradeDTO updatedRubricGrade = rubricGradeService.update(rubricGradeId, rubricGradeDTO);
        return ApiSuccessResponse.<RubricGradeDTO>builder()
                .data(updatedRubricGrade)
                .statusCode(HttpStatus.OK)
                .build();
    }

    @DeleteMapping("/{rubricGradeId}")
    @ApiResponse(responseCode = "204")
    @PreAuthorize("hasAuthority('RUBRIC_GRADE:DELETE')")
    @Operation(summary = "Delete Rubric Grade", description = "Delete an existing rubric grade")
    public ApiSuccessResponse<Void> deleteRubricGrade(
            @PathVariable(name = "rubricGradeId") final UUID rubricGradeId) {
        final ReferencedWarning referencedWarning = rubricGradeService.getReferencedWarning(rubricGradeId);
        if (referencedWarning != null) {
            throw new ReferencedException(referencedWarning);
        }
        rubricGradeService.delete(rubricGradeId);
        return ApiSuccessResponse.<Void>builder()
                .statusCode(HttpStatus.NO_CONTENT)
                .build();
    }
}

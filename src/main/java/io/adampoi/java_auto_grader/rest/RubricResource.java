package io.adampoi.java_auto_grader.rest;

import io.adampoi.java_auto_grader.domain.Rubric;
import io.adampoi.java_auto_grader.filter.RubricFilterDef;
import io.adampoi.java_auto_grader.model.dto.CourseDTO;
import io.adampoi.java_auto_grader.model.dto.RubricDTO;
import io.adampoi.java_auto_grader.model.response.ApiSuccessResponse;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.service.RubricService;
import io.adampoi.java_auto_grader.util.ReferencedException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.annotations.QFParam;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/rubrics",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Rubrics")
public class RubricResource {

    private final RubricService rubricService;

    public RubricResource(final RubricService rubricService) {
        this.rubricService = rubricService;
    }

    @PreAuthorize("hasAuthority('RUBRIC:LIST')")
    @GetMapping
    @Operation(summary = "Get Rubrics", description = "Get all rubrics with pagination and filtering capabilities")
    public ApiSuccessResponse<PageResponse<RubricDTO>> getAllRubrics(
            @RequestParam(required = false, defaultValue = "") @QFParam(RubricFilterDef.class) QueryFilter<Rubric> filter,
            @ParameterObject @PageableDefault(page = 0, size = 10) Pageable params) {
        return ApiSuccessResponse.<PageResponse<RubricDTO>>builder()
                .data(rubricService.findAll(filter, params))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PreAuthorize("hasAuthority('RUBRIC:READ')")
    @GetMapping("/{rubricId}")
    @Operation(summary = "Get Rubric", description = "Get a rubric by id")
    public ApiSuccessResponse<RubricDTO> getRubric(@PathVariable(name = "rubricId") final UUID rubricId) {

        return ApiSuccessResponse.<RubricDTO>builder()
                .data(rubricService.get(rubricId))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PreAuthorize("hasAuthority('RUBRIC:CREATE')")
    @PostMapping
    @Operation(summary = "Create Rubric", description = "Create a new rubric")
    public ApiSuccessResponse<RubricDTO> createRubric(@RequestBody @Validated(CourseDTO.CreateGroup.class) RubricDTO rubricDTO) {

        final RubricDTO createdRubric = rubricService.create(rubricDTO);

        return ApiSuccessResponse.<RubricDTO>builder()
                .data(createdRubric)
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @PreAuthorize("hasAuthority('RUBRIC:UPDATE')")
    @PatchMapping("/{rubricId}")
    @Operation(summary = "Update Rubric", description = "Update an existing rubric")
    public ApiSuccessResponse<RubricDTO> updateRubric(@PathVariable(name = "rubricId") final UUID rubricId,
                                                      @RequestBody @Validated(CourseDTO.UpdateGroup.class) RubricDTO rubricDTO) {
        final RubricDTO updatedRubric = rubricService.update(rubricId, rubricDTO);
        return ApiSuccessResponse.<RubricDTO>builder()
                .data(updatedRubric)
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PreAuthorize("hasAuthority('RUBRIC:DELETE')")
    @DeleteMapping("/{rubricId}")
    @Operation(summary = "Delete Rubric", description = "Delete an existing rubric")
    public ApiSuccessResponse<Void> deleteRubric(@PathVariable(name = "rubricId") final UUID rubricId) {
        final ReferencedWarning referencedWarning = rubricService.getReferencedWarning(rubricId);
        if (referencedWarning != null) {
            throw new ReferencedException(referencedWarning);
        }
        rubricService.delete(rubricId);
        return ApiSuccessResponse.<Void>builder()
                .statusCode(HttpStatus.NO_CONTENT)
                .build();
    }
}

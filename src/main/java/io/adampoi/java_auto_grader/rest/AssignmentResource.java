package io.adampoi.java_auto_grader.rest;

import io.adampoi.java_auto_grader.domain.Assignment;
import io.adampoi.java_auto_grader.domain.Course;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.filter.AssignmentFilterDef;
import io.adampoi.java_auto_grader.model.dto.AssignmentDTO;
import io.adampoi.java_auto_grader.model.response.ApiSuccessResponse;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.repository.CourseRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import io.adampoi.java_auto_grader.service.AssignmentService;
import io.adampoi.java_auto_grader.util.CustomCollectors;
import io.adampoi.java_auto_grader.util.ReferencedException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.annotations.QFParam;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/assignments",
        produces = MediaType.APPLICATION_JSON_VALUE)
public class AssignmentResource {

    private final AssignmentService assignmentService;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public AssignmentResource(final AssignmentService assignmentService,
                              final CourseRepository courseRepository, final UserRepository userRepository) {
        this.assignmentService = assignmentService;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('ASSIGNMENT:LIST')")
    @Operation(summary = "Get Assignments", description = "Get all assignments with pagination and filtering capabilities")
    public ApiSuccessResponse<PageResponse<AssignmentDTO>> getAllAssignments(
            @RequestParam(required = false, defaultValue = "") @QFParam(AssignmentFilterDef.class) QueryFilter<Assignment> filter,
            @ParameterObject Pageable pageable) {
        return ApiSuccessResponse.<PageResponse<AssignmentDTO>>builder()
                .data(assignmentService.findAll(filter, pageable))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @GetMapping("/{assignmentId}")
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('ASSIGNMENT:READ')")
    @Operation(summary = "Get Assignment", description = "Get an assignment by id")
    public ApiSuccessResponse<AssignmentDTO> getAssignment(
            @PathVariable(name = "assignmentId") final UUID assignmentId) {
        return ApiSuccessResponse.<AssignmentDTO>builder()
                .data(assignmentService.get(assignmentId))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    @PreAuthorize("hasAuthority('ASSIGNMENT:CREATE')")
    @Operation(summary = "Create Assignment", description = "Create a new assignment")
    public ApiSuccessResponse<AssignmentDTO> createAssignment(
            @RequestBody @Validated(AssignmentDTO.CreateGroup.class) final AssignmentDTO assignmentDTO) {
        final AssignmentDTO createdAssignment = assignmentService.create(assignmentDTO);
        return ApiSuccessResponse.<AssignmentDTO>builder()
                .data(createdAssignment)
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @PatchMapping("/{assignmentId}")
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('ASSIGNMENT:UPDATE')")
    @Operation(summary = "Update Assignment", description = "Update an existing assignment")
    public ApiSuccessResponse<AssignmentDTO> updateAssignment(
            @PathVariable(name = "assignmentId") final UUID assignmentId,
            @RequestBody @Validated(AssignmentDTO.UpdateGroup.class) final AssignmentDTO assignmentDTO) {
        final AssignmentDTO updatedAssignment = assignmentService.update(assignmentId, assignmentDTO);
        return ApiSuccessResponse.<AssignmentDTO>builder()
                .data(updatedAssignment)
                .statusCode(HttpStatus.OK)
                .build();
    }

    @DeleteMapping("/{assignmentId}")
    @ApiResponse(responseCode = "204")
    @PreAuthorize("hasAuthority('ASSIGNMENT:DELETE')")
    @Operation(summary = "Delete Assignment", description = "Delete an existing assignment")
    public ApiSuccessResponse<Void> deleteAssignment(
            @PathVariable(name = "assignmentId") final UUID assignmentId) {
        final ReferencedWarning referencedWarning = assignmentService.getReferencedWarning(assignmentId);
        if (referencedWarning != null) {
            throw new ReferencedException(referencedWarning);
        }
        assignmentService.delete(assignmentId);
        return ApiSuccessResponse.<Void>builder()
                .statusCode(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/courseValues")
    @ApiResponse(responseCode = "200")
    @Operation(summary = "Get Course Values", description = "Get course values for dropdowns")
    public ApiSuccessResponse<Map<UUID, String>> getCourseValues() {
        Map<UUID, String> courses = courseRepository.findAll(Sort.by("courseId"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Course::getId, Course::getCode));
        return ApiSuccessResponse.<Map<UUID, String>>builder()
                .data(courses)
                .statusCode(HttpStatus.OK)
                .build();
    }

    @GetMapping("/createdByTeacherValues")
    @ApiResponse(responseCode = "200")
    @Operation(summary = "Get Created By Teacher Values", description = "Get created by teacher values for dropdowns")
    public ApiSuccessResponse<Map<UUID, String>> getCreatedByTeacherValues() {
        Map<UUID, String> teachers = userRepository.findAll(Sort.by("userId"))
                .stream()
                .collect(CustomCollectors.toSortedMap(User::getId, User::getEmail));
        return ApiSuccessResponse.<Map<UUID, String>>builder()
                .data(teachers)
                .statusCode(HttpStatus.OK)
                .build();
    }

}

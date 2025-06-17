package io.adampoi.java_auto_grader.rest;

import io.adampoi.java_auto_grader.domain.Classroom;
import io.adampoi.java_auto_grader.domain.Course;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.filter.ClassroomFilterDef;
import io.adampoi.java_auto_grader.model.dto.ClassroomDTO;
import io.adampoi.java_auto_grader.model.response.ApiSuccessResponse;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.repository.CourseRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import io.adampoi.java_auto_grader.service.ClassroomService;
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
@RequestMapping(value = "/api/classrooms", produces = MediaType.APPLICATION_JSON_VALUE)
public class ClassroomResource {

    private final ClassroomService classroomService;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public ClassroomResource(final ClassroomService classroomService,
                             final CourseRepository courseRepository, final UserRepository userRepository) {
        this.classroomService = classroomService;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('CLASSROOM:LIST')")
    @Operation(summary = "Get Classrooms", description = "Get all classrooms with pagination and filtering capabilities")
    public ApiSuccessResponse<PageResponse<ClassroomDTO>> getAllClassrooms(
            @RequestParam(required = false, defaultValue = "") @QFParam(ClassroomFilterDef.class) QueryFilter<Classroom> filter,
            @ParameterObject Pageable pageable) {
        return ApiSuccessResponse.<PageResponse<ClassroomDTO>>builder()
                .data(classroomService.findAll(filter, pageable))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @GetMapping("/{classroomId}")
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('CLASSROOM:READ')")
    @Operation(summary = "Get Classroom", description = "Get a classroom by id")
    public ApiSuccessResponse<ClassroomDTO> getClassroom(
            @PathVariable(name = "classroomId") final UUID classroomId) {
        return ApiSuccessResponse.<ClassroomDTO>builder()
                .data(classroomService.get(classroomId))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    @PreAuthorize("hasAuthority('CLASSROOM:CREATE')")
    @Operation(summary = "Create Classroom", description = "Create a new classroom")
    public ApiSuccessResponse<ClassroomDTO> createClassroom(
            @RequestBody @Validated(ClassroomDTO.CreateGroup.class) final ClassroomDTO classroomDTO) {
        final ClassroomDTO createdClassroom = classroomService.create(classroomDTO);
        return ApiSuccessResponse.<ClassroomDTO>builder()
                .data(createdClassroom)
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @PatchMapping("/{classroomId}")
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('CLASSROOM:UPDATE')")
    @Operation(summary = "Update Classroom", description = "Update an existing classroom")
    public ApiSuccessResponse<ClassroomDTO> updateClassroom(
            @PathVariable(name = "classroomId") final UUID classroomId,
            @RequestBody @Validated(ClassroomDTO.UpdateGroup.class) final ClassroomDTO classroomDTO) {
        final ClassroomDTO updatedClassroom = classroomService.update(classroomId, classroomDTO);
        return ApiSuccessResponse.<ClassroomDTO>builder()
                .data(updatedClassroom)
                .statusCode(HttpStatus.OK)
                .build();
    }

    @DeleteMapping("/{classroomId}")
    @ApiResponse(responseCode = "204")
    @PreAuthorize("hasAuthority('CLASSROOM:DELETE')")
    @Operation(summary = "Delete Classroom", description = "Delete an existing classroom")
    public ApiSuccessResponse<Void> deleteClassroom(
            @PathVariable(name = "classroomId") final UUID classroomId) {
        final ReferencedWarning referencedWarning = classroomService.getReferencedWarning(classroomId);
        if (referencedWarning != null) {
            throw new ReferencedException(referencedWarning);
        }
        classroomService.delete(classroomId);
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

    @GetMapping("/teacherValues")
    @ApiResponse(responseCode = "200")
    @Operation(summary = "Get Teacher Values", description = "Get teacher values for dropdowns")
    public ApiSuccessResponse<Map<UUID, String>> getTeacherValues() {
        Map<UUID, String> teachers = userRepository.findAll(Sort.by("userId"))
                .stream()
                .collect(CustomCollectors.toSortedMap(User::getId, User::getEmail));
        return ApiSuccessResponse.<Map<UUID, String>>builder()
                .data(teachers)
                .statusCode(HttpStatus.OK)
                .build();
    }

}

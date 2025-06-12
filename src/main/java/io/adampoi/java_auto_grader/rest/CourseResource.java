package io.adampoi.java_auto_grader.rest;

import io.adampoi.java_auto_grader.domain.Course;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.filter.CourseFilterDef;
import io.adampoi.java_auto_grader.model.dto.CourseDTO;
import io.adampoi.java_auto_grader.model.response.ApiSuccessResponse;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.repository.UserRepository;
import io.adampoi.java_auto_grader.service.CourseService;
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
@RequestMapping(value = "/api/courses",
        produces = MediaType.APPLICATION_JSON_VALUE)
public class CourseResource {

    private final CourseService courseService;
    private final UserRepository userRepository;

    public CourseResource(final CourseService courseService, final UserRepository userRepository) {
        this.courseService = courseService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('COURSE:LIST')")
    @Operation(summary = "Get Courses", description = "Get all courses with pagination and filtering capabilities")
    public ApiSuccessResponse<PageResponse<CourseDTO>> getAllCourses(
            @RequestParam(required = false, defaultValue = "") @QFParam(CourseFilterDef.class) QueryFilter<Course> filter,
            @ParameterObject Pageable pageable) {
        return ApiSuccessResponse.<PageResponse<CourseDTO>>builder()
                .data(courseService.findAll(filter, pageable))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @GetMapping("/{courseId}")
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('COURSE:READ')")
    @Operation(summary = "Get Course", description = "Get a course by id")
    public ApiSuccessResponse<CourseDTO> getCourse(@PathVariable(name = "courseId") final UUID courseId) {
        return ApiSuccessResponse.<CourseDTO>builder()
                .data(courseService.get(courseId))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    @PreAuthorize("hasAuthority('COURSE:CREATE')")
    @Operation(summary = "Create Course", description = "Create a new course")
    public ApiSuccessResponse<CourseDTO> createCourse(@RequestBody @Validated(CourseDTO.CreateGroup.class) final CourseDTO courseDTO) {
        final CourseDTO createdCourse = courseService.create(courseDTO);
        return ApiSuccessResponse.<CourseDTO>builder()
                .data(createdCourse)
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @PatchMapping("/{courseId}")
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('COURSE:UPDATE')")
    @Operation(summary = "Update Course", description = "Update an existing course")
    public ApiSuccessResponse<CourseDTO> updateCourse(@PathVariable(name = "courseId") final UUID courseId,
                                                      @RequestBody @Validated(CourseDTO.UpdateGroup.class) final CourseDTO courseDTO) {
        final CourseDTO updatedCourse = courseService.update(courseId, courseDTO);
        return ApiSuccessResponse.<CourseDTO>builder()
                .data(updatedCourse)
                .statusCode(HttpStatus.OK)
                .build();
    }

    @DeleteMapping("/{courseId}")
    @ApiResponse(responseCode = "204")
    @PreAuthorize("hasAuthority('COURSE:DELETE')")
    @Operation(summary = "Delete Course", description = "Delete an existing course")
    public ApiSuccessResponse<Void> deleteCourse(@PathVariable(name = "courseId") final UUID courseId) {
        final ReferencedWarning referencedWarning = courseService.getReferencedWarning(courseId);
        if (referencedWarning != null) {
            throw new ReferencedException(referencedWarning);
        }
        courseService.delete(courseId);
        return ApiSuccessResponse.<Void>builder()
                .statusCode(HttpStatus.NO_CONTENT)
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

package io.adampoi.java_auto_grader.rest;

import io.adampoi.java_auto_grader.domain.Classroom;
import io.adampoi.java_auto_grader.domain.StudentClassroom;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.filter.StudentClassroomFilterDef;
import io.adampoi.java_auto_grader.model.dto.StudentClassroomDTO;
import io.adampoi.java_auto_grader.model.response.ApiSuccessResponse;
import io.adampoi.java_auto_grader.repository.ClassroomRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import io.adampoi.java_auto_grader.service.StudentClassroomService;
import io.adampoi.java_auto_grader.util.CustomCollectors;
import io.adampoi.java_auto_grader.util.ReferencedException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.annotations.QFParam;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
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
@RequestMapping(value = "/api/studentClassrooms",
        produces = MediaType.APPLICATION_JSON_VALUE)
public class StudentClassroomResource {

    private final StudentClassroomService studentClassroomService;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;

    public StudentClassroomResource(final StudentClassroomService studentClassroomService,
                                    final UserRepository userRepository, final ClassroomRepository classroomRepository) {
        this.studentClassroomService = studentClassroomService;
        this.userRepository = userRepository;
        this.classroomRepository = classroomRepository;
    }

    @GetMapping
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('STUDENT_CLASSROOM:LIST')")
    @Operation(summary = "Get Student Classrooms", description = "Get all student classrooms with pagination and filtering capabilities")
    public ApiSuccessResponse<Page<StudentClassroomDTO>> getAllStudentClassrooms(
            @RequestParam(required = false, defaultValue = "") @QFParam(StudentClassroomFilterDef.class) QueryFilter<StudentClassroom> filter,
            @ParameterObject Pageable pageable) {
        return ApiSuccessResponse.<Page<StudentClassroomDTO>>builder()
                .data(studentClassroomService.findAll(filter, pageable))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @GetMapping("/{id}")
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('STUDENT_CLASSROOM:READ')")
    @Operation(summary = "Get Student Classroom", description = "Get a student classroom by id")
    public ApiSuccessResponse<StudentClassroomDTO> getStudentClassroom(
            @PathVariable(name = "id") final UUID id) {
        return ApiSuccessResponse.<StudentClassroomDTO>builder()
                .data(studentClassroomService.get(id))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    @PreAuthorize("hasAuthority('STUDENT_CLASSROOM:CREATE')")
    @Operation(summary = "Create Student Classroom", description = "Create a new student classroom")
    public ApiSuccessResponse<StudentClassroomDTO> createStudentClassroom(
            @RequestBody @Validated(StudentClassroomDTO.CreateGroup.class) final StudentClassroomDTO studentClassroomDTO) {
        final StudentClassroomDTO createdStudentClassroom = studentClassroomService.create(studentClassroomDTO);
        return ApiSuccessResponse.<StudentClassroomDTO>builder()
                .data(createdStudentClassroom)
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @PatchMapping("/{id}")
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('STUDENT_CLASSROOM:UPDATE')")
    @Operation(summary = "Update Student Classroom", description = "Update an existing student classroom")
    public ApiSuccessResponse<StudentClassroomDTO> updateStudentClassroom(@PathVariable(name = "id") final UUID id,
                                                                          @RequestBody @Validated(StudentClassroomDTO.UpdateGroup.class) final StudentClassroomDTO studentClassroomDTO) {
        final StudentClassroomDTO updatedStudentClassroom = studentClassroomService.update(id,
                studentClassroomDTO);
        return ApiSuccessResponse.<StudentClassroomDTO>builder()
                .data(updatedStudentClassroom)
                .statusCode(HttpStatus.OK)
                .build();
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204")
    @PreAuthorize("hasAuthority('STUDENT_CLASSROOM:DELETE')")
    @Operation(summary = "Delete Student Classroom", description = "Delete an existing student classroom")
    public ApiSuccessResponse<Void> deleteStudentClassroom(@PathVariable(name = "id") final UUID id) {
        final ReferencedWarning referencedWarning = studentClassroomService.getReferencedWarning(id);
        if (referencedWarning != null) {
            throw new ReferencedException(referencedWarning);
        }
        studentClassroomService.delete(id);
        return ApiSuccessResponse.<Void>builder()
                .statusCode(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/studentValues")
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('STUDENT_CLASSROOM:READ')")
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
    @PreAuthorize("hasAuthority('STUDENT_CLASSROOM:READ')")
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

package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.domain.Assignment;
import io.adampoi.java_auto_grader.domain.Course;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.model.dto.AssignmentDTO;
import io.adampoi.java_auto_grader.model.dto.CourseDTO;
import io.adampoi.java_auto_grader.model.dto.UserDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.repository.AssignmentRepository;
import io.adampoi.java_auto_grader.repository.ClassroomRepository;
import io.adampoi.java_auto_grader.repository.CourseRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    private final AssignmentRepository assignmentRepository;

    public CourseService(final CourseRepository courseRepository, final UserRepository userRepository,
                         final ClassroomRepository classroomRepository,
                         final AssignmentRepository assignmentRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.classroomRepository = classroomRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public PageResponse<CourseDTO> findAll(QueryFilter<Course> filter, Pageable pageable) {
        final Page<Course> page = courseRepository.findAll(filter, pageable);
        Page<CourseDTO> dtoPage = new PageImpl<>(page.getContent()
                .stream()
                .map(course -> mapToDTO(course, new CourseDTO()))
                .collect(Collectors.toList()),
                pageable, page.getTotalElements());
        return PageResponse.from(dtoPage);
    }

    public CourseDTO get(final UUID courseId) {
        return courseRepository.findById(courseId)
                .map(course -> mapToDTO(course, new CourseDTO()))
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
    }

    public CourseDTO create(final CourseDTO courseDTO) {
        final Course course = new Course();
        mapToEntity(courseDTO, course);
        Course savedCourse = courseRepository.save(course);
        return mapToDTO(savedCourse, new CourseDTO());
    }

    public CourseDTO update(final UUID courseId, final CourseDTO courseDTO) {
        final Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        mapToEntity(courseDTO, course);
        Course savedCourse = courseRepository.save(course);
        return mapToDTO(savedCourse, new CourseDTO());
    }


    public void delete(final UUID courseId) {
        courseRepository.deleteById(courseId);
    }

    public PageResponse<AssignmentDTO> getCourseAssignments(final UUID courseId, QueryFilter<Assignment> filter, Pageable pageable) {
        filter.addNewField("course", QFOperationEnum.EQUAL, courseId.toString());
        Page<Assignment> assignmentPage = assignmentRepository.findAll(filter, pageable);

        Page<AssignmentDTO> dtoPage = new PageImpl<>(assignmentPage.getContent()
                .stream()
                .map(assignment -> AssignmentService.mapToDTO(assignment, new AssignmentDTO()))
                .collect(Collectors.toList()),
                pageable, assignmentPage.getTotalElements());

        return PageResponse.from(dtoPage);
    }

    private CourseDTO mapToDTO(final Course course, final CourseDTO courseDTO) {
        courseDTO.setId(course.getId());
        courseDTO.setCode(course.getCode());
        courseDTO.setName(course.getName());
        courseDTO.setDescription(course.getDescription());
        courseDTO.setIsActive(course.getIsActive());
        courseDTO.setCreatedAt(course.getCreatedAt());
        courseDTO.setUpdatedAt(course.getUpdatedAt());
//        courseDTO.setEnrolledStudents(course.getEnrolledUsers().stream()
//                .map(user -> UserService.mapToDTO(user, new UserDTO()))
//                .collect(Collectors.toList()));
        courseDTO.setCreatedByTeacher(course.getCreatedByTeacher() == null ? null :
                UserService.mapToDTO(course.getCreatedByTeacher(), new UserDTO()));

        List<AssignmentDTO> assignmentDTOs = Optional.ofNullable(course.getCourseAssignments())
                .orElse(Collections.emptySet())
                .stream()
                .map(assignment -> AssignmentService.mapToDTO(assignment, new AssignmentDTO()))
                .collect(Collectors.toList());

        courseDTO.setCourseAssignments(assignmentDTOs);

        return courseDTO;
    }

    private Course mapToEntity(final CourseDTO courseDTO, final Course course) {
        if (courseDTO.getCode() != null) {
            course.setCode(courseDTO.getCode());
        }
        if (courseDTO.getName() != null) {
            course.setName(courseDTO.getName());
        }
        if (courseDTO.getDescription() != null) {
            course.setDescription(courseDTO.getDescription());
        }
        if (courseDTO.getIsActive() != null) {
            course.setIsActive(courseDTO.getIsActive());
        }

        if (courseDTO.getTeacherId() != null) {
            final User teacher = userRepository.findById(courseDTO.getTeacherId())
                    .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));
            course.setCreatedByTeacher(teacher);
        }
        if (!courseDTO.getStudentIds().isEmpty()) {
            Set<User> users = courseDTO.getStudentIds().stream()
                    .map(userId -> userRepository.findById(userId)
                            .orElseThrow(() -> new EntityNotFoundException("Student not found: " + userId)))
                    .collect(Collectors.toSet());
            course.setEnrolledUsers(users);
        }
        if (course.getCourseAssignments() != null) {
            courseDTO.setAssignmentIds(course.getCourseAssignments().stream()
                    .map(Assignment::getId)
                    .collect(Collectors.toList()));
        } else {
            courseDTO.setAssignmentIds(Collections.emptyList());
        }


        return course;
    }

    public ReferencedWarning getReferencedWarning(final UUID courseId) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));


        final Assignment courseAssignment = assignmentRepository.findFirstByCourse(course);
        if (courseAssignment != null) {
            referencedWarning.setKey("course.assignment.course.referenced");
            referencedWarning.addParam(courseAssignment.getId());
            return referencedWarning;
        }
        return null;
    }
}

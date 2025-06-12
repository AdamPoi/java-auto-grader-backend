package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.domain.Assignment;
import io.adampoi.java_auto_grader.domain.Classroom;
import io.adampoi.java_auto_grader.domain.Course;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.model.dto.CourseDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.repository.AssignmentRepository;
import io.adampoi.java_auto_grader.repository.ClassroomRepository;
import io.adampoi.java_auto_grader.repository.CourseRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import io.adampoi.java_auto_grader.util.NotFoundException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;
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
                .orElseThrow(() -> new NotFoundException("Course not found"));
    }

    public CourseDTO create(final CourseDTO courseDTO) {
        final Course course = new Course();
        mapToEntity(courseDTO, course);
        Course savedCourse = courseRepository.save(course);
        return mapToDTO(savedCourse, new CourseDTO());
    }

    public CourseDTO update(final UUID courseId, final CourseDTO courseDTO) {
        final Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found"));
        mapToEntity(courseDTO, course);
        Course savedCourse = courseRepository.save(course);
        return mapToDTO(savedCourse, new CourseDTO());
    }

    public void delete(final UUID courseId) {
        courseRepository.deleteById(courseId);
    }

    private CourseDTO mapToDTO(final Course course, final CourseDTO courseDTO) {
        courseDTO.setId(course.getId());
        courseDTO.setCode(course.getCode());
        courseDTO.setName(course.getName());
        courseDTO.setDescription(course.getDescription());
        courseDTO.setIsActive(course.getIsActive());
        courseDTO.setEnrollmentStartDate(course.getEnrollmentStartDate());
        courseDTO.setEnrollmentEndDate(course.getEnrollmentEndDate());
        courseDTO.setCreatedAt(course.getCreatedAt());
        courseDTO.setUpdatedAt(course.getUpdatedAt());
        courseDTO.setCreatedByTeacher(
                course.getCreatedByTeacher() == null ? null : course.getCreatedByTeacher().getId());
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
        if (courseDTO.getEnrollmentStartDate() != null) {
            course.setEnrollmentStartDate(courseDTO.getEnrollmentStartDate());
        }
        if (courseDTO.getEnrollmentEndDate() != null) {
            course.setEnrollmentEndDate(courseDTO.getEnrollmentEndDate());
        }
        if (courseDTO.getCreatedByTeacher() != null) {
            final User createdByTeacher = userRepository.findById(courseDTO.getCreatedByTeacher())
                    .orElseThrow(() -> new NotFoundException("createdByTeacher not found"));
            course.setCreatedByTeacher(createdByTeacher);
        }
        if (courseDTO.getCreatedAt() != null) {
            course.setCreatedAt(courseDTO.getCreatedAt());
        }
        if (courseDTO.getUpdatedAt() != null) {
            course.setUpdatedAt(courseDTO.getUpdatedAt());
        }
        return course;
    }

    public ReferencedWarning getReferencedWarning(final UUID courseId) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found"));
        final Classroom courseClassroom = classroomRepository.findFirstByCourse(course);
        if (courseClassroom != null) {
            referencedWarning.setKey("course.classroom.course.referenced");
            referencedWarning.addParam(courseClassroom.getId());
            return referencedWarning;
        }

        final Assignment courseAssignment = assignmentRepository.findFirstByCourse(course);
        if (courseAssignment != null) {
            referencedWarning.setKey("course.assignment.course.referenced");
            referencedWarning.addParam(courseAssignment.getId());
            return referencedWarning;
        }
        return null;
    }
}

package io.adampoi.java_auto_grader.service;

import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.adampoi.java_auto_grader.domain.Classroom;
import io.adampoi.java_auto_grader.domain.Course;
import io.adampoi.java_auto_grader.domain.StudentClassroom;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.model.dto.ClassroomDTO;
import io.adampoi.java_auto_grader.repository.ClassroomRepository;
import io.adampoi.java_auto_grader.repository.CourseRepository;
import io.adampoi.java_auto_grader.repository.StudentClassroomRepository;
import io.adampoi.java_auto_grader.repository.SubmissionRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import io.adampoi.java_auto_grader.util.NotFoundException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final StudentClassroomRepository studentClassroomRepository;
    private final SubmissionRepository submissionRepository;

    public ClassroomService(final ClassroomRepository classroomRepository,
            final CourseRepository courseRepository, final UserRepository userRepository,
            final StudentClassroomRepository studentClassroomRepository,
            final SubmissionRepository submissionRepository) {
        this.classroomRepository = classroomRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.studentClassroomRepository = studentClassroomRepository;
        this.submissionRepository = submissionRepository;
    }

    public Page<ClassroomDTO> findAll(QueryFilter<Classroom> filter, Pageable pageable) {
        final Page<Classroom> page = classroomRepository.findAll(filter, pageable);
        return new PageImpl<>(page.getContent()
                .stream()
                .map(classroom -> mapToDTO(classroom, new ClassroomDTO()))
                .collect(Collectors.toList()),
                pageable, page.getTotalElements());
    }

    public ClassroomDTO get(final UUID classroomId) {
        return classroomRepository.findById(classroomId)
                .map(classroom -> mapToDTO(classroom, new ClassroomDTO()))
                .orElseThrow(() -> new NotFoundException("Classroom not found"));
    }

    public ClassroomDTO create(final ClassroomDTO classroomDTO) {
        final Classroom classroom = new Classroom();
        mapToEntity(classroomDTO, classroom);
        Classroom savedClassroom = classroomRepository.save(classroom);
        return mapToDTO(savedClassroom, new ClassroomDTO());
    }

    public ClassroomDTO update(final UUID classroomId, final ClassroomDTO classroomDTO) {
        final Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new NotFoundException("Classroom not found"));
        mapToEntity(classroomDTO, classroom);
        Classroom savedClassroom = classroomRepository.save(classroom);
        return mapToDTO(savedClassroom, new ClassroomDTO());
    }

    public void delete(final UUID classroomId) {
        classroomRepository.deleteById(classroomId);
    }

    private ClassroomDTO mapToDTO(final Classroom classroom, final ClassroomDTO classroomDTO) {
        classroomDTO.setId(classroom.getId());
        classroomDTO.setName(classroom.getName());
        classroomDTO.setIsActive(classroom.getIsActive());
        classroomDTO.setEnrollmentStartDate(classroom.getEnrollmentStartDate());
        classroomDTO.setEnrollmentEndDate(classroom.getEnrollmentEndDate());
        classroomDTO.setCreatedAt(classroom.getCreatedAt());
        classroomDTO.setUpdatedAt(classroom.getUpdatedAt());
        classroomDTO.setCourse(classroom.getCourse() == null ? null : classroom.getCourse().getId());
        classroomDTO.setTeacher(classroom.getTeacher() == null ? null : classroom.getTeacher().getId());
        return classroomDTO;
    }

    private Classroom mapToEntity(final ClassroomDTO classroomDTO, final Classroom classroom) {
        if (classroomDTO.getName() != null) {
            classroom.setName(classroomDTO.getName());
        }
        if (classroomDTO.getIsActive() != null) {
            classroom.setIsActive(classroomDTO.getIsActive());
        }
        if (classroomDTO.getEnrollmentStartDate() != null) {
            classroom.setEnrollmentStartDate(classroomDTO.getEnrollmentStartDate());
        }
        if (classroomDTO.getEnrollmentEndDate() != null) {
            classroom.setEnrollmentEndDate(classroomDTO.getEnrollmentEndDate());
        }
        if (classroomDTO.getCourse() != null) {
            final Course course = courseRepository.findById(classroomDTO.getCourse())
                    .orElseThrow(() -> new NotFoundException("course not found"));
            classroom.setCourse(course);
        }
        if (classroomDTO.getTeacher() != null) {
            final User teacher = userRepository.findById(classroomDTO.getTeacher())
                    .orElseThrow(() -> new NotFoundException("teacher not found"));
            classroom.setTeacher(teacher);
        }
        if (classroomDTO.getCreatedAt() != null) {
            classroom.setCreatedAt(classroomDTO.getCreatedAt());
        }
        if (classroomDTO.getUpdatedAt() != null) {
            classroom.setUpdatedAt(classroomDTO.getUpdatedAt());
        }
        return classroom;
    }

    public ReferencedWarning getReferencedWarning(final UUID classroomId) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new NotFoundException("Classroom not found"));
        final StudentClassroom classroomStudentClassroom = studentClassroomRepository.findFirstByClassroom(classroom);
        if (classroomStudentClassroom != null) {
            referencedWarning.setKey("classroom.studentClassroom.classroom.referenced");
            referencedWarning.addParam(classroomStudentClassroom.getId());
            return referencedWarning;
        }

        return null;
    }
}

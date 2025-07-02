package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.domain.Classroom;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.model.dto.ClassroomDTO;
import io.adampoi.java_auto_grader.model.dto.UserDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.repository.ClassroomRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;

    public ClassroomService(final ClassroomRepository classroomRepository,
                            final UserRepository userRepository
    ) {
        this.classroomRepository = classroomRepository;
        this.userRepository = userRepository;
    }

    public PageResponse<ClassroomDTO> findAll(QueryFilter<Classroom> filter, Pageable pageable) {
        final Page<Classroom> page = classroomRepository.findAll(filter, pageable);
        Page<ClassroomDTO> dtoPage = new PageImpl<>(page.getContent()
                .stream()
                .map(classroom -> {

                    return mapToDTO(classroom, new ClassroomDTO());
                })
                .collect(Collectors.toList()),
                pageable, page.getTotalElements());
        return PageResponse.from(dtoPage);
    }

    public ClassroomDTO get(final UUID classroomId) {
        return classroomRepository.findById(classroomId)
                .map(classroom -> mapToDTO(classroom, new ClassroomDTO()))
                .orElseThrow(() -> new EntityNotFoundException("Classroom not found"));
    }

    public ClassroomDTO create(final ClassroomDTO request) {
        final Classroom classroom = new Classroom();
        mapToEntity(request, classroom);
        Classroom savedClassroom = classroomRepository.save(classroom);
        return mapToDTO(savedClassroom, new ClassroomDTO());
    }

    public ClassroomDTO update(final UUID classroomId, final ClassroomDTO classroomDTO) {
        final Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new EntityNotFoundException("Classroom not found"));
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
        classroomDTO.setCreatedAt(classroom.getCreatedAt());
        classroomDTO.setUpdatedAt(classroom.getUpdatedAt());
        classroomDTO.setEnrolledStudents(classroom.getEnrolledStudents().stream()
                .map(student -> UserService.mapToDTO(student, new UserDTO()))
                .collect(Collectors.toList()));

        classroomDTO.setTeacher(classroom.getTeacher() == null ? null :
                UserService.mapToDTO(classroom.getTeacher(), new UserDTO()));

        return classroomDTO;
    }


    private Classroom mapToEntity(final ClassroomDTO classroomDTO, final Classroom classroom) {
        if (classroomDTO.getName() != null) {
            classroom.setName(classroomDTO.getName());
        }

        if (classroomDTO.getId() != null) {
            classroom.setId(classroomDTO.getId());
        }
        if (classroomDTO.getTeacherId() != null) {
            final User teacher = userRepository.findById(classroomDTO.getTeacherId())
                    .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));
            classroom.setTeacher(teacher);
        }
        if (!classroomDTO.getStudentIds().isEmpty()) {
            Set<User> students = classroomDTO.getStudentIds().stream()
                    .map(studentId -> userRepository.findById(studentId)
                            .orElseThrow(() -> new EntityNotFoundException("Student not found: " + studentId)))
                    .collect(Collectors.toSet());
            classroom.setEnrolledStudents(students);
        }

        return classroom;
    }

    public ReferencedWarning getReferencedWarning(final UUID classroomId) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new EntityNotFoundException("Classroom not found"));


        return null;
    }
}

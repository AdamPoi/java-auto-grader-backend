package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.domain.Classroom;
import io.adampoi.java_auto_grader.domain.StudentClassroom;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.model.dto.StudentClassroomDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.repository.ClassroomRepository;
import io.adampoi.java_auto_grader.repository.StudentClassroomRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentClassroomService {

    private final StudentClassroomRepository studentClassroomRepository;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;

    public StudentClassroomService(final StudentClassroomRepository studentClassroomRepository,
                                   final UserRepository userRepository, final ClassroomRepository classroomRepository) {
        this.studentClassroomRepository = studentClassroomRepository;
        this.userRepository = userRepository;
        this.classroomRepository = classroomRepository;
    }

    public PageResponse<StudentClassroomDTO> findAll(QueryFilter<StudentClassroom> filter, Pageable pageable) {
        final Page<StudentClassroom> page = studentClassroomRepository.findAll(filter, pageable);
        Page<StudentClassroomDTO> dtoPage = new PageImpl<>(page.getContent()
                .stream()
                .map(studentClassroom -> mapToDTO(studentClassroom, new StudentClassroomDTO()))
                .collect(Collectors.toList()),
                pageable, page.getTotalElements());
        return PageResponse.from(dtoPage);
    }

    public StudentClassroomDTO get(final UUID id) {
        return studentClassroomRepository.findById(id)
                .map(studentClassroom -> mapToDTO(studentClassroom, new StudentClassroomDTO()))
                .orElseThrow(() -> new EntityNotFoundException("StudentClassroom not found"));
    }

    public StudentClassroomDTO create(final StudentClassroomDTO studentClassroomDTO) {
        final StudentClassroom studentClassroom = new StudentClassroom();
        mapToEntity(studentClassroomDTO, studentClassroom);
        StudentClassroom savedStudentClassroom = studentClassroomRepository.save(studentClassroom);
        return mapToDTO(savedStudentClassroom, new StudentClassroomDTO());
    }

    public StudentClassroomDTO update(final UUID id, final StudentClassroomDTO studentClassroomDTO) {
        final StudentClassroom studentClassroom = studentClassroomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("StudentClassroom not found"));
        mapToEntity(studentClassroomDTO, studentClassroom);
        StudentClassroom savedStudentClassroom = studentClassroomRepository.save(studentClassroom);
        return mapToDTO(savedStudentClassroom, new StudentClassroomDTO());
    }

    public void delete(final UUID id) {
        studentClassroomRepository.deleteById(id);
    }

    private StudentClassroomDTO mapToDTO(final StudentClassroom studentClassroom,
                                         final StudentClassroomDTO studentClassroomDTO) {
        studentClassroomDTO.setId(studentClassroom.getId());
        studentClassroomDTO.setEnrollmentDate(studentClassroom.getCreatedAt());
        studentClassroomDTO.setIsActive(studentClassroom.getIsActive());
        studentClassroomDTO
                .setStudent(studentClassroom.getStudent() == null ? null : studentClassroom.getStudent().getId());
        studentClassroomDTO
                .setClassroom(studentClassroom.getClassroom() == null ? null : studentClassroom.getClassroom().getId());
        return studentClassroomDTO;
    }

    private StudentClassroom mapToEntity(final StudentClassroomDTO studentClassroomDTO,
                                         final StudentClassroom studentClassroom) {
        if (studentClassroomDTO.getIsActive() != null) {
            studentClassroom.setIsActive(studentClassroomDTO.getIsActive());
        }
        if (studentClassroomDTO.getStudent() != null) {
            final User student = userRepository.findById(studentClassroomDTO.getStudent())
                    .orElseThrow(() -> new EntityNotFoundException("student not found"));
            studentClassroom.setStudent(student);
        }
        if (studentClassroomDTO.getClassroom() != null) {
            final Classroom classroom = classroomRepository.findById(studentClassroomDTO.getClassroom())
                    .orElseThrow(() -> new EntityNotFoundException("classroom not found"));
            studentClassroom.setClassroom(classroom);
        }
        if (studentClassroomDTO.getEnrollmentDate() != null) {
            studentClassroom.setCreatedAt(studentClassroomDTO.getEnrollmentDate());
        }
        return studentClassroom;
    }

    public ReferencedWarning getReferencedWarning(final UUID studentClassroomId) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final StudentClassroom studentClassroom = studentClassroomRepository.findById(studentClassroomId)
                .orElseThrow(() -> new EntityNotFoundException("StudentClassroom not found"));
        return null;
    }
}

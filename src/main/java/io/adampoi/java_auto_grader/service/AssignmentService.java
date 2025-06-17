package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.domain.*;
import io.adampoi.java_auto_grader.model.dto.AssignmentDTO;
import io.adampoi.java_auto_grader.model.dto.RubricGradeDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.repository.*;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final RubricGradeRepository rubricGradeRepository;

    public AssignmentService(final AssignmentRepository assignmentRepository,
                             final CourseRepository courseRepository, final UserRepository userRepository,
                             final SubmissionRepository submissionRepository, RubricGradeRepository rubricGradeRepository) {
        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
        this.rubricGradeRepository = rubricGradeRepository;
    }

    public static AssignmentDTO mapToDTO(final Assignment assignment, final AssignmentDTO assignmentDTO) {
        assignmentDTO.setId(assignment.getId());
        assignmentDTO.setTitle(assignment.getTitle());
        assignmentDTO.setDescription(assignment.getDescription());
        assignmentDTO.setDueDate(assignment.getDueDate());
        assignmentDTO.setCreatedAt(assignment.getCreatedAt());
        assignmentDTO.setUpdatedAt(assignment.getUpdatedAt());
        assignmentDTO.setIsPublished(assignment.getIsPublished());
        assignmentDTO.setStarterCode(assignment.getStarterCode());
        assignmentDTO.setSolutionCode(assignment.getSolutionCode());
        assignmentDTO.setTestCode(assignment.getTestCode());
        assignmentDTO.setTotalPoints(assignment.getTotalPoints());
        assignmentDTO.setMaxAttempts(assignment.getMaxAttempts());
        assignmentDTO.setTimeLimit(assignment.getTimeLimit());
        assignmentDTO.setCourseId(assignment.getCourse() == null ? null : assignment.getCourse().getId());
        assignmentDTO.setCreatedByTeacher(
                assignment.getCreatedByTeacher() == null ? null : assignment.getCreatedByTeacher().getId());
        return assignmentDTO;
    }

    public PageResponse<AssignmentDTO> findAll(QueryFilter<Assignment> filter, Pageable pageable) {
        final Page<Assignment> page = assignmentRepository.findAll(filter, pageable);

        Page<AssignmentDTO> dtoPage = new PageImpl<>(
                page.getContent()
                        .stream()
                        .map(assignment -> mapToDTO(assignment, new AssignmentDTO()))
                        .collect(Collectors.toList()),
                pageable,
                page.getTotalElements());
        return PageResponse.from(dtoPage);
    }

    @SneakyThrows
    public AssignmentDTO get(final UUID assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .map(assignment -> mapToDTO(assignment, new AssignmentDTO()))
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));
    }

    public AssignmentDTO create(final AssignmentDTO assignmentDTO) {
        final Assignment assignment = new Assignment();
        mapToEntity(assignmentDTO, assignment);
        Assignment savedAssignment = assignmentRepository.save(assignment);
        return mapToDTO(savedAssignment, new AssignmentDTO());
    }

    @SneakyThrows
    public AssignmentDTO update(final UUID assignmentId, final AssignmentDTO assignmentDTO) {
        final Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));
        mapToEntity(assignmentDTO, assignment);
        Assignment savedAssignment = assignmentRepository.save(assignment);
        return mapToDTO(savedAssignment, new AssignmentDTO());
    }

    public void delete(final UUID assignmentId) {
        assignmentRepository.deleteById(assignmentId);
    }

    public PageResponse<RubricGradeDTO> getAssignmentRubricGrades(final UUID assignmentId, QueryFilter<RubricGrade> filter, Pageable pageable) {
        filter.addNewField("assignment", QFOperationEnum.EQUAL, assignmentId.toString());
        Page<RubricGrade> assignmentPage = rubricGradeRepository.findAll(filter, pageable);

        Page<RubricGradeDTO> dtoPage = new PageImpl<>(assignmentPage.getContent()
                .stream()
                .map(assignment -> RubricGradeService.mapToDTO(assignment, new RubricGradeDTO()))
                .collect(Collectors.toList()),
                pageable, assignmentPage.getTotalElements());

        return PageResponse.from(dtoPage);
    }

    private Assignment mapToEntity(final AssignmentDTO assignmentDTO, final Assignment assignment) {
        if (assignmentDTO.getTitle() != null) {
            assignment.setTitle(assignmentDTO.getTitle());
        }
        if (assignmentDTO.getDescription() != null) {
            assignment.setDescription(assignmentDTO.getDescription());
        }
        if (assignmentDTO.getDueDate() != null) {
            assignment.setDueDate(assignmentDTO.getDueDate());
        }
        if (assignmentDTO.getIsPublished() != null) {
            assignment.setIsPublished(assignmentDTO.getIsPublished());
        }
        if (assignmentDTO.getStarterCode() != null) {
            assignment.setStarterCode(assignmentDTO.getStarterCode());
        }
        if (assignmentDTO.getSolutionCode() != null) {
            assignment.setSolutionCode(assignmentDTO.getSolutionCode());
        }
        if (assignmentDTO.getTestCode() != null) {
            assignment.setTestCode(assignmentDTO.getTestCode());
        }
        if (assignmentDTO.getMaxAttempts() != null) {
            assignment.setMaxAttempts(assignmentDTO.getMaxAttempts());
        }
        if (assignmentDTO.getTimeLimit() != null) {
            assignment.setTimeLimit(assignmentDTO.getTimeLimit());
        }
        if (assignmentDTO.getTotalPoints() != null) {
            assignment.setTotalPoints(assignmentDTO.getTotalPoints());
        }
        if (assignmentDTO.getCourseId() != null) {
            final Course course = courseRepository.findById(assignmentDTO.getCourseId())
                    .orElseThrow(() -> new EntityNotFoundException("course not found"));
            assignment.setCourse(course);
        }
        if (assignmentDTO.getCreatedByTeacher() != null) {
            final User createdByTeacher = userRepository.findById(assignmentDTO.getCreatedByTeacher())
                    .orElseThrow(() -> new EntityNotFoundException("createdByTeacher not found"));
            assignment.setCreatedByTeacher(createdByTeacher);
        }
        if (assignmentDTO.getCreatedAt() != null) {
            assignment.setCreatedAt(assignmentDTO.getCreatedAt());
        }
        if (assignmentDTO.getUpdatedAt() != null) {
            assignment.setUpdatedAt(assignmentDTO.getUpdatedAt());
        }
        return assignment;
    }

    public ReferencedWarning getReferencedWarning(final UUID assignmentId) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));

        final Submission assignmentSubmission = submissionRepository.findFirstByAssignment(assignment);
        if (assignmentSubmission != null) {
            referencedWarning.setKey("assignment.submission.assignment.referenced");
            referencedWarning.addParam(assignmentSubmission.getId());
            return referencedWarning;
        }
        return null;
    }
}

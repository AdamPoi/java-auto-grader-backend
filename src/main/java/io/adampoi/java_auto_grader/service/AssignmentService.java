package io.adampoi.java_auto_grader.service;

import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.adampoi.java_auto_grader.domain.Assignment;
import io.adampoi.java_auto_grader.domain.Course;
import io.adampoi.java_auto_grader.domain.Submission;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.model.dto.AssignmentDTO;
import io.adampoi.java_auto_grader.repository.AssignmentRepository;
import io.adampoi.java_auto_grader.repository.CourseRepository;
import io.adampoi.java_auto_grader.repository.SubmissionRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import io.adampoi.java_auto_grader.util.NotFoundException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;

    public AssignmentService(final AssignmentRepository assignmentRepository,
            final CourseRepository courseRepository, final UserRepository userRepository,
            final SubmissionRepository submissionRepository) {
        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
    }

    public Page<AssignmentDTO> findAll(QueryFilter<Assignment> filter, Pageable pageable) {
        final Page<Assignment> page = assignmentRepository.findAll(filter, pageable);

        Page<AssignmentDTO> dtoPage = new PageImpl<>(
                page.getContent()
                        .stream()
                        .map(assignment -> mapToDTO(assignment, new AssignmentDTO()))
                        .collect(Collectors.toList()),
                pageable,
                page.getTotalElements());
        return dtoPage;
    }

    public AssignmentDTO get(final UUID assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .map(assignment -> mapToDTO(assignment, new AssignmentDTO()))
                .orElseThrow(() -> new NotFoundException("Assignment not found"));
    }

    public AssignmentDTO create(final AssignmentDTO assignmentDTO) {
        final Assignment assignment = new Assignment();
        mapToEntity(assignmentDTO, assignment);
        Assignment savedAssignment = assignmentRepository.save(assignment);
        return mapToDTO(savedAssignment, new AssignmentDTO());
    }

    public AssignmentDTO update(final UUID assignmentId, final AssignmentDTO assignmentDTO) {
        final Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment not found"));
        mapToEntity(assignmentDTO, assignment);
        Assignment savedAssignment = assignmentRepository.save(assignment);
        return mapToDTO(savedAssignment, new AssignmentDTO());
    }

    public void delete(final UUID assignmentId) {
        assignmentRepository.deleteById(assignmentId);
    }

    private AssignmentDTO mapToDTO(final Assignment assignment, final AssignmentDTO assignmentDTO) {
        assignmentDTO.setId(assignment.getId());
        assignmentDTO.setTitle(assignment.getTitle());
        assignmentDTO.setDescription(assignment.getDescription());
        assignmentDTO.setDueDate(assignment.getDueDate());
        assignmentDTO.setCreatedAt(assignment.getCreatedAt());
        assignmentDTO.setUpdatedAt(assignment.getUpdatedAt());
        assignmentDTO.setIsPublished(assignment.getIsPublished());
        assignmentDTO.setStarterCodeBasePath(assignment.getStarterCodeBasePath());
        assignmentDTO.setSolutionCodeBasePath(assignment.getSolutionCodeBasePath());
        assignmentDTO.setMaxAttempts(assignment.getMaxAttempts());
        assignmentDTO.setCourse(assignment.getCourse() == null ? null : assignment.getCourse().getId());
        assignmentDTO.setCreatedByTeacher(
                assignment.getCreatedByTeacher() == null ? null : assignment.getCreatedByTeacher().getId());
        return assignmentDTO;
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
        if (assignmentDTO.getStarterCodeBasePath() != null) {
            assignment.setStarterCodeBasePath(assignmentDTO.getStarterCodeBasePath());
        }
        if (assignmentDTO.getSolutionCodeBasePath() != null) {
            assignment.setSolutionCodeBasePath(assignmentDTO.getSolutionCodeBasePath());
        }
        if (assignmentDTO.getMaxAttempts() != null) {
            assignment.setMaxAttempts(assignmentDTO.getMaxAttempts());
        }
        if (assignmentDTO.getCourse() != null) {
            final Course course = courseRepository.findById(assignmentDTO.getCourse())
                    .orElseThrow(() -> new NotFoundException("course not found"));
            assignment.setCourse(course);
        }
        if (assignmentDTO.getCreatedByTeacher() != null) {
            final User createdByTeacher = userRepository.findById(assignmentDTO.getCreatedByTeacher())
                    .orElseThrow(() -> new NotFoundException("createdByTeacher not found"));
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
                .orElseThrow(() -> new NotFoundException("Assignment not found"));

        final Submission assignmentSubmission = submissionRepository.findFirstByAssignment(assignment);
        if (assignmentSubmission != null) {
            referencedWarning.setKey("assignment.submission.assignment.referenced");
            referencedWarning.addParam(assignmentSubmission.getId());
            return referencedWarning;
        }
        return null;
    }
}

package io.adampoi.java_auto_grader.service;

import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.adampoi.java_auto_grader.domain.GradeExecution;
import io.adampoi.java_auto_grader.domain.RubricGrade;
import io.adampoi.java_auto_grader.domain.Submission;
import io.adampoi.java_auto_grader.model.dto.GradeExecutionDTO;
import io.adampoi.java_auto_grader.repository.GradeExecutionRepository;
import io.adampoi.java_auto_grader.repository.RubricGradeRepository;
import io.adampoi.java_auto_grader.repository.SubmissionRepository;
import io.adampoi.java_auto_grader.util.NotFoundException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class GradeExecutionService {

    private final GradeExecutionRepository gradeExecutionRepository;
    private final RubricGradeRepository rubricGradeRepository;
    private final SubmissionRepository submissionRepository;

    public GradeExecutionService(final GradeExecutionRepository gradeExecutionRepository,
            final RubricGradeRepository rubricGradeRepository,
            final SubmissionRepository submissionRepository) {
        this.gradeExecutionRepository = gradeExecutionRepository;
        this.rubricGradeRepository = rubricGradeRepository;
        this.submissionRepository = submissionRepository;
    }

    public Page<GradeExecutionDTO> findAll(QueryFilter<GradeExecution> filter, Pageable pageable) {
        final Page<GradeExecution> page = gradeExecutionRepository.findAll(filter, pageable);
        return new PageImpl<>(page.getContent()
                .stream()
                .map(gradeExecution -> mapToDTO(gradeExecution, new GradeExecutionDTO()))
                .collect(Collectors.toList()),
                pageable, page.getTotalElements());
    }

    public GradeExecutionDTO get(final UUID gradeExecutionId) {
        return gradeExecutionRepository.findById(gradeExecutionId)
                .map(gradeExecution -> mapToDTO(gradeExecution, new GradeExecutionDTO()))
                .orElseThrow(() -> new NotFoundException("GradeExecution not found"));
    }

    public GradeExecutionDTO create(final GradeExecutionDTO gradeExecutionDTO) {
        final GradeExecution gradeExecution = new GradeExecution();
        mapToEntity(gradeExecutionDTO, gradeExecution);
        GradeExecution savedGradeExecution = gradeExecutionRepository.save(gradeExecution);
        return mapToDTO(savedGradeExecution, new GradeExecutionDTO());
    }

    public GradeExecutionDTO update(final UUID gradeExecutionId, final GradeExecutionDTO gradeExecutionDTO) {
        final GradeExecution gradeExecution = gradeExecutionRepository.findById(gradeExecutionId)
                .orElseThrow(() -> new NotFoundException("GradeExecution not found"));
        mapToEntity(gradeExecutionDTO, gradeExecution);
        GradeExecution savedGradeExecution = gradeExecutionRepository.save(gradeExecution);
        return mapToDTO(savedGradeExecution, new GradeExecutionDTO());
    }

    public void delete(final UUID gradeExecutionId) {
        gradeExecutionRepository.deleteById(gradeExecutionId);
    }

    private GradeExecutionDTO mapToDTO(final GradeExecution gradeExecution, final GradeExecutionDTO gradeExecutionDTO) {
        gradeExecutionDTO.setId(gradeExecution.getId());
        gradeExecutionDTO.setPointsAwarded(gradeExecution.getPointsAwarded());
        gradeExecutionDTO.setStatus(gradeExecution.getStatus());
        gradeExecutionDTO.setActual(gradeExecution.getActual());
        gradeExecutionDTO.setExpected(gradeExecution.getExpected());
        gradeExecutionDTO.setError(gradeExecution.getError());
        gradeExecutionDTO.setExecutionTime(gradeExecution.getExecutionTime());
        gradeExecutionDTO.setRubricGrade(
                gradeExecution.getRubricGrade() == null ? null : gradeExecution.getRubricGrade().getId());
        gradeExecutionDTO
                .setSubmission(gradeExecution.getSubmission() == null ? null : gradeExecution.getSubmission().getId());
        gradeExecutionDTO.setCreatedAt(gradeExecution.getCreatedAt());
        gradeExecutionDTO.setUpdatedAt(gradeExecution.getUpdatedAt());
        return gradeExecutionDTO;
    }

    private GradeExecution mapToEntity(final GradeExecutionDTO gradeExecutionDTO, final GradeExecution gradeExecution) {
        if (gradeExecutionDTO.getPointsAwarded() != null) {
            gradeExecution.setPointsAwarded(gradeExecutionDTO.getPointsAwarded());
        }
        if (gradeExecutionDTO.getStatus() != null) {
            gradeExecution.setStatus(gradeExecutionDTO.getStatus());
        }
        if (gradeExecutionDTO.getActual() != null) {
            gradeExecution.setActual(gradeExecutionDTO.getActual());
        }
        if (gradeExecutionDTO.getExpected() != null) {
            gradeExecution.setExpected(gradeExecutionDTO.getExpected());
        }
        if (gradeExecutionDTO.getError() != null) {
            gradeExecution.setError(gradeExecutionDTO.getError());
        }
        if (gradeExecutionDTO.getExecutionTime() != null) {
            gradeExecution.setExecutionTime(gradeExecutionDTO.getExecutionTime());
        }
        if (gradeExecutionDTO.getRubricGrade() != null) {
            final RubricGrade rubricGrade = rubricGradeRepository.findById(gradeExecutionDTO.getRubricGrade())
                    .orElseThrow(() -> new NotFoundException("RubricGrade not found"));
            gradeExecution.setRubricGrade(rubricGrade);
        }
        if (gradeExecutionDTO.getSubmission() != null) {
            final Submission submission = submissionRepository.findById(gradeExecutionDTO.getSubmission())
                    .orElseThrow(() -> new NotFoundException("Submission not found"));
            gradeExecution.setSubmission(submission);
        }
        if (gradeExecutionDTO.getCreatedAt() != null) {
            gradeExecution.setCreatedAt(gradeExecutionDTO.getCreatedAt());
        }
        if (gradeExecutionDTO.getUpdatedAt() != null) {
            gradeExecution.setUpdatedAt(gradeExecutionDTO.getUpdatedAt());
        }
        return gradeExecution;
    }

    public ReferencedWarning getReferencedWarning(final UUID gradeExecutionId) {
        // Implement logic to check for referenced entities if necessary
        return null;
    }
}

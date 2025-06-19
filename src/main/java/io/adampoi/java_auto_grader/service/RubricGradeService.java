package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.domain.Assignment;
import io.adampoi.java_auto_grader.domain.GradeExecution;
import io.adampoi.java_auto_grader.domain.Rubric;
import io.adampoi.java_auto_grader.domain.RubricGrade;
import io.adampoi.java_auto_grader.model.dto.RubricGradeDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.repository.AssignmentRepository;
import io.adampoi.java_auto_grader.repository.GradeExecutionRepository;
import io.adampoi.java_auto_grader.repository.RubricGradeRepository;
import io.adampoi.java_auto_grader.repository.RubricRepository;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class RubricGradeService {

    private final RubricGradeRepository rubricGradeRepository;
    private final RubricRepository rubricRepository;
    private final GradeExecutionRepository gradeExecutionRepository;
    private final AssignmentRepository assignmentRepository;

    public RubricGradeService(final RubricGradeRepository rubricGradeRepository,
                              final RubricRepository rubricRepository,
                              final GradeExecutionRepository gradeExecutionRepository, AssignmentRepository assignmentRepository) {
        this.rubricGradeRepository = rubricGradeRepository;
        this.rubricRepository = rubricRepository;
        this.gradeExecutionRepository = gradeExecutionRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public static RubricGradeDTO mapToDTO(final RubricGrade rubricGrade, final RubricGradeDTO rubricGradeDTO) {
        rubricGradeDTO.setId(rubricGrade.getId());
        rubricGradeDTO.setName(rubricGrade.getName());
        rubricGradeDTO.setDescription(rubricGrade.getDescription());
        rubricGradeDTO.setDisplayOrder(rubricGrade.getDisplayOrder());
        rubricGradeDTO.setArguments(rubricGrade.getArguments());
        rubricGradeDTO.setGradeType(rubricGrade.getGradeType());
        rubricGradeDTO.setRubricId(Optional.ofNullable(rubricGrade.getRubric())
                .map(Rubric::getId)
                .orElse(null));
        rubricGradeDTO.setAssignmentId(Optional.ofNullable(rubricGrade.getRubric())
                .map(Rubric::getId)
                .orElse(null));
        rubricGradeDTO.setGradeExecutionIds(Optional.ofNullable(rubricGrade.getGradeExecutions())
                .map(executions -> executions.stream()
                        .map(GradeExecution::getId)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet()));
        rubricGradeDTO.setCreatedAt(rubricGrade.getCreatedAt());
        rubricGradeDTO.setUpdatedAt(rubricGrade.getUpdatedAt());
        return rubricGradeDTO;
    }

    public PageResponse<RubricGradeDTO> findAll(QueryFilter<RubricGrade> filter, Pageable pageable) {
        final Page<RubricGrade> page = rubricGradeRepository.findAll(filter, pageable);
        Page<RubricGradeDTO> dtoPage = new PageImpl<>(page.getContent()
                .stream()
                .map(rubricGrade -> mapToDTO(rubricGrade, new RubricGradeDTO()))
                .collect(Collectors.toList()),
                pageable, page.getTotalElements());
        return PageResponse.from(dtoPage);
    }

    public RubricGradeDTO get(final UUID rubricGradeId) {
        return rubricGradeRepository.findById(rubricGradeId)
                .map(rubricGrade -> mapToDTO(rubricGrade, new RubricGradeDTO()))
                .orElseThrow(() -> new EntityNotFoundException("RubricGrade not found"));
    }

    public RubricGradeDTO create(final RubricGradeDTO rubricGradeDTO) {
        final RubricGrade rubricGrade = new RubricGrade();
        // Set ID if provided in DTO
        if (rubricGradeDTO.getId() != null) {
            rubricGrade.setId(rubricGradeDTO.getId());

            // Check if a RubricGrade with this UUID already exists
            Optional<RubricGrade> existingRubricGrade = rubricGradeRepository.findById(rubricGradeDTO.getId());

            if (existingRubricGrade.isPresent()) {
                // Update the existing RubricGrade
                RubricGrade existing = existingRubricGrade.get();
                mapToEntity(rubricGradeDTO, existing);
                RubricGrade updatedRubricGrade = rubricGradeRepository.save(existing);
                return mapToDTO(updatedRubricGrade, new RubricGradeDTO());
            }
        }

        mapToEntity(rubricGradeDTO, rubricGrade);

        // Save the new RubricGrade (UUID will be auto-generated if not provided)
        RubricGrade savedRubricGrade = rubricGradeRepository.save(rubricGrade);
        return mapToDTO(savedRubricGrade, new RubricGradeDTO());
    }

    public RubricGradeDTO update(final UUID rubricGradeId, final RubricGradeDTO rubricGradeDTO) {
        final RubricGrade rubricGrade = rubricGradeRepository.findById(rubricGradeId)
                .orElseThrow(() -> new EntityNotFoundException("RubricGrade not found"));
        mapToEntity(rubricGradeDTO, rubricGrade);
        RubricGrade savedRubricGrade = rubricGradeRepository.save(rubricGrade);
        return mapToDTO(savedRubricGrade, new RubricGradeDTO());
    }

    public void delete(final UUID rubricGradeId) {
        rubricGradeRepository.deleteById(rubricGradeId);
    }

    private RubricGrade mapToEntity(final RubricGradeDTO rubricGradeDTO, final RubricGrade rubricGrade) {
        if (rubricGradeDTO.getId() != null) {
            rubricGrade.setId(rubricGradeDTO.getId());
        }
        if (rubricGradeDTO.getName() != null) {
            rubricGrade.setName(rubricGradeDTO.getName());
        }

        if (rubricGradeDTO.getDescription() != null) {
            rubricGrade.setDescription(rubricGradeDTO.getDescription());
        }

        if (rubricGradeDTO.getDisplayOrder() != null) {
            rubricGrade.setDisplayOrder(rubricGradeDTO.getDisplayOrder());
        }

        if (rubricGradeDTO.getArguments() != null) {
            rubricGrade.setArguments(rubricGradeDTO.getArguments());
        }
        if (rubricGradeDTO.getGradeType() != null) {
            rubricGrade.setGradeType(rubricGradeDTO.getGradeType());
        }
        if (rubricGradeDTO.getRubricId() != null) {
            final Rubric rubric = rubricRepository.findById(rubricGradeDTO.getRubricId())
                    .orElseThrow(() -> new EntityNotFoundException("Rubric not found"));
            rubricGrade.setRubric(rubric);
        }
        if (rubricGradeDTO.getAssignmentId() != null) {
            final Assignment rubric = assignmentRepository.findById(rubricGradeDTO.getAssignmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));
            rubricGrade.setAssignment(rubric);
        }


        return rubricGrade;
    }

    public ReferencedWarning getReferencedWarning(final UUID rubricGradeId) {
        // TODO: Implement logic to check for referenced entities if necessary
        return null;
    }
}

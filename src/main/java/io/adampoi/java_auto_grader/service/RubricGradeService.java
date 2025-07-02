package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.domain.Assignment;
import io.adampoi.java_auto_grader.domain.Rubric;
import io.adampoi.java_auto_grader.domain.RubricGrade;
import io.adampoi.java_auto_grader.model.dto.RubricGradeDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.repository.AssignmentRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class RubricGradeService {

    private final RubricGradeRepository rubricGradeRepository;
    private final RubricRepository rubricRepository;
    private final AssignmentRepository assignmentRepository;

    public RubricGradeService(final RubricGradeRepository rubricGradeRepository,
                              final RubricRepository rubricRepository, AssignmentRepository assignmentRepository) {
        this.rubricGradeRepository = rubricGradeRepository;
        this.rubricRepository = rubricRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public static RubricGradeDTO mapToDTO(final RubricGrade rubricGrade, final RubricGradeDTO rubricGradeDTO) {
        rubricGradeDTO.setId(String.valueOf(rubricGrade.getId()));
        rubricGradeDTO.setName(rubricGrade.getName());
        rubricGradeDTO.setGradeType(rubricGrade.getGradeType());
        rubricGradeDTO.setRubricId(Optional.ofNullable(rubricGrade.getRubric())
                .map(Rubric::getId)
                .orElse(null));
//        rubricGradeDTO.setAssignmentId(Optional.ofNullable(rubricGrade.getRubric())
//                .map(Rubric::getId)
//                .orElse(null));
//        rubricGradeDTO.setTestExecutionIds(Optional.ofNullable(rubricGrade.getTestExecutions())
//                .map(executions -> executions.stream()
//                        .map(TestExecution::getId)
//                        .collect(Collectors.toSet()))
//                .orElse(Collections.emptySet()));
//        rubricGradeDTO.setCreatedAt(rubricGrade.getCreatedAt());
//        rubricGradeDTO.setUpdatedAt(rubricGrade.getUpdatedAt());
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
        if (rubricGradeDTO.getId() != null) {
            rubricGrade.setId(UUID.fromString(rubricGradeDTO.getId()));

            Optional<RubricGrade> existingRubricGrade = rubricGradeRepository.findById(UUID.fromString(rubricGradeDTO.getId()));

            if (existingRubricGrade.isPresent()) {
                RubricGrade existing = existingRubricGrade.get();
                mapToEntity(rubricGradeDTO, existing);
                RubricGrade updatedRubricGrade = rubricGradeRepository.save(existing);
                return mapToDTO(updatedRubricGrade, new RubricGradeDTO());
            }
        }

        mapToEntity(rubricGradeDTO, rubricGrade);

        RubricGrade savedRubricGrade = rubricGradeRepository.save(rubricGrade);
        return mapToDTO(savedRubricGrade, new RubricGradeDTO());
    }

    public List<RubricGradeDTO> saveManyByAssignment(UUID assignmentId, final List<RubricGradeDTO> rubricGradeDTOs) {

        // First, find existing grades and clear their test execution associations
        List<RubricGrade> existingGrades = rubricGradeRepository.findByAssignmentId(assignmentId);
        for (RubricGrade grade : existingGrades) {
            if (grade.getTestExecutions() != null) {
                grade.getTestExecutions().clear();
            }
        }

        // Save to clear associations, then flush to ensure changes are persisted
        if (!existingGrades.isEmpty()) {
            rubricGradeRepository.saveAll(existingGrades);
            rubricGradeRepository.flush();
        }

        // Now delete the rubric grades
        rubricGradeRepository.deleteByAssignmentId(assignmentId);
        rubricGradeRepository.flush(); // Ensure deletion is flushed

        List<RubricGradeDTO> savedRubricGrades = new ArrayList<>();
        for (RubricGradeDTO rubricGradeDTO : rubricGradeDTOs) {
            RubricGrade rubricGrade = new RubricGrade();

            // Make sure to properly set all required references
            mapToEntityWithoutId(rubricGradeDTO, rubricGrade);

            // Ensure the assignment reference is properly set with a managed entity
            if (rubricGrade.getAssignment() == null || !rubricGrade.getAssignment().getId().equals(assignmentId)) {
                // You might need to fetch the assignment entity here if not properly set in mapToEntityWithoutId
                Assignment assignment = assignmentRepository.findById(assignmentId)
                        .orElseThrow(() -> new RuntimeException("Assignment not found"));
                rubricGrade.setAssignment(assignment);
            }

            RubricGrade savedRubricGrade = rubricGradeRepository.save(rubricGrade);
            savedRubricGrades.add(mapToDTO(savedRubricGrade, new RubricGradeDTO()));
        }

        return savedRubricGrades;
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
            rubricGrade.setId(UUID.fromString(rubricGradeDTO.getId()));
        }
        if (rubricGradeDTO.getName() != null) {
            rubricGrade.setName(rubricGradeDTO.getName());
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
            final Assignment assignment = assignmentRepository.findById(rubricGradeDTO.getAssignmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));
            rubricGrade.setAssignment(assignment);
        }


        return rubricGrade;
    }

    private RubricGrade mapToEntityWithoutId(final RubricGradeDTO rubricGradeDTO, final RubricGrade rubricGrade) {
        // Don't set ID - let JPA generate it
        if (rubricGradeDTO.getName() != null) {
            rubricGrade.setName(rubricGradeDTO.getName());
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
            final Assignment assignment = assignmentRepository.findById(rubricGradeDTO.getAssignmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));
            rubricGrade.setAssignment(assignment);
        }

        return rubricGrade;
    }

    public ReferencedWarning getReferencedWarning(final UUID rubricGradeId) {
        // TODO: Implement logic to check for referenced entities if necessary
        return null;
    }
}

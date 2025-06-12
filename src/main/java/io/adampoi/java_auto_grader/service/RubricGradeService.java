package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.domain.GradeExecution;
import io.adampoi.java_auto_grader.domain.Rubric;
import io.adampoi.java_auto_grader.domain.RubricGrade;
import io.adampoi.java_auto_grader.model.dto.RubricGradeDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
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

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class RubricGradeService {

    private final RubricGradeRepository rubricGradeRepository;
    private final RubricRepository rubricRepository;
    private final GradeExecutionRepository gradeExecutionRepository;

    public RubricGradeService(final RubricGradeRepository rubricGradeRepository,
                              final RubricRepository rubricRepository,
                              final GradeExecutionRepository gradeExecutionRepository) {
        this.rubricGradeRepository = rubricGradeRepository;
        this.rubricRepository = rubricRepository;
        this.gradeExecutionRepository = gradeExecutionRepository;
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
        mapToEntity(rubricGradeDTO, rubricGrade);
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

    private RubricGradeDTO mapToDTO(final RubricGrade rubricGrade, final RubricGradeDTO rubricGradeDTO) {
        rubricGradeDTO.setId(rubricGrade.getId());
        rubricGradeDTO.setName(rubricGrade.getName());
        rubricGradeDTO.setDescription(rubricGrade.getDescription());
        rubricGradeDTO.setPoints(rubricGrade.getPoints());
        rubricGradeDTO.setDisplayOrder(rubricGrade.getDisplayOrder());
        rubricGradeDTO.setCode(rubricGrade.getCode());
        rubricGradeDTO.setArguments(rubricGrade.getArguments());
        rubricGradeDTO.setGradeType(rubricGrade.getGradeType());
        rubricGradeDTO.setRubric(rubricGrade.getRubric() == null ? null : rubricGrade.getRubric().getId());
        rubricGradeDTO.setGradeExecutions(rubricGrade.getGradeExecutions().stream()
                .map(GradeExecution::getId)
                .collect(Collectors.toSet()));
        rubricGradeDTO.setCreatedAt(rubricGrade.getCreatedAt());
        rubricGradeDTO.setUpdatedAt(rubricGrade.getUpdatedAt());
        return rubricGradeDTO;
    }

    private RubricGrade mapToEntity(final RubricGradeDTO rubricGradeDTO, final RubricGrade rubricGrade) {
        if (rubricGradeDTO.getName() != null) {
            rubricGrade.setName(rubricGradeDTO.getName());
        }
        if (rubricGradeDTO.getDescription() != null) {
            rubricGrade.setDescription(rubricGradeDTO.getDescription());
        }
        if (rubricGradeDTO.getPoints() != null) {
            rubricGrade.setPoints(rubricGradeDTO.getPoints());
        }
        if (rubricGradeDTO.getDisplayOrder() != null) {
            rubricGrade.setDisplayOrder(rubricGradeDTO.getDisplayOrder());
        }
        if (rubricGradeDTO.getCode() != null) {
            rubricGrade.setCode(rubricGradeDTO.getCode());
        }
        if (rubricGradeDTO.getArguments() != null) {
            rubricGrade.setArguments(rubricGradeDTO.getArguments());
        }
        if (rubricGradeDTO.getGradeType() != null) {
            rubricGrade.setGradeType(rubricGradeDTO.getGradeType());
        }
        if (rubricGradeDTO.getRubric() != null) {
            final Rubric rubric = rubricRepository.findById(rubricGradeDTO.getRubric())
                    .orElseThrow(() -> new EntityNotFoundException("Rubric not found"));
            rubricGrade.setRubric(rubric);
        }
        if (rubricGradeDTO.getCreatedAt() != null) {
            rubricGrade.setCreatedAt(rubricGradeDTO.getCreatedAt());
        }
        if (rubricGradeDTO.getUpdatedAt() != null) {
            rubricGrade.setUpdatedAt(rubricGradeDTO.getUpdatedAt());
        }
        return rubricGrade;
    }

    public ReferencedWarning getReferencedWarning(final UUID rubricGradeId) {
        // TODO: Implement logic to check for referenced entities if necessary
        return null;
    }
}

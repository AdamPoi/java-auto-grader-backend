package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.domain.Assignment;
import io.adampoi.java_auto_grader.domain.Rubric;
import io.adampoi.java_auto_grader.model.dto.AssignmentDTO;
import io.adampoi.java_auto_grader.model.dto.RubricDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.repository.AssignmentRepository;
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
public class RubricService {

    private final RubricRepository rubricRepository;
    private final AssignmentRepository assignmentRepository;

    public RubricService(final RubricRepository rubricRepository, AssignmentRepository assignmentRepository) {
        this.rubricRepository = rubricRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public static RubricDTO mapToRelationshipDTO(final Rubric rubric, final RubricDTO rubricDTO) {
        rubricDTO.setId(rubric.getId());
        rubricDTO.setName(rubric.getName());
        rubricDTO.setDescription(rubric.getDescription());
        rubricDTO.setPoints(rubric.getPoints());
        rubricDTO.setCreatedAt(rubric.getCreatedAt());
        rubricDTO.setUpdatedAt(rubric.getUpdatedAt());
        return rubricDTO;
    }

    private RubricDTO mapToDTO(final Rubric rubric, final RubricDTO rubricDTO) {
        rubricDTO.setId(rubric.getId());
        rubricDTO.setName(rubric.getName());
        rubricDTO.setDescription(rubric.getDescription());
        rubricDTO.setPoints(rubric.getPoints());
        rubricDTO.setAssignment(rubric.getAssignment() == null ? null :
                AssignmentService.mapToDTO(rubric.getAssignment(), new AssignmentDTO()));

//        rubricDTO.setRubricGrades(rubric.getRubricGrades().stream()
//                .map(rubricGrade -> RubricGradeService.mapToDTO(rubricGrade, new RubricGradeDTO()))
//                .collect(Collectors.toList()));
        rubricDTO.setCreatedAt(rubric.getCreatedAt());
        rubricDTO.setUpdatedAt(rubric.getUpdatedAt());
        return rubricDTO;
    }

    public PageResponse<RubricDTO> findAll(QueryFilter<Rubric> filter, Pageable pageable) {
        final Page<Rubric> page = rubricRepository.findAll(filter, pageable);

        Page<RubricDTO> dtoPage = new PageImpl<>(
                page.getContent()
                        .stream()
                        .map(rubric -> mapToDTO(rubric, new RubricDTO()))
                        .collect(Collectors.toList()),
                pageable,
                page.getTotalElements());
        return PageResponse.from(dtoPage);
    }


    public RubricDTO get(final UUID rubricId) {
        return rubricRepository.findById(rubricId)
                .map(rubric -> mapToDTO(rubric, new RubricDTO()))
                .orElseThrow(() -> new EntityNotFoundException("Rubric not found"));
    }

    public RubricDTO create(final RubricDTO rubricDTO) {
        final Rubric rubric = new Rubric();
        mapToEntity(rubricDTO, rubric);
        Rubric savedRubric = rubricRepository.save(rubric);
        return mapToDTO(savedRubric, new RubricDTO());

    }

    public RubricDTO update(final UUID rubricId, final RubricDTO rubricDTO) {
        final Rubric rubric = rubricRepository.findById(rubricId)
                .orElseThrow(() -> new EntityNotFoundException("Rubric not found"));
        mapToEntity(rubricDTO, rubric);
        Rubric savedRubric = rubricRepository.save(rubric);
        return mapToDTO(savedRubric, new RubricDTO());

    }

    public void delete(final UUID rubricId) {
        rubricRepository.deleteById(rubricId);
    }

    private Rubric mapToEntity(final RubricDTO rubricDTO, final Rubric rubric) {
        if (rubricDTO.getName() != null) {
            rubric.setName(rubricDTO.getName());
        }
        if (rubricDTO.getDescription() != null) {
            rubric.setDescription(rubricDTO.getDescription());
        }
        if (rubricDTO.getPoints() > 0) {
            rubric.setPoints(rubricDTO.getPoints());
        }


        if (rubricDTO.getAssignmentId() != null) {
            final Assignment assignment = assignmentRepository.findById(rubricDTO.getAssignmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));
            rubric.setAssignment(assignment);
        }
        // rubric.setAssignment(assignmentRepository.findById(rubricDTO.getAssignment()).orElse(null));
        // rubric.setRubricGrades(rubricDTO.getRubricGrades().stream()
        // .map(rubricGradeId ->
        // rubricGradeRepository.findById(rubricGradeId).orElse(null))
        // .filter(java.util.Objects::nonNull)
        // .collect(Collectors.toSet()));

        return rubric;
    }

    public ReferencedWarning getReferencedWarning(final UUID rubricId) {
        // TODO: Implement logic to check for referenced entities if necessary
        return null;
    }
}

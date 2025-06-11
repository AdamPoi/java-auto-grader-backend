package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.domain.Submission;
import io.adampoi.java_auto_grader.domain.SubmissionCode;
import io.adampoi.java_auto_grader.model.dto.SubmissionCodeDTO;
import io.adampoi.java_auto_grader.repository.SubmissionCodeRepository;
import io.adampoi.java_auto_grader.repository.SubmissionRepository;
import io.adampoi.java_auto_grader.util.NotFoundException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
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
public class SubmissionCodeService {

    private final SubmissionCodeRepository submissionCodeRepository;
    private final SubmissionRepository submissionRepository;

    public SubmissionCodeService(final SubmissionCodeRepository submissionCodeRepository,
                                 final SubmissionRepository submissionRepository) {
        this.submissionCodeRepository = submissionCodeRepository;
        this.submissionRepository = submissionRepository;
    }

    public Page<SubmissionCodeDTO> findAll(QueryFilter<SubmissionCode> filter, Pageable pageable) {
        final Page<SubmissionCode> page = submissionCodeRepository.findAll(filter, pageable);
        return new PageImpl<>(page.getContent()
                .stream()
                .map(submissionCode -> mapToDTO(submissionCode, new SubmissionCodeDTO()))
                .collect(Collectors.toList()),
                pageable, page.getTotalElements());
    }

    public SubmissionCodeDTO get(final UUID submissionCodeId) {
        return submissionCodeRepository.findById(submissionCodeId)
                .map(submissionCode -> mapToDTO(submissionCode, new SubmissionCodeDTO()))
                .orElseThrow(() -> new NotFoundException("SubmissionCode not found"));
    }

    public SubmissionCodeDTO create(final SubmissionCodeDTO submissionCodeDTO) {
        final SubmissionCode submissionCode = new SubmissionCode();
        mapToEntity(submissionCodeDTO, submissionCode);
        SubmissionCode savedSubmissionCode = submissionCodeRepository.save(submissionCode);
        return mapToDTO(savedSubmissionCode, new SubmissionCodeDTO());
    }

    public SubmissionCodeDTO update(final UUID submissionCodeId, final SubmissionCodeDTO submissionCodeDTO) {
        final SubmissionCode submissionCode = submissionCodeRepository.findById(submissionCodeId)
                .orElseThrow(() -> new NotFoundException("SubmissionCode not found"));
        mapToEntity(submissionCodeDTO, submissionCode);
        SubmissionCode savedSubmissionCode = submissionCodeRepository.save(submissionCode);
        return mapToDTO(savedSubmissionCode, new SubmissionCodeDTO());
    }

    public void delete(final UUID submissionCodeId) {
        submissionCodeRepository.deleteById(submissionCodeId);
    }

    private SubmissionCodeDTO mapToDTO(final SubmissionCode submissionCode, final SubmissionCodeDTO submissionCodeDTO) {
        submissionCodeDTO.setId(submissionCode.getId());
        submissionCodeDTO.setFileName(submissionCode.getFileName());
        submissionCodeDTO.setSourceCode(submissionCode.getSourceCode());
        submissionCodeDTO.setPackagePath(submissionCode.getPackagePath());
        submissionCodeDTO.setClassName(submissionCode.getClassName());
        submissionCodeDTO
                .setSubmission(submissionCode.getSubmission() == null ? null : submissionCode.getSubmission().getId());
        submissionCodeDTO.setCreatedAt(submissionCode.getCreatedAt());
        submissionCodeDTO.setUpdatedAt(submissionCode.getUpdatedAt());
        return submissionCodeDTO;
    }

    private SubmissionCode mapToEntity(final SubmissionCodeDTO submissionCodeDTO, final SubmissionCode submissionCode) {
        if (submissionCodeDTO.getFileName() != null) {
            submissionCode.setFileName(submissionCodeDTO.getFileName());
        }
        if (submissionCodeDTO.getSourceCode() != null) {
            submissionCode.setSourceCode(submissionCodeDTO.getSourceCode());
        }
        if (submissionCodeDTO.getPackagePath() != null) {
            submissionCode.setPackagePath(submissionCodeDTO.getPackagePath());
        }
        if (submissionCodeDTO.getClassName() != null) {
            submissionCode.setClassName(submissionCodeDTO.getClassName());
        }
        if (submissionCodeDTO.getSubmission() != null) {
            final Submission submission = submissionRepository.findById(submissionCodeDTO.getSubmission())
                    .orElseThrow(() -> new NotFoundException("Submission not found"));
            submissionCode.setSubmission(submission);
        }
        if (submissionCodeDTO.getCreatedAt() != null) {
            submissionCode.setCreatedAt(submissionCodeDTO.getCreatedAt());
        }
        if (submissionCodeDTO.getUpdatedAt() != null) {
            submissionCode.setUpdatedAt(submissionCodeDTO.getUpdatedAt());
        }
        return submissionCode;
    }

    public ReferencedWarning getReferencedWarning(final UUID submissionCodeId) {
        // TODO: Implement logic to check for referenced entities if necessary
        return null;
    }
}

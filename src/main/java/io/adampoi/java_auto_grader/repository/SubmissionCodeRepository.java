package io.adampoi.java_auto_grader.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.adampoi.java_auto_grader.domain.SubmissionCode;

public interface SubmissionCodeRepository
        extends JpaRepository<SubmissionCode, UUID>, JpaSpecificationExecutor<SubmissionCode> {

}

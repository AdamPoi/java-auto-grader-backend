package io.adampoi.java_auto_grader.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@Table(name = "submission_codes")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionCode {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String fileName;

    @Lob
    private String sourceCode;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "submission_id", referencedColumnName = "id", nullable = false)
    private Submission submission;
}
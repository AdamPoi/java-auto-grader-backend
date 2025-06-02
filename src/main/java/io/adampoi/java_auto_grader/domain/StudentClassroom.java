package io.adampoi.java_auto_grader.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "student_classrooms")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class StudentClassroom {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "VARCHAR(255)", updatable = false, nullable = false)
    private UUID id;

    @Column
    private OffsetDateTime enrollmentDate;

    @Column
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "id")
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", referencedColumnName = "id")
    private Classroom classroom;

    @CreationTimestamp
    @Column
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column
    private OffsetDateTime updatedAt;

}

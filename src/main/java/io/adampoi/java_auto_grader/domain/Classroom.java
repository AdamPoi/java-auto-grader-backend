package io.adampoi.java_auto_grader.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "classrooms")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true, exclude = {"enrolledStudents"})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Classroom extends Auditable {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String name;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id", referencedColumnName = "id", nullable = true)
    private User teacher;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_classrooms",
            joinColumns = @JoinColumn(name = "classroom_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> enrolledStudents;

}

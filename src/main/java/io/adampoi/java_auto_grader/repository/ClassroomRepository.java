package io.adampoi.java_auto_grader.repository;

import io.adampoi.java_auto_grader.domain.Classroom;
import io.adampoi.java_auto_grader.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;


public interface ClassroomRepository extends JpaRepository<Classroom, UUID>, JpaSpecificationExecutor<Classroom> {
    Classroom findFirstByTeacher(User user);
}

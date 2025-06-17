package io.adampoi.java_auto_grader.seeder;

import io.adampoi.java_auto_grader.domain.Assignment;
import io.adampoi.java_auto_grader.domain.Rubric;
import io.adampoi.java_auto_grader.repository.AssignmentRepository;
import io.adampoi.java_auto_grader.repository.RubricRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class RubricSeeder {

    private final RubricRepository rubricRepository;
    private final AssignmentRepository assignmentRepository;

    public RubricSeeder(RubricRepository rubricRepository,
                        AssignmentRepository assignmentRepository) {
        this.rubricRepository = rubricRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public void seedRubrics() {
        if (rubricRepository.count() > 0) {
            return;
        }

        List<Assignment> assignments = assignmentRepository.findAll();
        if (assignments.isEmpty()) {
            return;
        }

        String[] rubricNames = {
                "Code Quality",
                "Functionality",
                "Documentation",
                "Testing",
                "Design"
        };

        String[] rubricDescriptions = {
                "Code follows best practices, is well-structured and readable",
                "Program works correctly and meets all requirements",
                "Code is properly commented and documented",
                "Comprehensive test cases covering edge cases",
                "Good software design principles and architecture"
        };

        BigDecimal[] rubricPoints = {
                new BigDecimal("20"),
                new BigDecimal("40"),
                new BigDecimal("15"),
                new BigDecimal("15"),
                new BigDecimal("10")
        };

        List<Rubric> rubricsToSave = new ArrayList<>();

        for (Assignment assignment : assignments) {
            for (int i = 0; i < rubricNames.length; i++) {
                Rubric rubric = new Rubric();
                rubric.setName(rubricNames[i]);
                rubric.setDescription(rubricDescriptions[i]);
                rubric.setMaxPoints(rubricPoints[i]);
                rubric.setDisplayOrder(i + 1);
                rubric.setIsActive(true);
                rubric.setAssignment(assignment);
                rubric.setCreatedAt(OffsetDateTime.now());
                rubric.setUpdatedAt(OffsetDateTime.now());

                rubricsToSave.add(rubric);
            }
        }

        rubricRepository.saveAll(rubricsToSave);
    }
}
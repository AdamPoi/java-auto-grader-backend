package io.adampoi.java_auto_grader.seeder;

import io.adampoi.java_auto_grader.domain.Assignment;
import io.adampoi.java_auto_grader.domain.Rubric;
import io.adampoi.java_auto_grader.repository.AssignmentRepository;
import io.adampoi.java_auto_grader.repository.RubricRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        // --- NEW: Define rubric data structures per course ---
        Map<String, List<RubricData>> courseRubricData = new HashMap<>();

        // Rubrics for JAVA101 (Java Basics: Syntax & Data)
        List<RubricData> java101Rubrics = new ArrayList<>();
        java101Rubrics.add(new RubricData("Syntax & Structure", "Code follows basic Java syntax rules and has a readable structure.", 25));
        java101Rubrics.add(new RubricData("Variable Usage", "Correct declaration and use of primitive data types.", 30));
        java101Rubrics.add(new RubricData("Output Correctness", "Program produces the expected output.", 45));
        courseRubricData.put("JAVA101", java101Rubrics);

        // Rubrics for JAVA102 (Java Basics: Control Flow)
        List<RubricData> java102Rubrics = new ArrayList<>();
        java102Rubrics.add(new RubricData("Conditional Logic", "Correct implementation of if/else or switch statements.", 35));
        java102Rubrics.add(new RubricData("Loop Implementation", "Effective use of for/while loops for iteration.", 35));
        java102Rubrics.add(new RubricData("Edge Case Handling", "Program handles various inputs including edge cases.", 30));
        courseRubricData.put("JAVA102", java102Rubrics);

        // Rubrics for JAVA103 (Java Basics: Methods & Modularity)
        List<RubricData> java103Rubrics = new ArrayList<>();
        java103Rubrics.add(new RubricData("Method Definition", "Methods are correctly defined with appropriate parameters and return types.", 30));
        java103Rubrics.add(new RubricData("Modularity & Reusability", "Code is organized into logical methods, promoting reusability.", 35));
        java103Rubrics.add(new RubricData("Parameter Passing", "Parameters are passed and used correctly within methods.", 35));
        courseRubricData.put("JAVA103", java103Rubrics);

        // Rubrics for JAVA104 (Java Basics: Arrays)
        List<RubricData> java104Rubrics = new ArrayList<>();
        java104Rubrics.add(new RubricData("Array Declaration & Initialization", "Arrays are correctly declared and initialized.", 30));
        java104Rubrics.add(new RubricData("Array Traversal", "Correct use of loops to iterate through array elements.", 35));
        java104Rubrics.add(new RubricData("Algorithm Efficiency", "Efficient logic for array operations (e.g., finding max/min).", 35));
        courseRubricData.put("JAVA104", java104Rubrics);

        // Rubrics for JAVA201 (Java Intermediate: OOP)
        List<RubricData> java201Rubrics = new ArrayList<>();
        java201Rubrics.add(new RubricData("OOP Principles (Encapsulation, Inheritance, Polymorphism)", "Correct application of core OOP concepts.", 40));
        java201Rubrics.add(new RubricData("Class & Object Design", "Classes are well-designed with appropriate attributes and methods.", 30));
        java201Rubrics.add(new RubricData("Abstract Classes & Interfaces", "Effective use of abstract classes and interfaces for abstraction/contract.", 30));
        courseRubricData.put("JAVA201", java201Rubrics);

        // Default or general rubrics if a course is not explicitly defined above
        List<RubricData> defaultRubrics = new ArrayList<>();
        defaultRubrics.add(new RubricData("General Correctness", "Program runs without errors and meets primary requirements.", 60));
        defaultRubrics.add(new RubricData("Code Quality", "Code is readable, well-structured, and includes comments.", 40));


        List<Rubric> rubricsToSave = new ArrayList<>();

        for (Assignment assignment : assignments) {
            List<RubricData> rubricsForThisAssignment = courseRubricData.getOrDefault(assignment.getCourse().getCode(), defaultRubrics);

            for (RubricData data : rubricsForThisAssignment) {
                Rubric rubric = new Rubric();
                rubric.setName(data.name);
                rubric.setDescription(data.description);
                rubric.setPoints(data.points);
                rubric.setAssignment(assignment);
                rubricsToSave.add(rubric);
            }
        }

        rubricRepository.saveAll(rubricsToSave);
    }

    // --- NEW: Private helper class for rubric data ---
    private static class RubricData {
        String name;
        String description;
        int points;

        public RubricData(String name, String description, int points) {
            this.name = name;
            this.description = description;
            this.points = points;
        }
    }
}
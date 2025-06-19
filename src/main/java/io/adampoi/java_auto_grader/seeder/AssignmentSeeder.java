package io.adampoi.java_auto_grader.seeder;

import io.adampoi.java_auto_grader.domain.Assignment;
import io.adampoi.java_auto_grader.domain.Course;
import io.adampoi.java_auto_grader.repository.AssignmentRepository;
import io.adampoi.java_auto_grader.repository.CourseRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class AssignmentSeeder {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;

    public AssignmentSeeder(AssignmentRepository assignmentRepository,
                            CourseRepository courseRepository) {
        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
    }

    public void seedAssignments() {
        if (assignmentRepository.count() > 0) {
            System.out.println("Assignments already exist, skipping assignment seeding...");
            return;
        }

        List<Course> courses = courseRepository.findAll();
        if (courses.isEmpty()) {
            System.out.println("No courses found, skipping assignment seeding...");
            return;
        }

        List<Assignment> assignmentsToSave = new ArrayList<>();
        int totalAssignments = 0;

        for (Course course : courses) {
            List<AssignmentData> assignmentDataList = getAssignmentsForCourse(course.getCode());

            for (int i = 0; i < assignmentDataList.size(); i++) {
                AssignmentData data = assignmentDataList.get(i);

                Assignment assignment = new Assignment();
                assignment.setTitle(data.getTitle());
                assignment.setDescription(data.getDescription());
                // ... existing code ...

                assignment.setDueDate(OffsetDateTime.now().plusDays(7 + (i * 7))); // Stagger due dates weekly
//                assignment.setMaxPoints(data.getMaxPoints());
//                assignment.setDifficultyLevel(data.getDifficultyLevel());
//                assignment.setEstimatedHours(data.getEstimatedHours());
//                assignment.setIsActive(true);
//                assignment.setAllowLateSubmissions(true);
//                assignment.setLatePenaltyPercent(new BigDecimal("10"));
//                assignment.setMaxSubmissions(3);
                assignment.setCourse(course);

                assignmentsToSave.add(assignment);
                totalAssignments++;
            }

            System.out.println(String.format("Created %d assignments for course: %s - %s",
                    assignmentDataList.size(),
                    course.getCode(),
                    course.getName()));
        }

        List<Assignment> savedAssignments = assignmentRepository.saveAll(assignmentsToSave);
        System.out.println("Successfully seeded " + savedAssignments.size() + " assignments across " + courses.size() + " courses");
    }

    private List<AssignmentData> getAssignmentsForCourse(String courseCode) {
        List<AssignmentData> assignments = new ArrayList<>();

        switch (courseCode) {
            case "CS101":
                assignments.add(new AssignmentData(
                        "Hello World Program",
                        "Create your first Java program that prints 'Hello, World!' to the console.",
                        "Write a simple Java class with a main method that outputs 'Hello, World!' using System.out.println(). Submit your .java file.",
                        new BigDecimal("50"), "Beginner", 2
                ));
                assignments.add(new AssignmentData(
                        "Variable and Data Types Exercise",
                        "Practice using different data types and variables in Java.",
                        "Create a program that demonstrates the use of int, double, String, and boolean variables. Perform basic operations and print results.",
                        new BigDecimal("75"), "Beginner", 3
                ));
                assignments.add(new AssignmentData(
                        "Control Structures - Grade Calculator",
                        "Build a program that calculates letter grades based on numerical scores.",
                        "Write a program that takes a numerical grade as input and outputs the corresponding letter grade using if-else statements.",
                        new BigDecimal("100"), "Beginner", 4
                ));
                break;

            case "CS201":
                assignments.add(new AssignmentData(
                        "Implement Binary Search Tree",
                        "Create a complete Binary Search Tree implementation with basic operations.",
                        "Implement a BST class with insert, delete, search, and traversal methods. Include proper error handling and edge cases.",
                        new BigDecimal("150"), "Intermediate", 8
                ));
                assignments.add(new AssignmentData(
                        "Graph Algorithms - Shortest Path",
                        "Implement Dijkstra's algorithm for finding shortest paths in a weighted graph.",
                        "Create a graph data structure and implement Dijkstra's algorithm. Test with various graph configurations.",
                        new BigDecimal("200"), "Advanced", 12
                ));
                assignments.add(new AssignmentData(
                        "Hash Table Implementation",
                        "Build a hash table from scratch with collision resolution.",
                        "Implement a hash table using separate chaining for collision resolution. Include rehashing functionality.",
                        new BigDecimal("175"), "Intermediate", 10
                ));
                break;

            case "CS102":
                assignments.add(new AssignmentData(
                        "Design Pattern Implementation - Observer",
                        "Implement the Observer design pattern in a weather monitoring system.",
                        "Create a weather station that notifies multiple display devices when weather data changes. Use the Observer pattern.",
                        new BigDecimal("125"), "Intermediate", 6
                ));
                assignments.add(new AssignmentData(
                        "Factory Pattern - Shape Creator",
                        "Use the Factory pattern to create different types of geometric shapes.",
                        "Implement a shape factory that creates Circle, Rectangle, and Triangle objects based on input parameters.",
                        new BigDecimal("100"), "Intermediate", 5
                ));
                assignments.add(new AssignmentData(
                        "MVC Architecture - Student Management",
                        "Build a simple student management system using MVC architecture.",
                        "Create a student management application with Model, View, and Controller components. Include CRUD operations.",
                        new BigDecimal("175"), "Intermediate", 10
                ));
                break;

            case "CS301":
                assignments.add(new AssignmentData(
                        "Database Design - E-Commerce Schema",
                        "Design a normalized database schema for an e-commerce system.",
                        "Create an ER diagram and implement the database schema with proper normalization. Include tables for customers, products, orders, etc.",
                        new BigDecimal("150"), "Intermediate", 8
                ));
                assignments.add(new AssignmentData(
                        "Advanced SQL Queries",
                        "Write complex SQL queries involving joins, subqueries, and aggregate functions.",
                        "Create queries to analyze sales data, customer behavior, and inventory management. Use advanced SQL features.",
                        new BigDecimal("125"), "Advanced", 6
                ));
                assignments.add(new AssignmentData(
                        "Database Optimization Project",
                        "Optimize database performance through indexing and query optimization.",
                        "Analyze slow queries, create appropriate indexes, and optimize database performance. Document improvements.",
                        new BigDecimal("200"), "Advanced", 12
                ));
                break;

            case "CS151":
                assignments.add(new AssignmentData(
                        "Responsive Portfolio Website",
                        "Create a responsive personal portfolio website using HTML, CSS, and JavaScript.",
                        "Build a multi-page portfolio site with responsive design, interactive elements, and modern CSS features.",
                        new BigDecimal("125"), "Intermediate", 8
                ));
                assignments.add(new AssignmentData(
                        "REST API Development",
                        "Build a RESTful API for a task management system using Spring Boot.",
                        "Create a REST API with endpoints for managing tasks. Include proper HTTP methods, status codes, and error handling.",
                        new BigDecimal("175"), "Intermediate", 10
                ));
                assignments.add(new AssignmentData(
                        "Full-Stack Todo Application",
                        "Develop a complete todo application with React frontend and Spring Boot backend.",
                        "Build a full-stack application with user authentication, CRUD operations, and real-time updates.",
                        new BigDecimal("250"), "Advanced", 15
                ));
                break;

            case "CS250":
                assignments.add(new AssignmentData(
                        "Unit Testing Suite",
                        "Create comprehensive unit tests for a given codebase using JUnit.",
                        "Write unit tests covering various scenarios, edge cases, and error conditions. Achieve high code coverage.",
                        new BigDecimal("100"), "Intermediate", 6
                ));
                assignments.add(new AssignmentData(
                        "Code Review and Refactoring",
                        "Perform code review and refactor legacy code to improve maintainability.",
                        "Analyze existing code, identify issues, and refactor for better design patterns and performance.",
                        new BigDecimal("150"), "Intermediate", 8
                ));
                assignments.add(new AssignmentData(
                        "Agile Project Management",
                        "Plan and manage a software project using Agile methodologies.",
                        "Create user stories, sprint planning, and track progress using Agile tools and practices.",
                        new BigDecimal("125"), "Intermediate", 7
                ));
                break;

            case "CS350":
                assignments.add(new AssignmentData(
                        "TCP Client-Server Application",
                        "Implement a multi-threaded client-server application using TCP sockets.",
                        "Create a chat server that handles multiple clients simultaneously. Implement proper threading and error handling.",
                        new BigDecimal("175"), "Advanced", 10
                ));
                assignments.add(new AssignmentData(
                        "HTTP Server Implementation",
                        "Build a basic HTTP server from scratch that can serve static files.",
                        "Implement HTTP/1.1 protocol features including GET requests, status codes, and MIME types.",
                        new BigDecimal("200"), "Advanced", 12
                ));
                assignments.add(new AssignmentData(
                        "Network Security Analysis",
                        "Analyze network traffic and implement basic security measures.",
                        "Use network analysis tools to examine traffic patterns and implement security protocols.",
                        new BigDecimal("150"), "Advanced", 8
                ));
                break;

            case "CS401":
                assignments.add(new AssignmentData(
                        "Concurrent Programming - Producer Consumer",
                        "Implement the producer-consumer problem using Java concurrency utilities.",
                        "Use BlockingQueue, ThreadPool, and synchronization mechanisms to solve the producer-consumer problem.",
                        new BigDecimal("175"), "Advanced", 10
                ));
                assignments.add(new AssignmentData(
                        "Functional Programming with Streams",
                        "Solve complex data processing problems using Java 8 Streams and functional programming.",
                        "Process large datasets using streams, lambda expressions, and functional interfaces for data transformation.",
                        new BigDecimal("150"), "Advanced", 8
                ));
                assignments.add(new AssignmentData(
                        "Performance Optimization Project",
                        "Optimize the performance of a given application using profiling tools.",
                        "Use profiling tools to identify bottlenecks and optimize code for better performance and memory usage.",
                        new BigDecimal("225"), "Advanced", 14
                ));
                break;

            default:
                // Default assignments for any other courses
                assignments.add(new AssignmentData(
                        "Basic Programming Exercise",
                        "Complete a basic programming assignment for this course.",
                        "Follow the instructions provided in class and submit your solution.",
                        new BigDecimal("100"), "Beginner", 4
                ));
                break;
        }

        return assignments;
    }

    private static class AssignmentData {
        private final String title;
        private final String description;
        private final String instructions;
        private final BigDecimal maxPoints;
        private final String difficultyLevel;
        private final Integer estimatedHours;

        public AssignmentData(String title, String description, String instructions,
                              BigDecimal maxPoints, String difficultyLevel, Integer estimatedHours) {
            this.title = title;
            this.description = description;
            this.instructions = instructions;
            this.maxPoints = maxPoints;
            this.difficultyLevel = difficultyLevel;
            this.estimatedHours = estimatedHours;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getInstructions() {
            return instructions;
        }

        public BigDecimal getMaxPoints() {
            return maxPoints;
        }

        public String getDifficultyLevel() {
            return difficultyLevel;
        }

        public Integer getEstimatedHours() {
            return estimatedHours;
        }
    }
}
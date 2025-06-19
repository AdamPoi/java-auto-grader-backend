package io.adampoi.java_auto_grader.seeder;

import io.adampoi.java_auto_grader.domain.Course;
import io.adampoi.java_auto_grader.domain.Role;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.repository.CourseRepository;
import io.adampoi.java_auto_grader.repository.RoleRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CourseSeeder {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public CourseSeeder(CourseRepository courseRepository,
                        UserRepository userRepository,
                        RoleRepository roleRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public void seedCourses() {
        if (courseRepository.count() > 0) {
            System.out.println("Courses already exist, skipping course seeding...");
            return;
        }

        Role teacherRole = roleRepository.findByName("teacher").orElse(null);
        Role studentRole = roleRepository.findByName("student").orElse(null);

        if (teacherRole == null || studentRole == null) {
            System.out.println("Teacher or Student roles not found, skipping course seeding...");
            return;
        }

        List<User> teachers = userRepository.findByUserRolesContaining(Collections.singleton(teacherRole));
        List<User> students = userRepository.findByUserRolesContaining(Collections.singleton(studentRole));

        if (teachers.isEmpty()) {
            System.out.println("No teachers found, skipping course seeding...");
            return;
        }

        if (students.size() < 30) {
            System.out.println("Not enough students found, skipping course seeding...");
            return;
        }

        // Course data with comprehensive information
        CourseData[] coursesData = {
                new CourseData("CS101", "Introduction to Programming",
                        "Learn fundamental programming concepts using Java. Cover variables, data types, control structures, methods, and basic object-oriented principles.",
                        3, "Beginner"),

                new CourseData("CS201", "Advanced Data Structures",
                        "Master advanced data structures including trees, graphs, hash tables, and heaps. Learn algorithm analysis and complexity theory.",
                        4, "Intermediate"),

                new CourseData("CS102", "Object-Oriented Design Patterns",
                        "Explore design patterns and object-oriented principles. Learn SOLID principles, common design patterns, and software architecture.",
                        3, "Intermediate"),

                new CourseData("CS301", "Database Design & Implementation",
                        "Design and implement relational database systems. Cover SQL, normalization, indexing, and database optimization techniques.",
                        4, "Advanced"),

                new CourseData("CS151", "Full-Stack Web Development",
                        "Build modern web applications using front-end and back-end technologies. Learn React, Spring Boot, RESTful APIs, and deployment.",
                        4, "Intermediate"),

                new CourseData("CS250", "Software Engineering Principles",
                        "Learn software development methodologies, version control, testing strategies, and project management in software engineering.",
                        3, "Intermediate"),

                new CourseData("CS350", "Computer Networks",
                        "Study network protocols, architecture, and security. Cover TCP/IP, HTTP, network programming, and distributed systems.",
                        4, "Advanced"),

                new CourseData("CS401", "Advanced Programming Concepts",
                        "Explore advanced programming topics including concurrency, functional programming, and performance optimization.",
                        4, "Advanced")
        };

        List<Course> coursesToSave = new ArrayList<>();

        // Shuffle students for random distribution
        List<User> shuffledStudents = new ArrayList<>(students);
        Collections.shuffle(shuffledStudents);

        int studentsPerCourse = 6;
        int maxCourses = Math.min(coursesData.length, teachers.size());
        int totalStudentsNeeded = maxCourses * studentsPerCourse;

        if (shuffledStudents.size() < totalStudentsNeeded) {
            studentsPerCourse = shuffledStudents.size() / maxCourses;
            System.out.println("Adjusting students per course to: " + studentsPerCourse);
        }

        for (int i = 0; i < maxCourses; i++) {
            User teacher = teachers.get(i);
            CourseData data = coursesData[i];

            Course course = new Course();
            course.setCode(data.getCode());
            course.setName(data.getName());
            course.setDescription(data.getDescription());
            course.setIsActive(true);

            // Assign students to course (same distribution as classrooms for consistency)
            int startIndex = i * studentsPerCourse;
            int endIndex = Math.min(startIndex + studentsPerCourse, shuffledStudents.size());

            Set<User> courseStudents = shuffledStudents.subList(startIndex, endIndex)
                    .stream()
                    .collect(Collectors.toSet());

            course.setEnrolledUsers(courseStudents);

            coursesToSave.add(course);

            System.out.println(String.format("Created course: %s - %s with teacher: %s and %d students",
                    course.getCode(),
                    course.getName(),
                    teacher.getUsername(),
                    courseStudents.size()));
        }

        List<Course> savedCourses = courseRepository.saveAll(coursesToSave);
        System.out.println("Successfully seeded " + savedCourses.size() + " courses");
    }

    private static class CourseData {
        private final String code;
        private final String name;
        private final String description;
        private final Integer creditHours;
        private final String difficultyLevel;

        public CourseData(String code, String name, String description, Integer creditHours, String difficultyLevel) {
            this.code = code;
            this.name = name;
            this.description = description;
            this.creditHours = creditHours;
            this.difficultyLevel = difficultyLevel;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public Integer getCreditHours() {
            return creditHours;
        }

        public String getDifficultyLevel() {
            return difficultyLevel;
        }
    }
}
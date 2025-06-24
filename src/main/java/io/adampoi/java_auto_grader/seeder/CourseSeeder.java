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

        CourseData[] coursesData = {
                new CourseData("JAVA101", "Java Basics: Syntax & Data", "Learn the very first steps in Java programming, including how to write simple programs, understand basic syntax, declare variables, and work with different data types."),
                new CourseData("JAVA102", "Java Basics: Control Flow", "Master conditional statements (if-else, switch) and looping constructs (for, while, do-while) to control the flow of your Java programs."),
                new CourseData("JAVA103", "Java Basics: Methods & Modularity", "Explore how to create and use methods to break down your code into reusable and manageable units, understanding parameters, return types, and method overloading."),
                new CourseData("JAVA104", "Java Basics: Arrays", "Learn to work with arrays to store and manage collections of data. Understand how to declare, initialize, access, and iterate through one-dimensional arrays."),
                new CourseData("JAVA201", "Java Intermediate: Object-Oriented Programming (OOP)", "Dive deep into the core principles of Object-Oriented Programming in Java, including classes, objects, constructors, encapsulation, inheritance, and polymorphism.")
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
            course.setCreatedByTeacher(teacher);

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


        public CourseData(String code, String name, String description) {
            this.code = code;
            this.name = name;
            this.description = description;

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


    }
}
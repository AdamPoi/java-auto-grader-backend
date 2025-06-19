package io.adampoi.java_auto_grader.seeder;

import io.adampoi.java_auto_grader.domain.Classroom;
import io.adampoi.java_auto_grader.domain.Role;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.repository.ClassroomRepository;
import io.adampoi.java_auto_grader.repository.RoleRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ClassroomSeeder {

    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public ClassroomSeeder(ClassroomRepository classroomRepository,
                           UserRepository userRepository,
                           RoleRepository roleRepository) {
        this.classroomRepository = classroomRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public void seedClassrooms() {
        if (classroomRepository.count() > 0) {
            System.out.println("Classrooms already exist, skipping classroom seeding...");
            return;
        }

        Role teacherRole = roleRepository.findByName("teacher").orElse(null);
        Role studentRole = roleRepository.findByName("student").orElse(null);

        if (teacherRole == null || studentRole == null) {
            System.out.println("Teacher or Student roles not found, skipping classroom seeding...");
            return;
        }

        // Use findAll and filter in Java to avoid detached entities
        List<User> allUsers = userRepository.findAll();
        List<User> teachers = allUsers.stream()
                .filter(user -> user.getUserRoles().contains(teacherRole))
                .collect(Collectors.toList());
        List<User> students = allUsers.stream()
                .filter(user -> user.getUserRoles().contains(studentRole))
                .collect(Collectors.toList());

        if (teachers.isEmpty()) {
            System.out.println("No teachers found, skipping classroom seeding...");
            return;
        }

        if (students.size() < 30) { // Need at least 30 students for 5 classes of 6 each
            System.out.println("Not enough students found (need at least 30), skipping classroom seeding...");
            return;
        }

        // Classroom data with more realistic course information
        ClassroomData[] classroomsData = {
                new ClassroomData("CS 101 - Introduction to Programming", "Fall 2024", "MWF 9:00-10:00 AM"),
                new ClassroomData("CS 201 - Data Structures & Algorithms", "Fall 2024", "TTh 11:00-12:30 PM"),
                new ClassroomData("CS 102 - Object-Oriented Programming", "Fall 2024", "MWF 2:00-3:00 PM"),
                new ClassroomData("CS 301 - Database Management Systems", "Fall 2024", "TTh 2:00-3:30 PM"),
                new ClassroomData("CS 151 - Web Development Fundamentals", "Fall 2024", "MWF 10:00-11:00 AM"),
                new ClassroomData("CS 250 - Software Engineering Principles", "Fall 2024", "TTh 9:30-11:00 AM"),
                new ClassroomData("CS 350 - Computer Networks", "Fall 2024", "MWF 1:00-2:00 PM"),
                new ClassroomData("CS 401 - Advanced Programming Concepts", "Fall 2024", "TTh 3:30-5:00 PM")
        };

        List<Classroom> classroomsToSave = new ArrayList<>();

        // Shuffle students for random distribution
        List<User> shuffledStudents = new ArrayList<>(students);
        Collections.shuffle(shuffledStudents);

        int studentsPerClass = 6;
        int maxClassrooms = Math.min(classroomsData.length, teachers.size());
        int totalStudentsNeeded = maxClassrooms * studentsPerClass;

        if (shuffledStudents.size() < totalStudentsNeeded) {
            studentsPerClass = shuffledStudents.size() / maxClassrooms;
            System.out.println("Adjusting students per class to: " + studentsPerClass);
        }

        for (int i = 0; i < maxClassrooms; i++) {
            User teacher = teachers.get(i);
            ClassroomData data = classroomsData[i];

            Classroom classroom = new Classroom();
            classroom.setName(data.getName());
            classroom.setTeacher(teacher);
            classroom.setCreatedAt(OffsetDateTime.now());
            classroom.setUpdatedAt(OffsetDateTime.now());

            // Assign students to classroom - ensure users are managed entities
            int startIndex = i * studentsPerClass;
            int endIndex = Math.min(startIndex + studentsPerClass, shuffledStudents.size());

            Set<User> classroomStudents = shuffledStudents.subList(startIndex, endIndex)
                    .stream()
                    .collect(Collectors.toSet());

            classroom.setEnrolledStudents(classroomStudents);

            classroomsToSave.add(classroom);

            System.out.println(String.format("Created classroom: %s with teacher: %s and %d students",
                    classroom.getName(),
                    teacher.getUsername(),
                    classroomStudents.size()));
        }

        // Save all classrooms at once
        List<Classroom> savedClassrooms = classroomRepository.saveAll(classroomsToSave);
        System.out.println("Successfully seeded " + savedClassrooms.size() + " classrooms");
    }

    private static class ClassroomData {
        private final String name;
        private final String semester;
        private final String schedule;

        public ClassroomData(String name, String semester, String schedule) {
            this.name = name;
            this.semester = semester;
            this.schedule = schedule;
        }

        public String getName() {
            return name;
        }

        public String getSemester() {
            return semester;
        }

        public String getSchedule() {
            return schedule;
        }
    }
}
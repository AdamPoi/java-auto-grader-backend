package io.adampoi.java_auto_grader.seeder;

import io.adampoi.java_auto_grader.domain.Classroom;
import io.adampoi.java_auto_grader.domain.Role;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.repository.ClassroomRepository;
import io.adampoi.java_auto_grader.repository.RoleRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ClassroomSeeder {

    private static final int MINIMUM_STUDENTS = 30;

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
            log.info("Classrooms already exist, skipping classroom seeding");
            return;
        }

        Role teacherRole = roleRepository.findByName("teacher").orElse(null);
        Role studentRole = roleRepository.findByName("student").orElse(null);

        if (teacherRole == null || studentRole == null) {
            log.warn("Teacher or student roles not found, skipping classroom seeding");
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
            log.warn("No teachers found, skipping classroom seeding");
            return;
        }

        if (students.size() < MINIMUM_STUDENTS) { // Need at least 30 students for 5 classes of 6 each
            log.warn("Not enough students found (need at least {}), skipping classroom seeding", MINIMUM_STUDENTS);
            return;
        }

        // Classroom data with more realistic course information
        ClassroomData[] classroomsData = {
                new ClassroomData("CS 101 - Introduction to Programming"),
                new ClassroomData("CS 201 - Data Structures & Algorithms"),
                new ClassroomData("CS 102 - Object-Oriented Programming"),
                new ClassroomData("CS 301 - Database Management Systems"),
                new ClassroomData("CS 151 - Web Development Fundamentals"),
                new ClassroomData("CS 250 - Software Engineering Principles"),
                new ClassroomData("CS 350 - Computer Networks"),
                new ClassroomData("CS 401 - Advanced Programming Concepts")
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
            log.info("Adjusting students per class to: {}", studentsPerClass);
        }

        for (int i = 0; i < maxClassrooms; i++) {
            User teacher = teachers.get(i);
            ClassroomData data = classroomsData[i];

            Classroom classroom = new Classroom();
            classroom.setName(data.name());
            classroom.setTeacher(teacher);

            // Assign students to classroom - ensure users are managed entities
            int startIndex = i * studentsPerClass;
            int endIndex = Math.min(startIndex + studentsPerClass, shuffledStudents.size());

            Set<User> classroomStudents = shuffledStudents.subList(startIndex, endIndex)
                    .stream()
                    .collect(Collectors.toSet());

            classroom.setEnrolledStudents(classroomStudents);

            classroomsToSave.add(classroom);

            log.info("Created classroom: {} with teacher: {} and {} students",
                    classroom.getName(),
                    teacher.getUsername(),
                    classroomStudents.size());
        }

        // Save all classrooms at once
        List<Classroom> savedClassrooms = classroomRepository.saveAll(classroomsToSave);
        log.info("Successfully seeded {} classrooms", savedClassrooms.size());
    }

    private record ClassroomData(String name) {


    }
}

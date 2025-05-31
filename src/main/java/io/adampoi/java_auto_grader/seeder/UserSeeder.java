package io.adampoi.java_auto_grader.seeder;

import io.adampoi.java_auto_grader.domain.Role;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.repository.RoleRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserSeeder {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    public UserSeeder(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void seedUsers() {
        // Admin user
        if (!userRepository.existsByEmail("admin@example.com")) {
            User admin = new User();
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setIsActive(true);
            Role adminRole = roleRepository.findByName("ADMIN").getFirst();
            admin.setUserRoles(Set.of(adminRole));
            userRepository.save(admin);
        }

        // Teacher user
        if (!userRepository.existsByEmail("teacher@example.com")) {
            User teacher = new User();
            teacher.setEmail("teacher@example.com");
            teacher.setPassword(passwordEncoder.encode("teacher123"));
            teacher.setFirstName("John");
            teacher.setLastName("Teacher");
            teacher.setIsActive(true);
            Role teacherRole = roleRepository.findByName("TEACHER").getFirst();
            teacher.setUserRoles(Set.of(teacherRole));
            userRepository.save(teacher);
        }

        // Student user
        if (!userRepository.existsByEmail("student@example.com")) {
            User student = new User();
            student.setEmail("student@example.com");
            student.setPassword(passwordEncoder.encode("student123"));
            student.setFirstName("Jane");
            student.setLastName("Student");
            student.setIsActive(true);
            Role studentRole = roleRepository.findByName("STUDENT").getFirst();
            student.setUserRoles(Set.of(studentRole));
            userRepository.save(student);
        }
    }
}
package io.adampoi.java_auto_grader.seeder;

import io.adampoi.java_auto_grader.domain.Role;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.repository.RoleRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        // Get all roles at once
        Map<String, Role> roles = roleRepository.findAll().stream()
                .collect(Collectors.toMap(Role::getName, role -> role));

        // Get existing users by role to check what's already seeded
        List<User> existingUsers = userRepository.findAll();
        Map<String, Long> userCountByRole = existingUsers.stream()
                .flatMap(user -> user.getUserRoles().stream())
                .collect(Collectors.groupingBy(Role::getName, Collectors.counting()));

        List<User> usersToSave = new ArrayList<>();

        // Seed admin if none exists
        if (userCountByRole.getOrDefault("admin", 0L) == 0) {
            usersToSave.add(createUser("admin@example.com", "admin123",
                    "Admin", "User", roles.get("admin")));
        }

        // Seed teachers if less than 5 exist
        long existingTeachers = userCountByRole.getOrDefault("teacher", 0L);
        if (existingTeachers < 5) {
            for (int i = (int) existingTeachers + 1; i <= 5; i++) {
                usersToSave.add(createUser("teacher" + i + "@example.com", "teacher123",
                        "Teacher", "User" + i, roles.get("teacher")));
            }
        }

        // Seed students if less than 30 exist
        long existingStudents = userCountByRole.getOrDefault("student", 0L);
        if (existingStudents < 30) {
            for (int i = (int) existingStudents + 1; i <= 30; i++) {
                usersToSave.add(createUser("student" + i + "@example.com", "student123",
                        "Student", "User" + i, roles.get("student")));
            }
        }

        // Batch save all users
        if (!usersToSave.isEmpty()) {
            userRepository.saveAll(usersToSave);
        }
    }

    private User createUser(String email, String password, String firstName, String lastName, Role role) {
        if (role == null) {
            throw new EntityNotFoundException("Role not found");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setIsActive(true);
        user.setUserRoles(Set.of(role));
        return user;
    }
}
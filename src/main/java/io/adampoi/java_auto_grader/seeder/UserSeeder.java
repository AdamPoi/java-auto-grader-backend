package io.adampoi.java_auto_grader.seeder;

import io.adampoi.java_auto_grader.domain.Role;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.repository.RoleRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

        if (userCountByRole.getOrDefault("admin", 0L) == 0) {
            usersToSave.add(User.builder()
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("User")
                    .userRoles(Collections.singleton(roles.get("admin")))
                    .isActive(true)

                    .build());
        }

// Seed teachers if less than 5 exist

        if (userCountByRole.getOrDefault("teacher", 0L) == 0) {
            for (int i = 1; i <= 5; i++) {
                usersToSave.add(User.builder()
                        .email("teacher" + i + "@example.com")
                        .password(passwordEncoder.encode("teacher123"))
                        .firstName("Teacher")
                        .lastName("User" + i)
                        .userRoles(Collections.singleton(roles.get("teacher")))
                        .isActive(true)

                        .nip("124172018" + i)
                        .build());
            }
        }

        if (userCountByRole.getOrDefault("student", 0L) == 0) {
            for (int i = 1; i <= 30; i++) {
                usersToSave.add(User.builder()
                        .email("student" + i + "@example.com")
                        .password(passwordEncoder.encode("student123"))
                        .firstName("Student")
                        .lastName("User" + i)
                        .userRoles(Collections.singleton(roles.get("student")))
                        .nim("214172018" + String.format("%02d", i))
                        .isActive(true)
                        .build());
            }
        }


        // Batch save all users
        if (!usersToSave.isEmpty()) {
            userRepository.saveAll(usersToSave);
        }
    }


}
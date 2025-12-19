package io.adampoi.java_auto_grader.seeder;

import io.adampoi.java_auto_grader.domain.Permission;
import io.adampoi.java_auto_grader.domain.Role;
import io.adampoi.java_auto_grader.repository.PermissionRepository;
import io.adampoi.java_auto_grader.repository.RoleRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class RoleSeeder {

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;

    public RoleSeeder(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    public void seedRoles() {
        if (!roleRepository.existsByName("admin")) {
            Role adminRole = new Role();
            adminRole.setName("admin");
            adminRole.setRolePermissions(new HashSet<>(permissionRepository.findAll()));
            roleRepository.save(adminRole);
        }

        if (!roleRepository.existsByName("teacher")) {
            Role teacherRole = roleRepository.findByName("teacher").orElse(new Role());
            teacherRole.setName("teacher");
            // Assign specific permissions to teacher role
            List<String> teacherPermissionNames = Arrays.asList(
                    "USER:LIST", "USER:READ",
                    "ROLE:LIST", "ROLE:READ",
                    "PERMISSION:LIST", "PERMISSION:READ",
                    "CLASSROOM:LIST", "CLASSROOM:READ",
                    "COURSE:CREATE", "COURSE:LIST", "COURSE:READ", "COURSE:UPDATE", "COURSE:DELETE",
                    "ASSIGNMENT:CREATE", "ASSIGNMENT:LIST", "ASSIGNMENT:READ", "ASSIGNMENT:UPDATE", "ASSIGNMENT:DELETE",
                    "RUBRIC:CREATE", "RUBRIC:LIST", "RUBRIC:READ", "RUBRIC:UPDATE", "RUBRIC:DELETE",
                    "UNIT_TEST:CREATE", "UNIT_TEST:LIST", "UNIT_TEST:READ", "UNIT_TEST:UPDATE", "UNIT_TEST:DELETE",
                    "SUBMISSION:CREATE", "SUBMISSION:LIST", "SUBMISSION:READ", "SUBMISSION:UPDATE", "SUBMISSION:DELETE",
                    "SUBMISSION:GENERATE_FEEDBACK", "SUBMISSION:ASSESSMENT", "SUBMISSION:TEST", "SUBMISSION:GRADE", "SUBMISSION:RUN_CODE"
            );

            Set<Permission> teacherPermissions = permissionRepository.findByNameIn(teacherPermissionNames);
            teacherRole.setRolePermissions(teacherPermissions);
            roleRepository.save(teacherRole);
        }

        if (!roleRepository.existsByName("student")) {
            Role studentRole = roleRepository.findByName("student").orElse(new Role());
            studentRole.setName("student");

            // Assign specific permissions to student role
            List<String> studentPermissionNames = Arrays.asList(
                    "USER:READ",
                    "ROLE:LIST", "ROLE:READ",
                    "PERMISSION:LIST", "PERMISSION:READ",
                    "COURSE:LIST", "COURSE:READ",
                    "CLASSROOM:LIST", "CLASSROOM:READ",
                    "ASSIGNMENT:LIST", "ASSIGNMENT:READ",
                    "RUBRIC:LIST", "RUBRIC:READ",
                    "UNIT_TEST:LIST", "UNIT_TEST:READ",
                    "SUBMISSION:CREATE", "SUBMISSION:LIST", "SUBMISSION:READ", "SUBMISSION:UPDATE",
                    "SUBMISSION:ASSESSMENT", "SUBMISSION:TEST", "SUBMISSION:RUN_CODE"
            );

            Set<Permission> studentPermissions = permissionRepository.findByNameIn(studentPermissionNames);
            studentRole.setRolePermissions(studentPermissions);
            roleRepository.save(studentRole);
        }
    }
}
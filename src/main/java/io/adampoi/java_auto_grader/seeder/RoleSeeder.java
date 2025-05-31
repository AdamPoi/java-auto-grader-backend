package io.adampoi.java_auto_grader.seeder;

import io.adampoi.java_auto_grader.domain.Permission;
import io.adampoi.java_auto_grader.domain.Role;
import io.adampoi.java_auto_grader.repository.PermissionRepository;
import io.adampoi.java_auto_grader.repository.RoleRepository;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class RoleSeeder {

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;

    public RoleSeeder(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public void seedRoles() {
        if (!roleRepository.existsByName("ADMIN")) {
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setRolePermissions(new HashSet<>(permissionRepository.findAll()));
            roleRepository.save(adminRole);
        }

        if (!roleRepository.existsByName("TEACHER")) {
            Role teacherRole = new Role();
            teacherRole.setName("TEACHER");
            Set<Permission> teacherPermissions = permissionRepository.findByNameIn(Arrays.asList(
                    "COURSE_CREATE", "COURSE_READ", "COURSE_UPDATE",
                    "CLASSROOM_CREATE", "CLASSROOM_READ", "CLASSROOM_UPDATE", "CLASSROOM_DELETE",
                    "ASSIGNMENT_CREATE", "ASSIGNMENT_READ", "ASSIGNMENT_UPDATE", "ASSIGNMENT_DELETE",
                    "SUBMISSION_READ", "USER_READ"
            ));
            teacherRole.setRolePermissions(teacherPermissions);
            roleRepository.save(teacherRole);
        }

        if (!roleRepository.existsByName("STUDENT")) {
            Role studentRole = new Role();
            studentRole.setName("STUDENT");
            Set<Permission> studentPermissions = permissionRepository.findByNameIn(Arrays.asList(
                    "COURSE_READ", "CLASSROOM_READ", "ASSIGNMENT_READ",
                    "SUBMISSION_CREATE", "SUBMISSION_READ", "SUBMISSION_UPDATE"
            ));
            studentRole.setRolePermissions(studentPermissions);
            roleRepository.save(studentRole);
        }
    }
}
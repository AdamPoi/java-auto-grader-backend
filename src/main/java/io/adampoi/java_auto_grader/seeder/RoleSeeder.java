package io.adampoi.java_auto_grader.seeder;

import io.adampoi.java_auto_grader.domain.Permission;
import io.adampoi.java_auto_grader.domain.Role;
import io.adampoi.java_auto_grader.repository.PermissionRepository;
import io.adampoi.java_auto_grader.repository.RoleRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
            Set<Permission> teacherPermissions = permissionRepository.findByNameIn(Arrays.asList(
                    "COURSE:CREATE", "COURSE:READ", "COURSE:LIST", "COURSE:UPDATE",
                    "CLASSROOM:CREATE", "CLASSROOM:READ", "CLASSROOM:LIST", "CLASSROOM:UPDATE", "CLASSROOM:DELETE",
                    "ASSIGNMENT:CREATE", "ASSIGNMENT:READ", "ASSIGNMENT:LIST", "ASSIGNMENT:UPDATE", "ASSIGNMENT:DELETE",
                    "SUBMISSION:READ", "SUBMISSION:LIST", "USER:READ", "USER:LIST",
                    "RUBRIC:CREATE", "RUBRIC:READ", "RUBRIC:LIST", "RUBRIC:UPDATE", "RUBRIC:DELETE",
                    "RUBRIC_GRADE:CREATE", "RUBRIC_GRADE:READ", "RUBRIC_GRADE:LIST", "RUBRIC_GRADE:UPDATE", "RUBRIC_GRADE:DELETE",
                    "GRADE_EXECUTION:CREATE", "GRADE_EXECUTION:READ", "GRADE_EXECUTION:LIST", "GRADE_EXECUTION:UPDATE", "GRADE_EXECUTION:DELETE"
            ));
            teacherRole.setRolePermissions(teacherPermissions);
            roleRepository.save(teacherRole);
        }
        if (!roleRepository.existsByName("student")) {
            Role studentRole = roleRepository.findByName("student").orElse(new Role());
            studentRole.setName("student");
            Set<Permission> studentPermissions = permissionRepository.findByNameIn(Arrays.asList(
                    "COURSE:READ", "CLASSROOM:READ", "ASSIGNMENT:READ",
                    "RUBRIC:LIST", "RUBRIC:READ", "RUBRIC_GRADE:LIST", "RUBRIC_GRADE:READ",
                    "SUBMISSION:CREATE", "SUBMISSION:READ", "SUBMISSION:UPDATE",
                    "SUBMISSION_CODE:CREATE", "SUBMISSION_CODE:READ", "SUBMISSION_CODE:UPDATE",
                    "GRADE_EXECUTION:CREATE", "GRADE_EXECUTION:READ", "GRADE_EXECUTION:LIST", "GRADE_EXECUTION:UPDATE"
            ));
            studentRole.setRolePermissions(studentPermissions);
            roleRepository.save(studentRole);
        }
    }
}
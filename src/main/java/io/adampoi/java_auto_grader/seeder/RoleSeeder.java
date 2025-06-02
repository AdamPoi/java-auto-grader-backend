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
                    "COURSE:CREATE", "COURSE:READ", "COURSE:LIST", "COURSE:UPDATE",
                    "CLASSROOM:CREATE", "CLASSROOM:READ", "CLASSROOM:LIST", "CLASSROOM:UPDATE", "CLASSROOM:DELETE",
                    "ASSIGNMENT:CREATE", "ASSIGNMENT:READ", "ASSIGNMENT:LIST", "ASSIGNMENT:UPDATE", "ASSIGNMENT:DELETE",
                    "SUBMISSION:READ", "SUBMISSION:LIST", "USER:READ", "USER:LIST"
            ));
            teacherRole.setRolePermissions(teacherPermissions);
            roleRepository.save(teacherRole);
        }

        if (!roleRepository.existsByName("STUDENT")) {
            Role studentRole = new Role();
            studentRole.setName("STUDENT");
            Set<Permission> studentPermissions = permissionRepository.findByNameIn(Arrays.asList(
                    "COURSE:READ", "CLASSROOM:READ", "ASSIGNMENT:READ",
                    "SUBMISSION:CREATE", "SUBMISSION:READ", "SUBMISSION:UPDATE"
            ));
            studentRole.setRolePermissions(studentPermissions);
            roleRepository.save(studentRole);
        }
    }
}
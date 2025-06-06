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
        Role adminRole = roleRepository.findByName("admin").orElse(new Role());
        adminRole.setName("admin");
        adminRole.setRolePermissions(new HashSet<>(permissionRepository.findAll()));
        roleRepository.save(adminRole);

        Role teacherRole = roleRepository.findByName("teacher").orElse(new Role());
        teacherRole.setName("teacher");
        Set<Permission> teacherPermissions = permissionRepository.findByNameIn(Arrays.asList(
                "COURSE:CREATE", "COURSE:READ", "COURSE:LIST", "COURSE:UPDATE",
                "CLASSROOM:CREATE", "CLASSROOM:READ", "CLASSROOM:LIST", "CLASSROOM:UPDATE", "CLASSROOM:DELETE",
                "ASSIGNMENT:CREATE", "ASSIGNMENT:READ", "ASSIGNMENT:LIST", "ASSIGNMENT:UPDATE", "ASSIGNMENT:DELETE",
                "SUBMISSION:READ", "SUBMISSION:LIST", "USER:READ", "USER:LIST"
        ));
        teacherRole.setRolePermissions(teacherPermissions);
        roleRepository.save(teacherRole);

        Role studentRole = roleRepository.findByName("student").orElse(new Role());
        studentRole.setName("student");
        Set<Permission> studentPermissions = permissionRepository.findByNameIn(Arrays.asList(
                "COURSE:READ", "CLASSROOM:READ", "ASSIGNMENT:READ",
                "SUBMISSION:CREATE", "SUBMISSION:READ", "SUBMISSION:UPDATE"
        ));
        studentRole.setRolePermissions(studentPermissions);
        roleRepository.save(studentRole);
    }
}
package io.adampoi.java_auto_grader.seeder;

import io.adampoi.java_auto_grader.domain.Role;
import io.adampoi.java_auto_grader.repository.PermissionRepository;
import io.adampoi.java_auto_grader.repository.RoleRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

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
            teacherRole.setRolePermissions(new HashSet<>(permissionRepository.findAll()));


            roleRepository.save(teacherRole);
        }
        if (!roleRepository.existsByName("student")) {
            Role studentRole = roleRepository.findByName("student").orElse(new Role());
            studentRole.setName("student");
            studentRole.setRolePermissions(new HashSet<>(permissionRepository.findAll()));

            roleRepository.save(studentRole);
        }
    }
}
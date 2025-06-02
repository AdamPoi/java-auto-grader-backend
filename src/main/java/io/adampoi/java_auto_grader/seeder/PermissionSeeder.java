package io.adampoi.java_auto_grader.seeder;

import io.adampoi.java_auto_grader.domain.Permission;
import io.adampoi.java_auto_grader.repository.PermissionRepository;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class PermissionSeeder {

    private final PermissionRepository permissionRepository;

    public PermissionSeeder(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public void seedPermissions() {
        List<String> domains = Arrays.asList(
                "USER", "ROLE", "COURSE", "CLASSROOM",
                "TEACHER_COURSE", "STUDENT_CLASSROOM",
                "ASSIGNMENT", "SUBMISSION"
        );

        List<String> actions = Arrays.asList("CREATE", "LIST", "READ", "UPDATE", "DELETE");

        for (String domain : domains) {
            for (String action : actions) {
                String name = domain + ":" + action;
                if (!permissionRepository.existsByName(name)) {
                    Permission permission = new Permission();
                    permission.setName(name);
                    permission.setDescription(action + " permission for " + domain);
                    permissionRepository.save(permission);
                }
            }
        }
    }
}
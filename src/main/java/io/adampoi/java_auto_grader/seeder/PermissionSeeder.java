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
                "USER", "ROLE", "PERMISSION", "COURSE", "CLASSROOM",
                "RUBRIC", "SUBMISSION", "UNIT_TEST", "ASSIGNMENT", "RUBRIC_GRADE",
                "SUBMISSION_CODE", "GRADE_EXECUTION"
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

        // Custom permissions
        List<String> customPermissions = Arrays.asList(
                "SUBMISSION:GENERATE_FEEDBACK",
                "SUBMISSION:ASSESSMENT",
                "SUBMISSION:RUN_CODE",
                "SUBMISSION:TEST",
                "SUBMISSION:GRADE"
        );

        for (String name : customPermissions) {
            if (!permissionRepository.existsByName(name)) {
                Permission permission = new Permission();
                permission.setName(name);
                String[] parts = name.split(":");
                String description = parts.length > 1 ? parts[1] + " permission for " + parts[0] : name;
                permission.setDescription(description);
                permission.setDescription(description);
                permissionRepository.save(permission);
            }
        }
    }
}
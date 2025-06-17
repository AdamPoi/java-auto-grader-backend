package io.adampoi.java_auto_grader.seeder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataSeeder {

    private final ClassroomSeeder classroomSeeder;
    private final CourseSeeder courseSeeder;
    private final AssignmentSeeder assignmentSeeder;
    private final RubricSeeder rubricSeeder;
    private final PermissionSeeder permissionSeeder;
    private final RoleSeeder roleSeeder;
    private final UserSeeder userSeeder;

    public DataSeeder(ClassroomSeeder classroomSeeder,
                      CourseSeeder courseSeeder,
                      AssignmentSeeder assignmentSeeder,
                      RubricSeeder rubricSeeder, PermissionSeeder permissionSeeder, RoleSeeder roleSeeder, UserSeeder userSeeder) {
        this.classroomSeeder = classroomSeeder;
        this.courseSeeder = courseSeeder;
        this.assignmentSeeder = assignmentSeeder;
        this.rubricSeeder = rubricSeeder;
        this.permissionSeeder = permissionSeeder;
        this.roleSeeder = roleSeeder;
        this.userSeeder = userSeeder;
    }

    public void seedData() {
        log.info("Starting data seeding...");
        permissionSeeder.seedPermissions();
        log.info("Permissions seeding completed.");
        roleSeeder.seedRoles();
        log.info("Roles seeding completed.");
        userSeeder.seedUsers();
        log.info("Users seeding completed.");
        classroomSeeder.seedClassrooms();
        log.info("Classrooms seeding completed.");
        courseSeeder.seedCourses();
        log.info("Courses seeding completed.");
        assignmentSeeder.seedAssignments();
        log.info("Assignments seeding completed.");
        rubricSeeder.seedRubrics();
        log.info("Rubrics seeding completed.");

        log.info("Data seeding completed.");

    }
}
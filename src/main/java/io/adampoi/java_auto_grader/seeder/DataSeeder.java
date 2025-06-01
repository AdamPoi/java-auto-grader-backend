package io.adampoi.java_auto_grader.seeder;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("development")
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final PermissionSeeder permissionSeeder;

    private final RoleSeeder roleSeeder;

    private final UserSeeder userSeeder;

    public DataSeeder(PermissionSeeder permissionSeeder, RoleSeeder roleSeeder, UserSeeder userSeeder) {
        this.permissionSeeder = permissionSeeder;
        this.roleSeeder = roleSeeder;
        this.userSeeder = userSeeder;
    }

    @Override
    public void run(String... args) {
        log.info("Starting data seeding...");
        permissionSeeder.seedPermissions();
        log.info("Permissions seeding completed.");
        roleSeeder.seedRoles();
        log.info("Roles seeding completed.");
        userSeeder.seedUsers();
        log.info("Users seeding completed.");
        log.info("Data seeding completed.");
    }
}
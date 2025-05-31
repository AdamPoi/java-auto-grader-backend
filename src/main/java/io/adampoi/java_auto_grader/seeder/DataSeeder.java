package io.adampoi.java_auto_grader.seeder;


import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("development")
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
        System.out.println("Data seeding started...");
        permissionSeeder.seedPermissions();
        roleSeeder.seedRoles();
        userSeeder.seedUsers();

        System.out.println("Data seeding completed!");
    }
}
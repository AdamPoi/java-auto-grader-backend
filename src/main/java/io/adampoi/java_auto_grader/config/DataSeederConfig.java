package io.adampoi.java_auto_grader.config;

import io.adampoi.java_auto_grader.seeder.DataSeeder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"development", "test"}) // Only run in development or test environments
public class DataSeederConfig {

    @Bean
    CommandLineRunner initDatabase(DataSeeder dataSeeder) {
        return args -> {
            dataSeeder.seedData();
        };
    }
}
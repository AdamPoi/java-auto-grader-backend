package io.adampoi.java_auto_grader.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Configuration
@EntityScan("io.adampoi.java_auto_grader.domain")
@EnableJpaRepositories("io.adampoi.java_auto_grader.repository")
@EnableTransactionManagement
public class DomainConfig {

}

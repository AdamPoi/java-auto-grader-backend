package io.adampoi.java_auto_grader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class JavaAutoGraderApplication {

	public static void main(String[] args) {
		SpringApplication.run(JavaAutoGraderApplication.class, args);
	}

}

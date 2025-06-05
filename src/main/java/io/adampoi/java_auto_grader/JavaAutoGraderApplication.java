package io.adampoi.java_auto_grader;

import io.adampoi.java_auto_grader.filter.UserFilterDef;
import io.github.acoboh.query.filter.jpa.annotations.EnableQueryFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableAspectJAutoProxy
@EnableQueryFilter(basePackageClasses = UserFilterDef.class)
public class JavaAutoGraderApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaAutoGraderApplication.class, args);
    }

}

package io.adampoi.java_auto_grader.config;

import io.adampoi.java_auto_grader.resolvers.AuthenticationUserResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;


@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    private final AuthenticationUserResolver authenticationUserResolver;

    public WebConfiguration(AuthenticationUserResolver authenticationUserResolver) {
        this.authenticationUserResolver = authenticationUserResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authenticationUserResolver);
    }

}

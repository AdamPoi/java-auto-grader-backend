package io.adampoi.java_auto_grader.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // Apply to methods
@Retention(RetentionPolicy.RUNTIME) // Available at runtime for AOP
public @interface Loggable {
    boolean logArgs() default true;

    boolean logResult() default true;

    boolean logExecutionTime() default true;
}
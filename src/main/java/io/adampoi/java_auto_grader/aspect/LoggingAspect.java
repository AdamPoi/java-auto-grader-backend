package io.adampoi.java_auto_grader.aspect;


import io.adampoi.java_auto_grader.annotation.Loggable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.UUID;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-ID";
    private static final String CORRELATION_ID_LOG_VAR_NAME = "correlationId";

    @Pointcut("within(io.adampoi.java_auto_grader.rest..*)")
    public void controllerPackagePointcut() {
    }

    @Pointcut("within(io.adampoi.java_auto_grader.service..*)")
    public void servicePackagePointcut() {
    }

    @Pointcut("within(io.adampoi.java_auto_grader.repository..*)")
    public void repositoryPackagePointcut() {
    }

    @Before("controllerPackagePointcut()")
    public void logBeforeController(JoinPoint joinPoint) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String correlationId = request.getHeader(CORRELATION_ID_HEADER_NAME);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put(CORRELATION_ID_LOG_VAR_NAME, correlationId); // Set for all logs in this thread

        {
            { // Modified line: Removed logging of arguments
                log.info("CONTROLLER REQ ===> {} {} from IP {}",
                        request.getMethod(),
                        request.getRequestURI(),
                        request.getRemoteAddr());
            }
        }
    }

    @AfterReturning(pointcut = "controllerPackagePointcut()", returning = "result")
    public void logAfterController(JoinPoint joinPoint, Object result) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        {
            { // Modified line:  logging a message instead of the result
                log.info("CONTROLLER RES <=== {} {} : {}",
                        request.getMethod(),
                        request.getRequestURI(),
                        "Request Completed");
            }
        }
        MDC.remove(CORRELATION_ID_LOG_VAR_NAME); // Clean up MDC
    }


    @AfterThrowing(pointcut = "controllerPackagePointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        {
            { // Modified lines: Removed stack trace from log
                log.error("Exception in {}.{}() with cause = '{}' and exception = '{}'. Full stack trace:",
                        joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(),
                        e.getCause() != null ? e.getCause() : "NULL",
                        e.getMessage(),
                        e);
            }
        }
    }

    @Pointcut("@annotation(io.adampoi.java_auto_grader.annotation.Loggable)")
    public void loggableMethodsPointcut() {
    }

    @Around("loggableMethodsPointcut() && @annotation(loggableConfig)") // Bind annotation instance
    public Object logAnnotatedMethods(ProceedingJoinPoint joinPoint, Loggable loggableConfig) throws Throwable {
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        if (loggableConfig.logArgs()) {
            log.info("Enter (Annotated): {}.{}() with args: {}", className, methodName, Arrays.toString(joinPoint.getArgs()));
        } else {
            log.info("Enter (Annotated): {}.{}()", className, methodName);
        }

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            log.error("Exception in (Annotated) {}.{}()", className, methodName, throwable);
            throw throwable;
        }

        if (loggableConfig.logExecutionTime()) {
            long timeTaken = System.currentTimeMillis() - startTime;
            if (loggableConfig.logResult()) {
                log.info("Exit (Annotated): {}.{}() with result: {}. Execution time: {} ms", className, methodName,
                        result != null ? result.toString().substring(0, Math.min(result.toString().length(), 100)) : "null", timeTaken);
            } else {
                log.info("Exit (Annotated): {}.{}(). Execution time: {} ms", className, methodName, timeTaken);
            }
        } else if (loggableConfig.logResult()) {
            log.info("Exit (Annotated): {}.{}() with result: {}", className, methodName,
                    result != null ? result.toString().substring(0, Math.min(result.toString().length(), 100)) : "null");
        }
        return result;
    }
}
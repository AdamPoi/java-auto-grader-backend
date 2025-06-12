package io.adampoi.java_auto_grader.model.arguments;

import io.adampoi.java_auto_grader.model.type.GradeArguments;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class TimeoutArguments extends GradeArguments {
    private String className;
    private String methodName;
    private Long timeoutMs = 5000L;
    private List<TimeoutTestInput> testInputs;
    private Integer memoryLimitMB = 512;
    private Boolean allowEarlyTermination = true;
    private Integer retryAttempts = 3;

    @Override
    public String getType() {
        return "TIMEOUT";
    }

    @Data
    public static class TimeoutTestInput {
        private Integer size;
        private String type; // "random", "sorted", "reverse", "custom"
        private Object customData;
        private String description;
    }
}
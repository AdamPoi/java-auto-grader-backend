package io.adampoi.java_auto_grader.model.arguments;

import io.adampoi.java_auto_grader.model.type.GradeArguments;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class FunctionalityArguments extends GradeArguments {
    private String className;
    private String methodName;
    private List<FunctionTestCase> testCases;
    private Double tolerance = 0.001;
    private Boolean strictTypeChecking = true;
    private Long timeoutMs = 5000L;

    @Override
    public String getType() {
        return "FUNCTIONALITY";
    }

    @Data
    public static class FunctionTestCase {
        private List<Object> inputs;
        private Object expected;
        private String description;
    }
}
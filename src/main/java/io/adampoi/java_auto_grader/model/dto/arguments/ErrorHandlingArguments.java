package io.adampoi.java_auto_grader.model.dto.arguments;

import io.adampoi.java_auto_grader.model.dto.GradeArguments;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ErrorHandlingArguments extends GradeArguments {
    private String className;
    private String methodName;
    private List<String> expectedExceptions;
    private List<ExceptionTestCase> testInputs;
    private Boolean mustThrowException = true;
    private Boolean checkExceptionMessage = false;
    private List<String> expectedMessages;

    @Override
    public String getType() {
        return "ERROR_HANDLING";
    }

    @Data
    public static class ExceptionTestCase {
        private List<Object> args;
        private String expectedException;
        private String expectedMessage;
    }
}
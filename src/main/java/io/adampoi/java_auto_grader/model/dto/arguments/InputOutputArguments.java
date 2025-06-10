package io.adampoi.java_auto_grader.model.dto.arguments;

import io.adampoi.java_auto_grader.model.dto.GradeArguments;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class InputOutputArguments extends GradeArguments {
    private String mainClass;
    private List<IOTestCase> testCases;
    private Boolean trimWhitespace = true;
    private Boolean ignoreCase = false;
    private Long timeoutMs = 10000L;
    private Boolean useSystemIn = true;

    @Override
    public String getType() {
        return "INPUT_OUTPUT";
    }

    @Data
    public static class IOTestCase {
        private String input;
        private String expectedOutput;
        private String description;
    }
}
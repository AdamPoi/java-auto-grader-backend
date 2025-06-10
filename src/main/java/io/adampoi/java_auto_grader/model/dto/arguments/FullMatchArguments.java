package io.adampoi.java_auto_grader.model.dto.arguments;

import io.adampoi.java_auto_grader.model.dto.GradeArguments;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class FullMatchArguments extends GradeArguments {
    private String className;
    private String methodName;
    private List<FullMatchTestCase> testCases;
    private Boolean trimSpaces = true;
    private Boolean normalizeWhitespace = false;

    @Override
    public String getType() {
        return "FULL_MATCH";
    }

    @Data
    public static class FullMatchTestCase {
        private Object input;
        private String exactMatch;
        private Boolean caseSensitive = true;
        private String description;
    }
}
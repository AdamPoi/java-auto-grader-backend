package io.adampoi.java_auto_grader.model.arguments;

import io.adampoi.java_auto_grader.model.type.GradeArguments;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class RegexMatchArguments extends GradeArguments {
    private String className;
    private String methodName;
    private List<RegexPattern> patterns;
    private List<String> flags; // ["CASE_INSENSITIVE", "MULTILINE", "DOTALL"]

    @Override
    public String getType() {
        return "REGEX_MATCH";
    }

    @Data
    public static class RegexPattern {
        private String input;
        private String pattern;
        private Boolean shouldMatch;
        private String description;
    }
}
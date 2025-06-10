package io.adampoi.java_auto_grader.model.dto.arguments;

import io.adampoi.java_auto_grader.model.dto.GradeArguments;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class CodeStyleArguments extends GradeArguments {
    private List<StyleRule> rules;
    private Integer maxLineLength = 120;
    private Boolean requireJavadoc = false;
    private String indentationStyle = "spaces"; // "spaces" or "tabs"
    private Integer indentationSize = 4;
    private Boolean checkBraceStyle = true;
    private List<String> allowedImports;
    private List<String> forbiddenKeywords;

    @Override
    public String getType() {
        return "CODE_STYLE";
    }

    @Data
    public static class StyleRule {
        private String type;
        private String pattern;
        private List<String> appliesTo; // ["methods", "variables", "classes", "constants"]
        private Boolean required = true;
        private String description;
    }
}
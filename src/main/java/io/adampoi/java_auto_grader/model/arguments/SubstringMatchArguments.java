package io.adampoi.java_auto_grader.model.arguments;

import io.adampoi.java_auto_grader.model.type.GradeArguments;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class SubstringMatchArguments extends GradeArguments {
    private String className;
    private String methodName;
    private List<String> requiredSubstrings;
    private List<String> forbiddenSubstrings;
    private Boolean caseSensitive = false;
    private Boolean mustContainAll = true;
    private Boolean allowPartialMatches = false;

    @Override
    public String getType() {
        return "SUBSTRING_MATCH";
    }
}
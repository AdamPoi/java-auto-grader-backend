package io.adampoi.java_auto_grader.model.arguments;


import io.adampoi.java_auto_grader.model.type.GradeArguments;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class CompilationArguments extends GradeArguments {
    private String className;
    private String packageName;
    private List<String> requiredMethods;
    private List<String> requiredFields;
    private List<String> requiredConstructors;
    private Boolean checkModifiers = true;
    private Boolean allowWarnings = false;

    @Override
    public String getType() {
        return "COMPILATION";
    }
}
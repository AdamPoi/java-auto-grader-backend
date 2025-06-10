package io.adampoi.java_auto_grader.model.dto.arguments;

import io.adampoi.java_auto_grader.model.dto.GradeArguments;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class CodeStructureArguments extends GradeArguments {
    private List<RequiredClass> requiredClasses;
    private Boolean requireMainMethod = false;
    private Integer maxClassCount;
    private Integer minMethodsPerClass;
    private Boolean requireConstructor = false;
    private Boolean inheritanceRequired = false;
    private List<String> interfaceImplementation;

    @Override
    public String getType() {
        return "CODE_STRUCTURE";
    }

    @Data
    public static class RequiredClass {
        private String name;
        private Boolean mustBePublic = true;
        private Boolean mustBeAbstract = false;
        private Boolean mustBeFinal = false;
        private List<RequiredMethod> requiredMethods;
        private List<RequiredField> requiredFields;
        private String extendsClass;
        private List<String> implementsInterfaces;
    }

    @Data
    public static class RequiredMethod {
        private String name;
        private String returnType;
        private List<String> parameters;
        private Boolean mustBePublic = true;
        private Boolean mustBeStatic = false;
        private Boolean mustBeFinal = false;
        private Boolean mustBeAbstract = false;
    }

    @Data
    public static class RequiredField {
        private String name;
        private String type;
        private Boolean mustBePrivate = false;
        private Boolean mustBePublic = false;
        private Boolean mustBeStatic = false;
        private Boolean mustBeFinal = false;
        private Object defaultValue;
    }
}
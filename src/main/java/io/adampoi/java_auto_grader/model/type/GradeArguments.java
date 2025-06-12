package io.adampoi.java_auto_grader.model.type;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.adampoi.java_auto_grader.model.arguments.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CompilationArguments.class, name = "COMPILATION"),
        @JsonSubTypes.Type(value = ErrorHandlingArguments.class, name = "ERROR_HANDLING"),
        @JsonSubTypes.Type(value = FunctionalityArguments.class, name = "FUNCTIONALITY"),
        @JsonSubTypes.Type(value = InputOutputArguments.class, name = "INPUT_OUTPUT"),
        @JsonSubTypes.Type(value = DataTypeArguments.class, name = "DATA_TYPE"),
        @JsonSubTypes.Type(value = RegexMatchArguments.class, name = "REGEX_MATCH"),
        @JsonSubTypes.Type(value = FullMatchArguments.class, name = "FULL_MATCH"),
        @JsonSubTypes.Type(value = SubstringMatchArguments.class, name = "SUBSTRING_MATCH"),
        @JsonSubTypes.Type(value = TimeoutArguments.class, name = "TIMEOUT"),
        @JsonSubTypes.Type(value = FileTestingArguments.class, name = "FILE_TESTING"),
        @JsonSubTypes.Type(value = ScriptTestingArguments.class, name = "SCRIPT_TESTING"),
        @JsonSubTypes.Type(value = CodeStyleArguments.class, name = "CODE_STYLE"),
        @JsonSubTypes.Type(value = CodeStructureArguments.class, name = "CODE_STRUCTURE"),
        @JsonSubTypes.Type(value = ManualGradingArguments.class, name = "MANUAL_GRADING"),
        @JsonSubTypes.Type(value = OtherArguments.class, name = "OTHER")
})
public abstract class GradeArguments {
    public abstract String getType();
}
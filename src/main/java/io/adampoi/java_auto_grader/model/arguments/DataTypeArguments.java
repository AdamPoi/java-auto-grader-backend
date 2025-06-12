package io.adampoi.java_auto_grader.model.arguments;

import io.adampoi.java_auto_grader.model.type.GradeArguments;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class DataTypeArguments extends GradeArguments {
    private String className;
    private String methodName;
    private List<DataTypeTest> typeTests;
    private Boolean strictTypeChecking = true;
    private Boolean allowAutoboxing = true;
    private Boolean checkReturnType = true;
    private Boolean checkParameterTypes = true;

    @Override
    public String getType() {
        return "DATA_TYPE";
    }

    @Data
    public static class DataTypeTest {
        private String expectedReturnType;
        private List<String> expectedParameterTypes;
        private List<Object> testInputs;
        private String description;
    }
}
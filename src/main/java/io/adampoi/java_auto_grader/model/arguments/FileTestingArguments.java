package io.adampoi.java_auto_grader.model.arguments;

import io.adampoi.java_auto_grader.model.type.GradeArguments;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class FileTestingArguments extends GradeArguments {
    private String className;
    private List<InputFile> inputFiles;
    private List<ExpectedOutputFile> expectedOutputFiles;
    private Boolean cleanupAfterTest = true;
    private String workingDirectory = "./temp";

    @Override
    public String getType() {
        return "FILE_TESTING";
    }

    @Data
    public static class InputFile {
        private String path;
        private String content;
        private Boolean required = true;
    }

    @Data
    public static class ExpectedOutputFile {
        private String path;
        private Boolean mustExist;
        private String expectedContent;
        private Boolean exactMatch = false;
        private Long minSizeBytes;
        private Long maxSizeBytes;
    }
}
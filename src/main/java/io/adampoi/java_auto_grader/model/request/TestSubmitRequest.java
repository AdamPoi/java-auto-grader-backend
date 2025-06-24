package io.adampoi.java_auto_grader.model.request;

import io.adampoi.java_auto_grader.model.type.CodeFile;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestSubmitRequest {
    private List<CodeFile> sourceFiles;
    private List<CodeFile> testFiles;
    private String mainClassName;

    private String submissionId;
    private String userId;
    private String assignmentId;

    @Pattern(regexp = "gradle|maven",
            message = "Build Tool must be one of: gradle, maven")
    private String buildTool;

}


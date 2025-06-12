package io.adampoi.java_auto_grader.model.request;

import io.adampoi.java_auto_grader.model.dto.CodeFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCodeRequest {
    private List<CodeFile> sourceFiles;
    private List<CodeFile> testFiles;
    private String mainClassName;

}

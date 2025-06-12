package io.adampoi.java_auto_grader.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompilationError {
    private String errorFile;
    private int line;
    private String errorMessage;
    private String codeSnippet;
    private String pointer;
}
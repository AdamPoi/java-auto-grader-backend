package io.adampoi.java_auto_grader.model.response;

import io.adampoi.java_auto_grader.model.type.CompilationError;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunCodeResponse {
    private boolean success;
    private String output;
    private String error;
    private String exception;
    private long executionTime;
    private List<CompilationError> compilationErrors;


}

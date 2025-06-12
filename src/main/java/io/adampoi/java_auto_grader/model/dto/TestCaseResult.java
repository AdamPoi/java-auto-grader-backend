package io.adampoi.java_auto_grader.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseResult {
    private String className;
    private String methodName;
    private String status; // "PASSED", "FAILED", "ERROR", "SKIPPED"
    private double executionTime;
    private String failureMessage;
    private String errorMessage;
    private String stackTrace;
}

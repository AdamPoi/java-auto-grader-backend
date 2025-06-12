package io.adampoi.java_auto_grader.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestSuiteResult {
    private String name;
    private int totalTests;
    private int failures;
    private int errors;
    private int skipped;
    private double executionTime;
    private List<TestCaseResult> testCases;
}

package io.adampoi.java_auto_grader.model.dto;

import lombok.Data;

@Data
public class TestResultDTO {
    private String name;
    private String testClass;
    private String status;
    private String error;
    private int executionTime;
}

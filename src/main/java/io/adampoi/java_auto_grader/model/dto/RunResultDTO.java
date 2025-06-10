package io.adampoi.java_auto_grader.model.dto;

import lombok.Data;

@Data
public class RunResultDTO {
    private String output;
    private String error;
    private Integer executionTime;
    private String status;
}

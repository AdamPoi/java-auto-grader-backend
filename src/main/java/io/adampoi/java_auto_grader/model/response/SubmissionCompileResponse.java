package io.adampoi.java_auto_grader.model.response;

import io.adampoi.java_auto_grader.model.dto.RunResultDTO;
import io.adampoi.java_auto_grader.model.dto.TestResultDTO;
import lombok.Data;

import java.util.List;

@Data
public class SubmissionCompileResponse {
    private Integer total;

    private Integer score;

    private Integer passed;

    private Integer failed;

    private Integer skipped;

    private RunResultDTO run;

    private List<TestResultDTO> tests;
}

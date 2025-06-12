package io.adampoi.java_auto_grader.model.type;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProcessResult {
    private int exitCode;
    private String output;
    private String errors;
    private long executionTime;

    public boolean isSuccess() {
        return exitCode == 0;
    }
}
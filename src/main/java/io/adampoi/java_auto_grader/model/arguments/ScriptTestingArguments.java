package io.adampoi.java_auto_grader.model.arguments;

import io.adampoi.java_auto_grader.model.type.GradeArguments;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class ScriptTestingArguments extends GradeArguments {
    private String scriptType; // "bash", "powershell", "python", "node"
    private String scriptContent;
    private String workingDirectory = "./student_code";
    private Map<String, String> environmentVars;
    private Integer expectedExitCode = 0;
    private Long timeoutMs = 30000L;
    private Boolean captureOutput = true;
    private List<String> arguments;
    private String setupScript;
    private String cleanupScript;

    @Override
    public String getType() {
        return "SCRIPT_TESTING";
    }
}
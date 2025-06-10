package io.adampoi.java_auto_grader.model.dto.arguments;

import io.adampoi.java_auto_grader.model.dto.GradeArguments;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class OtherArguments extends GradeArguments {
    private String customTestType;
    private Map<String, Object> customProperties;
    private List<String> dependencies;
    private String configurationUrl;
    private Map<String, String> environmentSettings;
    private Long timeoutMs = 30000L;
    private String setupScript;
    private String cleanupScript;

    @Override
    public String getType() {
        return "OTHER";
    }
}